package com.gastospessoais.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}