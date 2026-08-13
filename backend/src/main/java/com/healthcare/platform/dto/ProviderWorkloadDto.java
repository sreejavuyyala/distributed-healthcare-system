package com.healthcare.platform.dto;

public record ProviderWorkloadDto(String providerName, String specialty, long encounterCount) {
}