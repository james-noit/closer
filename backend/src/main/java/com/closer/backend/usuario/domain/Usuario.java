package com.closer.backend.usuario.domain;

import com.closer.backend.persona.domain.Persona;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "usuarios", uniqueConstraints = { @UniqueConstraint(columnNames = "username") })
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(min = 4, max = 50)
  @Column(nullable = false, length = 50)
  private String username;

  @NotBlank
  @Size(min = 6, max = 100)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(nullable = false, length = 100)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private UserRole role = UserRole.ROLE_LOGGED_USER;

  @OneToOne
  @JsonIgnore
  @JoinColumn(name = "persona_id", unique = true)
  private Persona persona;

  @OneToMany(mappedBy = "usuario")
  @JsonIgnore
  private List<com.closer.backend.persona.domain.Persona> personas;

  @Column(nullable = false)
  private int failedLoginAttempts = 0;

  @Column
  private Instant lockoutUntil;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Persona getPersona() {
    return persona;
  }

  public void setPersona(Persona persona) {
    this.persona = persona;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public List<com.closer.backend.persona.domain.Persona> getPersonas() {
    return personas;
  }

  public void setPersonas(List<com.closer.backend.persona.domain.Persona> personas) {
    this.personas = personas;
  }

  public int getFailedLoginAttempts() {
    return failedLoginAttempts;
  }

  public void setFailedLoginAttempts(int failedLoginAttempts) {
    this.failedLoginAttempts = failedLoginAttempts;
  }

  public Instant getLockoutUntil() {
    return lockoutUntil;
  }

  public void setLockoutUntil(Instant lockoutUntil) {
    this.lockoutUntil = lockoutUntil;
  }
}