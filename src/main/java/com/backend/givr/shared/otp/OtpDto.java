package com.backend.givr.shared.otp;

import jakarta.validation.constraints.Email;

public record OtpDto(@Email String otp) {
}
