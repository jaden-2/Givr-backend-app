package com.backend.givr.shared.interfaces;

import com.backend.givr.shared.oauth.AuthProviderType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public interface SecurityDetails extends UserDetails {
    String getId();
    void setAuthorities();
    AuthProviderType getProviderType();
}
