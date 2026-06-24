package com.gastospessoais.api.domain.contafixa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaFixaRepository extends JpaRepository<ContaFixa, UUID> {
    List<ContaFixa> findAllByUsuarioId(UUID usuarioId);
    List<ContaFixa> findAllByUsuarioIdAndAtiva(UUID usuarioId, boolean ativa);
    Optional<ContaFixa> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}