package com.example.kakula.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.kakula.config.JwtUtil;
import com.example.kakula.dto.UsuarioDto;
import com.example.kakula.dto.UsuarioResponseDto;
import com.example.kakula.models.Usuario;
import com.example.kakula.procuders.UserProducer;
import com.example.kakula.repositories.UsuarioRepository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service 
public class UsuarioService {
   @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Autowired
    private UserProducer userProducer;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SEPEService sepeService;

    public Usuario criarUsuario(UsuarioDto usuarioDto) {
        // Consultar o BI na API do SEPE para verificar se é válido
        Map<String, Object> dadosBI = sepeService.consultarBI(usuarioDto.getBi());
        
        // Verificar se a resposta da API contém dados do BI
        if (dadosBI == null || dadosBI.isEmpty()) {
            throw new IllegalArgumentException("BI inválido ou não encontrado.");
        }
    
        // Verificar se o email já está cadastrado
        if (usuarioRepository.findByEmail(usuarioDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }
    
        // Criar um novo objeto Usuario
        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setBi(usuarioDto.getBi());
        usuario.setTelefone(usuarioDto.getTelefone());
        
        // Criptografar a senha antes de salvar
        usuario.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        usuario.setRole(usuarioDto.getRole());
    
        // Salvar o usuário no banco de dados
        Usuario salvarUsuario = usuarioRepository.save(usuario);
    
        // Enviar email de confirmação ou outro tipo de notificação
        userProducer.sendEmail(salvarUsuario);
        
        return salvarUsuario;
    }
    

    @Transactional
    public List<UsuarioResponseDto> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        // Converter lista de Usuário para lista de UsuarioResponseDto
        return usuarios.stream()
                .map(UsuarioResponseDto::new)
                .toList();
    }

    public UsuarioResponseDto encontrarUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o email: " + email));
                return new UsuarioResponseDto(usuario);
    }
    
    
    @Transactional
    public UsuarioResponseDto encontrarUsuarioPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    
        // Utilizando o novo construtor para simplificar a criação do DTO
        return new UsuarioResponseDto(usuario);
    }
    

    public UsuarioResponseDto atualizarUsuarioCompleto(UUID id, UsuarioDto usuarioDto) {
        // Obter o usuário pelo ID
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    
        // Atualizar todos os campos com os dados fornecidos no UsuarioDto
        usuario.setNome(usuarioDto.getNome());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setBi(usuarioDto.getBi());
        usuario.setTelefone(usuarioDto.getTelefone());
        usuario.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        usuario.setRole(usuarioDto.getRole());
    
        // Salvar as alterações no banco de dados
        usuarioRepository.save(usuario);
    
        // Retornar o DTO atualizado
        return new UsuarioResponseDto(usuario);
    }

    public UsuarioResponseDto atualizarNomeEEmailUsuario(UUID id, UsuarioDto usuarioDto) {
        // Obter o usuário pelo ID
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        
        // Atualizar apenas o nome e o email, se não forem nulos ou em branco
        if (usuarioDto.getNome() != null && !usuarioDto.getNome().isBlank()) {
            usuario.setNome(usuarioDto.getNome());
        }
    
        if (usuarioDto.getEmail() != null && !usuarioDto.getEmail().isBlank()) {
            usuario.setEmail(usuarioDto.getEmail());
        }
    
        // Salvar as alterações no banco de dados
        usuarioRepository.save(usuario);
    
        // Retornar o DTO atualizado
        return new UsuarioResponseDto(usuario);
    }
        
    public void deletarUsuario(UUID id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional
   public void forgotPassword(String email) {
    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado com este email."));

    String resetPasswordToken = jwtUtil.gerarTokenResetSenha(email);
    usuario.setResetPasswordToken(resetPasswordToken);

    
    Date expirationDate = new Date(System.currentTimeMillis() + 60000);
    usuario.setTokenExpirationDate(expirationDate);

    usuarioRepository.save(usuario);

    String resetLink = "http://localhost:4200/reset-password?token=" + resetPasswordToken;
    userProducer.sendResetPasswordEmail(usuario.getEmail(), resetLink);
}


    public void resetPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        // Verifica se o token expirou, se necessário
        if (usuario.getTokenExpirationDate().before(new Date())) {
            throw new RuntimeException("Token expirado");
        }

        // Codifica a nova senha
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Atualiza a senha e remove o token de recuperação
        usuario.setSenha(encodedPassword);
        usuario.setResetPasswordToken(null); // Opcional: limpar o token após o uso
        usuario.setTokenExpirationDate(null); // Opcional: remover a data de expiração

        // Salva as alterações no banco de dados
        usuarioRepository.save(usuario);
    }
   
}
