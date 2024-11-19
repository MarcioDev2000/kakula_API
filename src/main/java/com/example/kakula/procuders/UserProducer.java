package com.example.kakula.procuders;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.kakula.dto.EmailDto;
import com.example.kakula.models.Usuario;



@Component
public class UserProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.queue}")
    private String queue;

    public void sendEmail(Usuario usuario) {
        EmailDto emailDto = new EmailDto();
        emailDto.setUserId(usuario.getId());
        emailDto.setEmailTo(usuario.getEmail());
        emailDto.setSubject("Bem-vindo ao Uevocola");
        emailDto.setText("Olá " + usuario.getNome() + ",\n\nObrigado por se cadastrar no Uevocola!");

        rabbitTemplate.convertAndSend(queue, emailDto);
    }

    public void sendResetPasswordEmail(String email, String recoveryLink) {
        EmailDto emailDto = new EmailDto();
        emailDto.setEmailTo(email);
        emailDto.setSubject("Recuperação de Senha");
        emailDto.setText("Olá,\n\nVocê solicitou a recuperação da sua senha. Acesse o link para redefinir sua senha: " + recoveryLink);

        rabbitTemplate.convertAndSend(queue, emailDto);
    }
    
}
