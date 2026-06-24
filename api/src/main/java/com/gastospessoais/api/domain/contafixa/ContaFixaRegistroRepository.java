package com.gastospessoais.api.domain.contafixa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaFixaRegistroRepository extends JpaRepository<ContaFixaRegistro, UUID> {
    List<ContaFixaRegistro> findAllByContaFixaId(UUID contaFixaId);
    List<ContaFixaRegistro> findAllByUsuarioIdAndMesAndAno(UUID usuarioId, int mes, int ano);
    Optional<ContaFixaRegistro> findByContaFixaIdAndMesAndAno(UUID contaFixaId, int mes, int ano);
    Optional<ContaFixaRegistro> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}