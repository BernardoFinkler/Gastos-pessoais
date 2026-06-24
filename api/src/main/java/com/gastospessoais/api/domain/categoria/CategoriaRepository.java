package com.gastospessoais.api.domain.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    List<Categoria> findAllByUsuarioId(UUID usuarioId);
    List<Categoria> findAllByUsuarioIdAndTipo(UUID usuarioId, TipoCategoria tipo);
    List<Categoria> findAllByUsuarioIdAndEssencialidade(UUID usuarioId, Essencialidade essencialidade);
    List<Categoria> findAllByUsuarioIdAndTipoAndEssencialidade(UUID usuarioId, TipoCategoria tipo, Essencialidade essencialidade);
    Optional<Categoria> findByIdAndUsuarioId(UUID id, UUID usuarioId);
    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);
}