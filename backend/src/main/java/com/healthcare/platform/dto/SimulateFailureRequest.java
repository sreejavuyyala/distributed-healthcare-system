package com.healthcare.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SimulateFailureRequest(
        @NotBlank String feedName,
        @Min(1) Integer attemptsToFail
) {
}