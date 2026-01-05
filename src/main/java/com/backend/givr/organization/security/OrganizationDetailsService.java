package com.backend.givr.organization.security;

import com.backend.givr.volunteer.security.VolunteerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class OrganizationDetailsService implements UserDetailsService {
    @Autowired
    private OrganizationDetailsRepo repo;

    @Override
    public OrganizationDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
    }

    public void save(OrganizationDetails details){
        if(details == null)
            return;
        repo.save(details);
    }

    public void updatePassword(String newPassword, String email) {
        OrganizationDetails organizationDetails = loadUserByUsername(email);
        organizationDetails.setPassword(newPassword);
        repo.save(organizationDetails);
    }
}
