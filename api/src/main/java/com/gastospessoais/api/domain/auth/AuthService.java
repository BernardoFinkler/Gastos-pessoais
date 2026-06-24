package com.gastospessoais.api.domain.auth;

import com.gastospessoais.api.domain.auth.dto.*;
import com.gastospessoais.api.domain.categoria.*;
import com.gastospessoais.api.domain.usuario.Usuario;
import com.gastospessoais.api.domain.usuario.UsuarioRepository;
import com.gastospessoais.api.infra.exception.BusinessException;
import com.gastospessoais.api.infra.exception.ResourceNotFoundException;
import com.gastospessoais.api.infra.security.JwtService;
import com.gastospessoais.api.infra.security.RefreshToken;
import com.gastospessoais.api.infra.security.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration-refresh}")
    private long expirationRefresh;

    @Transactional
    public LoginResponseDTO cadastrar(CadastroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .build();

        usuarioRepository.save(usuario);
        criarCategoriasPadrao(usuario);

        return gerarTokens(usuario);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        refreshTokenRepository.deleteAllByUsuarioId(usuario.getId());

        return gerarTokens(usuario);
    }

    @Transactional
    public LoginResponseDTO refresh(RefreshRequestDTO dto) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.refreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (refreshToken.getExpiraEm().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token expirado");
        }

        Usuario usuario = refreshToken.getUsuario();
        refreshTokenRepository.delete(refreshToken);

        return gerarTokens(usuario);
    }

    @Transactional
    public void logout(RefreshRequestDTO dto) {
        refreshTokenRepository.findByToken(dto.refreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    private LoginResponseDTO gerarTokens(Usuario usuario) {
        String accessToken = jwtService.gerarAccessToken(usuario.getId(), usuario.getEmail());
        String refreshTokenStr = jwtService.gerarRefreshToken(usuario.getId(), usuario.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(refreshTokenStr)
                .expiraEm(LocalDateTime.now().plusSeconds(expirationRefresh / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        return new LoginResponseDTO(accessToken, refreshTokenStr, "Bearer", usuario.getNome(), usuario.getEmail());
    }

    private void criarCategoriasPadrao(Usuario usuario) {
        List<Categoria> categorias = List.of(
                Categoria.builder().usuario(usuario).nome("Alimentação").tipo(TipoCategoria.DESPESA).essencialidade(Essencialidade.ESSENCIAL).padrao(true).build(),
                Categoria.builder().usuario(usuario).nome("Transporte").tipo(TipoCategoria.DESPESA).essencialidade(Essencialidade.TRANSPORTE).padrao(true).build(),
                Categoria.builder().usuario(usuario).nome("Saúde").tipo(TipoCategoria.DESPESA).essencialidade(Essencialidade.SAUDE).padrao(true).build(),
                Categoria.builder().usuario(usuario).nome("Lazer").tipo(TipoCategoria.DESPESA).essencialidade(Essencialidade.LAZER).padrao(true).build(),
                Categoria.builder().usuario(usuario).nome("Educação").tipo(TipoCategoria.DESPESA).essencialidade(Essencialidade.EDUCACAO).padrao(true).build(),
                Categoria.builder().usuario(usuario).nome("Freelance").tipo(TipoCategoria.RECEITA).essencialidade(Essencialidade.OUTROS).padrao(true).build()
        );

        categoriaRepository.saveAll(categorias);
    }
}