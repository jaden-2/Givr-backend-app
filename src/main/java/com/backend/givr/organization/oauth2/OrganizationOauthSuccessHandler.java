package com.backend.givr.organization.oauth2;

import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.jwt.GivrCookie;
import com.backend.givr.shared.oauth.AuthProviderType;
import com.backend.givr.shared.service.TokenIdService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
@RequiredArgsConstructor
public class OrganizationOauthSuccessHandler implements AuthenticationSuccessHandler {
    private final OrganizationDetailsService service;
    private final GivrCookie givrCookie;
    private final String appBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        assert oidcUser != null;
        OrganizationDetails organizationDetails = service.loadUserByProvider(oidcUser.getSubject(), AuthProviderType.GOOGLE);

        if(organizationDetails.getAuthProvider() != AuthProviderType.GOOGLE){
            response.sendError(HttpServletResponse.SC_CONFLICT);
            response.sendRedirect(String.format("%s/signin/organization?error=Account was registered with username/password", appBaseUrl));
            response.getWriter().flush();
        }else{
            givrCookie.addCookieToResponse(organizationDetails, response);
            response.sendRedirect(String.format("%s/organization", appBaseUrl));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().flush();
        }
    }
}
