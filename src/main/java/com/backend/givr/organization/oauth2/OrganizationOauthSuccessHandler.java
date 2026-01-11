package com.backend.givr.organization.oauth2;

import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.jwt.JwtUtil;
import com.backend.givr.shared.oauth.AuthProvider;
import com.backend.givr.shared.service.TokenIdService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
@RequiredArgsConstructor
public class OrganizationOauthSuccessHandler implements AuthenticationSuccessHandler {
    private final OrganizationDetailsService service;
    private final JwtUtil jwtUtil;
    private final TokenIdService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        assert oidcUser != null;
        OrganizationDetails organizationDetails = service.loadUserByProvider(oidcUser.getSubject(), AuthProvider.GOOGLE);

        String accessId = JwtUtil.generateJti();
        String accessToken = jwtUtil.generateToken(organizationDetails, accessId, JwtUtil.ACCESSEXPIRATION.toMillis());
        String refreshId = JwtUtil.generateJti();
        String refreshToken= jwtUtil.generateToken(organizationDetails, refreshId, JwtUtil.REFRESHEXPIRATION.toMillis());

        String email = (organizationDetails.getUsername());

        tokenService.createToken(refreshId, email, JwtUtil.REFRESHEXPIRATION.toMillis());

        ResponseCookie accessCookie = ResponseCookie.from("AccessToken").value(accessToken)
                .path("/")
                .maxAge(JwtUtil.ACCESSEXPIRATION)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .httpOnly(true)
                .secure(true)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("RefreshToken").value(refreshToken)
                .path("/")
                .maxAge(JwtUtil.REFRESHEXPIRATION)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .httpOnly(true)
                .secure(true)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        response.setStatus(200);
        response.getWriter().flush();
    }
}
