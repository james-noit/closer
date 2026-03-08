package com.closer.backend.persona.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(name = "personas")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false)
    private String apellidos;

    @NotBlank
    @Pattern(regexp = "^[+]?[-() 0-9]{7,20}$")
    @Column(nullable = false, length = 20)
    private String numeroTelefono;

    @Past
    @Column(nullable = false)
    private LocalDate fechaCumpleanos;

    @Email
    @Column(length = 150)
    private String email;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "usuario_id")
    private com.closer.backend.usuario.domain.Usuario usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public LocalDate getFechaCumpleanos() {
        return fechaCumpleanos;
    }

    public void setFechaCumpleanos(LocalDate fechaCumpleanos) {
        this.fechaCumpleanos = fechaCumpleanos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public com.closer.backend.usuario.domain.Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(com.closer.backend.usuario.domain.Usuario usuario) {
        this.usuario = usuario;
    }
}
