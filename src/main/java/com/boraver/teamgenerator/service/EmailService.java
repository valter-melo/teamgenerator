package com.boraver.teamgenerator.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@AllArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  public String send(@RequestParam String to) {
    String response = null;

    try {
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setFrom("R4NDO <contato@rando.esp.br>");
      msg.setTo(to);
      msg.setSubject("Verificação de e-mail");
      msg.setText("Olá!\n\n" +
        "Clique no link abaixo para confirmar seu e‑mail e ativar sua conta:\n" +
        "https://rando.esp.br/verify?token=123456\n\n" +
        "Se você não solicitou essa verificação, ignore este e‑mail.\n\n" +
        "Atenciosamente,\nEquipe R4ANDO");

      mailSender.send(msg);
    } catch (Exception e) {
      response = e.getMessage();
    }

    return response;
  }
}
