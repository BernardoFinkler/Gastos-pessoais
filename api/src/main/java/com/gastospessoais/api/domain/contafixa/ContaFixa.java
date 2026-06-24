package com.gastospessoais.api.domain.contafixa;

import com.gastospessoais.api.domain.categoria.Essencialidade;
import com.gastospessoais.api.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "contas_fixas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaFixa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gasto", nullable = false, length = 20)
    private TipoGasto tipoGasto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Essencialidade essencialidade;

    @Column(nullable = false)
    private boolean ativa;
}