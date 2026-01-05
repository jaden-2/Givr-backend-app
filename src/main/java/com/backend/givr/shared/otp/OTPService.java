package com.backend.givr.shared.otp;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.exceptions.InvalidOtpException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class OTPService {
    @Autowired
    private OTPRepo repo;

    @Value("${OTP.SECRETS}")
    private String otpSecret;

    @Transactional
    public void generateOtp(String email, String token, AccountType accountType, String id, OtpPurpose purpose){
        invalidateExistingOtp(email, accountType, purpose);
        OTP otp = new OTP(email, generateHash(token),OTPGenerator.DURATION, accountType, id);
        otp.setPurpose(purpose);
        repo.save(otp);
    }

    @Transactional
    public void verifyOtp(String email, String token, AccountType accountType, OtpPurpose purpose){
        String otpHash = generateHash(token);
        OTP otp = repo.findValidOtp(email, otpHash, accountType, purpose).orElseThrow(()->new InvalidOtpException("Invalid or expired OTP"));
        otp.setIsUsed(true);
        repo.save(otp);
    }

    private void invalidateExistingOtp(String email, AccountType accountType, OtpPurpose purpose){
        repo.markAllUsed(email, accountType, purpose);
    }
    private String generateHash(String otp){
        return DigestUtils.md5DigestAsHex((otp + otpSecret).getBytes(StandardCharsets.UTF_8));
    }
}
