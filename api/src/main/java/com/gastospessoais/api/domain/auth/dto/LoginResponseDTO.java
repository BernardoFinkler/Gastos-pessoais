package com.gastospessoais.api.domain.auth.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String tipo,
        String nome,
        String email
) {}