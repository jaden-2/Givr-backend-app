package com.backend.givr.shared.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    private final SpringTemplateEngine engine;

    public String otpEmail(String otp, Duration duration){
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("appName", "Givr");
        context.setVariable("expiryMinutes", duration.toMinutes());

        return engine.process("email/otp", context);
    }


}
