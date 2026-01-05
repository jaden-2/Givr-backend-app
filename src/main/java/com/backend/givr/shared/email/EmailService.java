package com.backend.givr.shared.email;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.exceptions.FailedToSendOTPException;
import com.backend.givr.shared.otp.OTPGenerator;
import com.backend.givr.shared.otp.OTPService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${RESEND_API_TOKEN}")
    private String apiToken;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailTemplateService emailTemplateService;
    private Resend resend;

    @PostConstruct
    private void setResend(){
        this.resend = new Resend(apiToken);
    }

    public void sendOtpTo(String email, AccountType accountType, OtpPurpose purpose)  {
        String otpToken = OTPGenerator.generateOTP();
        String html = emailTemplateService.otpEmail(otpToken, OTPGenerator.DURATION);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Acme <onboarding@resend.dev>")
                .to("jedidiahamonia1@gmail.com")
                .subject("GIVR OTP Request")
                .html(html)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            otpService.generateOtp(email, otpToken, accountType, data.getId(), purpose);
        } catch (ResendException e) {
            throw new FailedToSendOTPException(e.getLocalizedMessage());
        }
    }


}
