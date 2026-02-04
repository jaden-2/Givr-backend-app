package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.OrganizationVerificationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationVerificationSessionRepo extends JpaRepository<OrganizationVerificationSession, Long> {
    Optional<OrganizationVerificationSession> findByOrganization(Organization organization);
}
