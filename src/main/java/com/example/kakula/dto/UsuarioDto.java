package com.example.kakula.dto;

import java.util.UUID;

import com.example.kakula.enums.Role;

import jakarta.validation.constraints.*;

public class UsuarioDto {
    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail deve ser válido.")
    @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres.")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, max = 50, message = "A senha deve ter entre 6 e 50 caracteres.")
    private String senha;

    @NotBlank(message = "O número do BI é obrigatório.")
    private String bi;

    @NotNull(message = "O telefone é obrigatório.")
    private String telefone;

    @NotNull(message = "O papel (role) é obrigatório.")
    private Role role; // Deve ser do tipo Role

     // Construtores, getters e setters
     public UsuarioDto() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role; 
    }

    public void setRole(Role role) {
        this.role = role; 
    }

    public void setRole(String role) {
        this.role = Role.valueOf(role); // Converte a String para o tipo Role
    }

    public String getBi() {
        return bi;
    }

    public void setBi(String bi) {
        this.bi = bi;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }


    public UUID getId() {
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }
}