package com.gastospessoais.api.domain.lancamento;

import com.gastospessoais.api.domain.categoria.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LancamentoRepository extends JpaRepository<Lancamento, UUID> {
    List<Lancamento> findAllByUsuarioIdAndDataMonthAndDataYear(UUID usuarioId, int mes, int ano);
    Optional<Lancamento> findByIdAndUsuarioId(UUID id, UUID usuarioId);
    boolean existsByCategoriaIdAndUsuarioId(UUID categoriaId, UUID usuarioId);
}