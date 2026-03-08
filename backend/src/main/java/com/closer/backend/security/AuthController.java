package com.closer.backend.security;

import com.closer.backend.usuario.domain.Usuario;
import com.closer.backend.usuario.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final LoginProtectionService loginProtectionService;

    public AuthController(AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            UsuarioRepository usuarioRepository,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService,
            LoginProtectionService loginProtectionService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.loginProtectionService = loginProtectionService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();

        // Guard login endpoint with IP rate limit and per-user lockout checks.
        loginProtectionService.validateLoginAttempt(request.getUsername(), clientIp);

        // If credentials are invalid, AuthenticationManager throws and the global
        // exception handler maps it to a 401 response.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            loginProtectionService.onFailedLogin(request.getUsername(), clientIp);
            throw ex;
        }

        // Re-load user details so the JWT can include custom claims (userId).
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        loginProtectionService.onSuccessfulLogin(request.getUsername(), clientIp);

        String accessToken = jwtUtil.generateToken(userDetails);

        Usuario user = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new org.springframework.security.authentication.AuthenticationServiceException(
                        "Usuario autenticado no encontrado"));

        String refreshToken = refreshTokenService.issueToken(user);
        LOGGER.info("Login completado username={} ip={}", request.getUsername(), clientIp);
        return ResponseEntity.ok(new AuthenticationResponse(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpirationSeconds()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        Usuario user = refreshTokenService.getUserFromActiveToken(request.getRefreshToken());
        String nextRefresh = refreshTokenService.rotateAndGetNewRawToken(request.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String nextAccess = jwtUtil.generateToken(userDetails);

        LOGGER.info("Refresh completado username={} ip={}", user.getUsername(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(new AuthenticationResponse(
                nextAccess,
                nextRefresh,
                jwtUtil.getAccessTokenExpirationSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        Optional<Long> userId = refreshTokenService.revoke(request.getRefreshToken());
        if (userId.isPresent()) {
            LOGGER.info("Logout con revocación de refresh usuarioId={} ip={}", userId.get(),
                    httpRequest.getRemoteAddr());
        } else {
            LOGGER.warn("Logout recibido con refresh token no registrado ip={}", httpRequest.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }
}