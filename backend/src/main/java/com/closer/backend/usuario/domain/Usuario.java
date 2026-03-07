package com.closer.backend.usuario.domain;

import com.closer.backend.persona.domain.Persona;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
  @Column(nullable = false, length = 100)
  private String password;

  @OneToOne
  @JoinColumn(name = "persona_id", unique = true)
  private Persona persona;

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
}