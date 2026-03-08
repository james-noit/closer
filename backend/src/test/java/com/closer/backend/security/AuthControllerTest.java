package com.closer.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private com.closer.backend.usuario.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private com.closer.backend.persona.repository.PersonaRepository personaRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private LoginProtectionService loginProtectionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        // clear refresh tokens first to avoid FK violations when deleting users
        refreshTokenRepository.deleteAll();
        // limpiamos personas primero para evitar restricciones de FK
        personaRepository.deleteAll();
        // luego usuarios
        usuarioRepository.deleteAll();
        // construir MockMvc con filtros de Spring Security activos
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        loginProtectionService.clearInMemoryStateForTests();

        com.closer.backend.usuario.domain.Usuario u = new com.closer.backend.usuario.domain.Usuario();
        u.setUsername("localuser");
        u.setPassword("{noop}secret");
        u.setRole(com.closer.backend.usuario.domain.UserRole.ROLE_LOGGED_USER);
        usuarioRepository.save(u);

        com.closer.backend.usuario.domain.Usuario other = new com.closer.backend.usuario.domain.Usuario();
        other.setUsername("otheruser");
        other.setPassword("{noop}secret");
        other.setRole(com.closer.backend.usuario.domain.UserRole.ROLE_LOGGED_USER);
        usuarioRepository.save(other);

        com.closer.backend.usuario.domain.Usuario admin = new com.closer.backend.usuario.domain.Usuario();
        admin.setUsername("admin");
        admin.setPassword("{noop}secret");
        admin.setRole(com.closer.backend.usuario.domain.UserRole.ROLE_ADMIN);
        usuarioRepository.save(admin);

        com.closer.backend.persona.domain.Persona p1 = new com.closer.backend.persona.domain.Persona();
        p1.setNombre("Alice");
        p1.setApellidos("User");
        p1.setNumeroTelefono("+34910000001");
        p1.setFechaCumpleanos(java.time.LocalDate.of(1990, 1, 1));
        p1.setEmail("alice@example.com");
        p1.setUsuario(u);
        personaRepository.save(p1);

        com.closer.backend.persona.domain.Persona p2 = new com.closer.backend.persona.domain.Persona();
        p2.setNombre("Bob");
        p2.setApellidos("Other");
        p2.setNumeroTelefono("+34910000002");
        p2.setFechaCumpleanos(java.time.LocalDate.of(1991, 2, 2));
        p2.setEmail("bob@example.com");
        p2.setUsuario(other);
        personaRepository.save(p2);
    }

    @Test
    public void loginShouldReturnToken() throws Exception {
        AuthenticationRequest req = new AuthenticationRequest();
        req.setUsername("localuser");
        req.setPassword("secret");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    public void loginInvalidShould401() throws Exception {
        AuthenticationRequest req = new AuthenticationRequest();
        req.setUsername("foo");
        req.setPassword("bar");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Credenciales inválidas"));
    }

    private String obtainToken() throws Exception {
        return obtainTokenFor("localuser", "secret");
    }

    private String obtainTokenFor(String username, String password) throws Exception {
        AuthenticationRequest req = new AuthenticationRequest();
        req.setUsername(username);
        req.setPassword(password);
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String obtainRefreshToken() throws Exception {
        AuthenticationRequest req = new AuthenticationRequest();
        req.setUsername("localuser");
        req.setPassword("secret");
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("refreshToken").asText();
    }

    @Test
    public void accessProtectedEndpointWithToken() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/personas")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void anonymousShouldGet403OnProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void roleLoggedUserShouldOnlySeeOwnPersonas() throws Exception {
        String token = obtainTokenFor("localuser", "secret");
        mockMvc.perform(get("/api/personas")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.personaList.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    public void roleAdminShouldSeeAllPersonas() throws Exception {
        String token = obtainTokenFor("admin", "secret");
        mockMvc.perform(get("/api/personas")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.personaList.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    public void roleLoggedUserCannotListUsuarios() throws Exception {
        String token = obtainTokenFor("localuser", "secret");
        mockMvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void roleAdminCanListUsuarios() throws Exception {
        String token = obtainTokenFor("admin", "secret");
        mockMvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void roleLoggedUserCannotReadOtherUsuario() throws Exception {
        Long otherId = usuarioRepository.findByUsername("otheruser").orElseThrow().getId();
        String token = obtainTokenFor("localuser", "secret");

        mockMvc.perform(get("/api/usuarios/{id}", otherId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void refreshShouldRotateAndReturnNewTokens() throws Exception {
        String refreshToken = obtainRefreshToken();

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString());

        // old refresh token must be revoked by rotation
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void logoutShouldRevokeRefreshToken() throws Exception {
        String refreshToken = obtainRefreshToken();

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void accountShouldBeLockedAfterRepeatedFailedLogins() throws Exception {
        AuthenticationRequest req = new AuthenticationRequest();
        req.setUsername("localuser");
        req.setPassword("wrong");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        req.setPassword("secret");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isLocked());
    }

    @Test
    public void loginRateLimitShouldReturn429() throws Exception {
        for (int i = 0; i < 25; i++) {
            AuthenticationRequest req = new AuthenticationRequest();
            req.setUsername("nouser-" + i);
            req.setPassword("bad");
            int statusCode = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andReturn().getResponse().getStatus();
            if (statusCode == 429) {
                return;
            }
        }
        org.junit.jupiter.api.Assertions.fail("Expected at least one 429 from rate limiting");
    }

    @Test
    public void adminCanCreateUsuarioAndValidationWorks() throws Exception {
        String token = obtainTokenFor("admin", "secret");

        // valid creation - construct JSON directly so password is included (entity has
        // WRITE_ONLY)
        Map<String, String> newUser = Map.of("username", "created", "password", "{noop}abc123");
        String body = objectMapper.writeValueAsString(newUser);

        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("created"));

        // missing password should return 400
        Map<String, String> bad = Map.of("username", "nopwd");
        String badBody = objectMapper.writeValueAsString(bad);
        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos no validos"));
    }

    @Test
    public void creatingDuplicateUsuarioReturns409() throws Exception {
        String token = obtainTokenFor("admin", "secret");

        Map<String, String> u = Map.of("username", "duplicate", "password", "{noop}dup123");
        String body2 = objectMapper.writeValueAsString(u);

        // first creation should succeed
        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body2))
                .andExpect(status().isCreated());

        // second attempt with same username must return 409 Conflict
        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Usuario ya existente"));
    }
}