package com.example.kakula.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.kakula.services.UsuarioDetailsService;


@SuppressWarnings("deprecation")
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ConfiguracaoSeguranca {
    

    private final UsuarioDetailsService usuarioDetailsService; 
    private final JwtAuthenticationFilter jwtAutenticacaoFiltro; 
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public ConfiguracaoSeguranca(UsuarioDetailsService usuarioDetailsService, JwtAuthenticationFilter jwtAutenticacaoFiltro, 
     JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtAutenticacaoFiltro = jwtAutenticacaoFiltro;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain configuracaoFiltroSeguranca(HttpSecurity http) throws Exception { 
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/usuarios/login", 
            "/usuarios/registrar",
            "/usuarios/forgot-password",
            "/usuarios/reset-password").permitAll()
            .requestMatchers(HttpMethod.GET, "/usuarios/{id}").authenticated()
            .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/usuarios/email/{email}").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/usuarios/{id}").authenticated()
            .requestMatchers(HttpMethod.PUT, "/usuarios/{id}").authenticated()

              // Adicionando rotas para Livros
              .requestMatchers("/livros",  
            "/livros/{id}",
            "/livros").permitAll()
              
            ).exceptionHandling(ex -> ex
            .authenticationEntryPoint(jwtAuthenticationEntryPoint) 
            .accessDeniedHandler(customAccessDeniedHandler) 
        );

        // Adiciona o filtro de autenticação JWT
        http.addFilterBefore(jwtAutenticacaoFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorSenha() { 
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager gerenciadorAutenticacao(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder gerenciadorAutenticacaoBuilder = 
                http.getSharedObject(AuthenticationManagerBuilder.class);

        gerenciadorAutenticacaoBuilder.userDetailsService(usuarioDetailsService).passwordEncoder(codificadorSenha());

        return gerenciadorAutenticacaoBuilder.build();
    }
}