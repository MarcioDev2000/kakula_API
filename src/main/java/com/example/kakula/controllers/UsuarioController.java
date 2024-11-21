package com.example.kakula.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.example.kakula.config.JwtUtil;
import com.example.kakula.dto.ResetPasswordDto;
import com.example.kakula.dto.UsuarioDto;
import com.example.kakula.dto.UsuarioResponseDto;
import com.example.kakula.models.Usuario;
import com.example.kakula.services.UsuarioService;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController 
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService; 

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UsuarioDto usuarioRecordDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuarioRecordDto.getEmail(), usuarioRecordDto.getSenha())
            );
    
            String token = jwtUtil.gerarToken(usuarioRecordDto.getEmail());
            UsuarioResponseDto usuarioResponseDto = usuarioService.encontrarUsuarioPorEmail(usuarioRecordDto.getEmail());
    
            Map<String, Object> response = new HashMap<>();
            response.put("name", usuarioResponseDto.getNome());
            response.put("token", token);
    
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciais inválidas"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/registrar")
public ResponseEntity<?> criarUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
    try {
        Usuario usuarioCriado = usuarioService.criarUsuario(usuarioDto);
        return new ResponseEntity<>(usuarioCriado, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
        // Retornar um erro com a mensagem do problema
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
        // Captura outras exceções inesperadas
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Erro inesperado: " + e.getMessage());
    }
}


    @GetMapping("/email/{email}")
   public ResponseEntity<UsuarioResponseDto> encontrarUsuarioPorEmail(@PathVariable String email) {
    UsuarioResponseDto usuarioResponseDto = usuarioService.encontrarUsuarioPorEmail(email);
    return new ResponseEntity<>(usuarioResponseDto, HttpStatus.OK);
   }


    @GetMapping 
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
    List<UsuarioResponseDto> usuarios = usuarioService.listarUsuarios();
    return new ResponseEntity<>(usuarios, HttpStatus.OK);
   }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> encontrarUsuarioPorId(@PathVariable UUID id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String emailAutenticado = authentication.getName();
    UsuarioResponseDto usuarioResponseDtoAutenticado = usuarioService.encontrarUsuarioPorEmail(emailAutenticado);

    if (!usuarioResponseDtoAutenticado.getId().equals(id)) {
        throw new AccessDeniedException("Acesso negado. Você não tem permissão para acessar este recurso.");
    }

    UsuarioResponseDto usuarioResponseDto = usuarioService.encontrarUsuarioPorId(id);
    return ResponseEntity.ok(usuarioResponseDto);
}



@PatchMapping("/{id}")
public ResponseEntity<UsuarioResponseDto> atualizarNomeEEmail( @Valid @PathVariable UUID id, @RequestBody UsuarioDto usuarioDto) {
    UsuarioResponseDto usuarioAtualizado = usuarioService.atualizarNomeEEmailUsuario(id, usuarioDto);
    return ResponseEntity.ok(usuarioAtualizado);
}


    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> atualizarUsuarioCompleto(@Valid @PathVariable UUID id, @RequestBody UsuarioDto usuarioDto) {
        UsuarioResponseDto usuarioAtualizado = usuarioService.atualizarUsuarioCompleto(id, usuarioDto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> deletarUsuario(@PathVariable UUID id) {
        usuarioService.deletarUsuario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); 
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            usuarioService.forgotPassword(email);
            return ResponseEntity.ok("Email de redefinição de senha enviado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            usuarioService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
            return ResponseEntity.ok("Senha atualizada com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro inesperado.");
        }
    }
}