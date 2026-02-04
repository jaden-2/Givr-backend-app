package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.CACResponse;
import com.backend.givr.organization.entity.OrganizationVerificationSession;
import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.organization.repo.OrganizationCACResponseRepo;
import com.backend.givr.organization.repo.OrganizationVerificationSessionRepo;
import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.organization.service.verify.CACRecord;
import com.backend.givr.organization.service.verify.OrganizationClaim;
import com.backend.givr.organization.service.verify.OrganizationVerifier;
import com.backend.givr.organization.service.verify.VerificationResult;
import com.backend.givr.shared.dtos.PaymentInitResponse;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.ApiAuthenticationException;
import com.backend.givr.shared.service.LocationService;
import com.backend.givr.shared.service.MonnifyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VerificationService {
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private OrganizationVerifier verifier;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrganizationCACResponseRepo repo;

    @Autowired
    private OrganizationVerificationSessionRepo verificationSessionRepo;
    private final Logger logger = LoggerFactory.getLogger(VerificationService.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MonnifyClient paymentGateway;
    @Autowired
    private LocationService locationService;
    private QoreIdAuthResponse credentials;

    @Value("${QoreId.client.id}")
    private String clientId;
    @Value("${QoreId.client.secret}")
    private String clientSecret;
    @Value("${QoreId.baseUrl}")
    private String baseUrl;

    private static final Double VERIFICATION_AMOUNT = 1000.0;

    private record QoreIdAuthResponse(
            String accessToken,
            String tokenType,
            long expiresIn
    ){}
    private LocalDateTime expirationTime;

    public VerificationService(){
       authenticate();
       this.expirationTime = LocalDateTime.now().plusSeconds(credentials.expiresIn);
    }

    private <T> ResponseEntity<T> fetch(HttpMethod requestType, String authHeader, String authToken, String path, Object body, Class<T> responseType){
        var endpoint = baseUrl + path;
        HttpHeaders headers = new HttpHeaders();

        if(authHeader != null && authToken != null)
            headers.add("Authorization", authHeader + " " + authToken);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.TEXT_PLAIN));
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(endpoint, requestType, entity, responseType);
    }

    public void authenticate(){
        Map<String, String> body = Map.of("clientId", clientId, "secret", clientSecret);

        ResponseEntity<QoreIdAuthResponse> response = fetch(HttpMethod.POST, null,null, "/token", body, QoreIdAuthResponse.class);

        if(response.getStatusCode().is2xxSuccessful()){
            this.credentials = response.getBody();
        }else {
            logger.error("Failed to authenticate with QoreId. Could not get access token");
            throw new ApiAuthenticationException("Failed to authenticate with QoreId");
        }
    }

    public CACResponse getOrganizationCACInfo(String regNumber) throws BadRequestException {
        var path = "/v1/ng/identities/cac-basic";
        if(LocalDateTime.now().isAfter(expirationTime)){
            authenticate();
        }
        var response = fetch(HttpMethod.POST, credentials.tokenType, credentials.accessToken, path,
                Map.of("regNumber", regNumber), CACResponse.class);

        if(response.getStatusCode().is2xxSuccessful()){
            var data = response.getBody();
            if(data == null)
                return null;
            data.setId(regNumber);
            return repo.save(data);
        }else throw new BadRequestException("Bad request");
    }

    /**
     * A verification session is initiated when a user makes claims about an organization
     * This claim needs to be verified by fetching information from a reputable source*/
    public String createVerificationSession(Organization organization, OrganizationUpdateDto updateDto, String email) throws BadRequestException {
        if(updateDto.getCacRegNumber() == null)
            return null;
        OrganizationVerificationSession verificationSession = new OrganizationVerificationSession(organization, updateDto);
        Location location = locationService.createLocation(updateDto.getLocation());
        verificationSession.setClaimedLocation(location);
        organization.setStatus(VerificationStatus.PENDING);
        verificationSessionRepo.save(verificationSession);

        // Initiate payment for verification
        VerificationPayment initiatedPayment = new VerificationPayment(new BigDecimal(VERIFICATION_AMOUNT),
                String.format("Payment for verification of %s account", organization.getOrganizationId()),
                organization);
        PaymentInitResponse gatewayResponse = paymentGateway.initializePayment(organization.getOrganizationId(), email, initiatedPayment);

        return gatewayResponse.responseBody().checkoutUrl();
    }

    public OrganizationVerificationSession findByOrganization(Organization organization){
        return verificationSessionRepo.findByOrganization(organization).orElseThrow();
    }


    public void verifyOrganization(Organization organization, String cacRegNumber, OrganizationDetails details){
        OrganizationVerificationSession session = findByOrganization(organization);
        CACResponse cacResponse = repo.findById(cacRegNumber).orElseThrow();

        // Create a uniform format for address for both claimed address and official address
        OrganizationClaim claim = new OrganizationClaim(session);
        CACRecord cacRecord = new CACRecord(cacResponse);

        VerificationResult result = verifier.verifyOrganization(claim, cacRecord);

        if(result.isOverallVerified()){
            organization.setStatus(VerificationStatus.VERIFIED);
            emailService.notifyOrgVerificationSuccess(details.getUsername());
            return;
        }

        // When an address fails to match, there's no similarity at all. Different state, LGA, and address

    }

}
