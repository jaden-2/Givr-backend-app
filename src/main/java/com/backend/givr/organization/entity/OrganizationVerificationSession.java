package com.backend.givr.organization.entity;

import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.organization.service.verify.Address;
import com.backend.givr.shared.entity.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(uniqueConstraints = @UniqueConstraint(name = "org_reg_unq", columnNames = {"organization_id", "claimed_cac_reg_number"}))
public class OrganizationVerificationSession {
    private static final Duration expirationDuration = Duration.ofDays(7);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sessionId;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "claimed_cac_reg_number")
    private String claimedCACRegNumber;
    private String claimedOrgName;

    @Setter
    private Location claimedLocation;
    private Address claimedAddress;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
    @PrePersist
    private void setExpiresAt(){
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plus(expirationDuration);
    }

    public OrganizationVerificationSession(Organization organization, OrganizationUpdateDto updateDto){
        this.organization = organization;
        this.claimedAddress = new Address(updateDto.getAddress(), updateDto.getLocation().getLga(), updateDto.getLocation().getState());
        this.claimedOrgName = updateDto.getName();
        this.claimedCACRegNumber = updateDto.getCacRegNumber();
    }
}
