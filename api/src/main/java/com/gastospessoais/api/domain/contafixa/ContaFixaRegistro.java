package com.gastospessoais.api.domain.contafixa;

import com.gastospessoais.api.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "contas_fixas_registros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaFixaRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_fixa_id", nullable = false)
    private ContaFixa contaFixa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private int mes;

    @Column(nullable = false)
    private int ano;

    @Column(name = "valor_real", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorReal;

    @Column(length = 255)
    private String observacao;
}