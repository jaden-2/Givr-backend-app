package com.backend.givr.organization.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationDetailsRepo extends JpaRepository<OrganizationDetails, Long> {
    Optional<OrganizationDetails> findByEmail(String email);
}
