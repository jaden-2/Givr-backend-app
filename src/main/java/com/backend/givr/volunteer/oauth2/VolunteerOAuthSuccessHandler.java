package com.backend.givr.volunteer.oauth2;

import com.backend.givr.shared.jwt.GivrCookie;
import com.backend.givr.shared.oauth.AuthProviderType;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class VolunteerOAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final VolunteerDetailsService service;
    private final String appBaseUrl;
    private final GivrCookie givrCookie;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser authUser = (OidcUser) authentication.getPrincipal();
        assert authUser != null;

        System.out.println("Success running");
        System.out.println(appBaseUrl);
        VolunteerDetails details = service.loadUserByUsername(authUser.getEmail());
        if(details.getAuthProvider() != AuthProviderType.GOOGLE){
            response.sendError(HttpServletResponse.SC_CONFLICT);
            response.sendRedirect(String.format("%s/signin/volunteer?error=Account was registered with username/password", appBaseUrl));
            response.getWriter().flush();
        }else {
            givrCookie.addCookieToResponse(details, response);

            response.sendRedirect(String.format("%s/volunteer", appBaseUrl));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().flush();
        }

    }
}
