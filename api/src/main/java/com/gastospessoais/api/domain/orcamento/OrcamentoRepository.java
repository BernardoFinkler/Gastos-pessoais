package com.gastospessoais.api.domain.orcamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {
    List<Orcamento> findAllByUsuarioIdAndMesAndAno(UUID usuarioId, int mes, int ano);
    Optional<Orcamento> findByUsuarioIdAndCategoriaIdAndMesAndAno(UUID usuarioId, UUID categoriaId, int mes, int ano);
    Optional<Orcamento> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}