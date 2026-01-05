package com.backend.givr.shared.otp;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@NoArgsConstructor
@Getter
@Table(indexes = {
        @Index(name = "idx_otp_lookup", columnList = "email, accountType, purpose, otpHash"),
        @Index(name = "idx_otp_expiry", columnList = "expiresAt")
})
public class OTP {
    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String otpHash;

    @Column(nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private Boolean isUsed;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false)
    private Duration duration;

    @Setter
    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    public OTP(String email, String otpHash, Duration duration, AccountType accountType, String id){
        this.otpHash = otpHash;
        this.duration = duration;
        this.email = email;
        this.accountType = accountType;
        this.id = id;
        this.isUsed = false;
    }

    @PrePersist()
    private void setTimeline(){
        this.createdAt = LocalDateTime.now(ZoneId.of("africa/lagos"));
        this.expiresAt = this.createdAt.plus(this.duration);
    }

}
