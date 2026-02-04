package com.backend.givr.organization.service.verify;

import com.backend.givr.organization.entity.OrganizationVerificationSession;
import lombok.Getter;
import lombok.Setter;

// Mock data classes
@Getter
public class OrganizationClaim {
    private final String cacRegistrationNumber;
    private final String organizationName;
    private final Address registeredAddress;

    public OrganizationClaim(String cac, String name, Address address) {
        this.cacRegistrationNumber = cac;
        this.organizationName = name;
        this.registeredAddress = address;
    }

    public OrganizationClaim(OrganizationVerificationSession verificationSession){
        this.cacRegistrationNumber = verificationSession.getClaimedCACRegNumber();
        this.organizationName = verificationSession.getClaimedOrgName();
        this.registeredAddress = verificationSession.getClaimedAddress();
    }

}
