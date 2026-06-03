package com.boraver.teamgenerator.controller;

import com.boraver.teamgenerator.service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@AllArgsConstructor
public class EmailController {

  private final EmailService emailService;

  @PostMapping("/send")
  public String send(@RequestParam String to) {
    return emailService.send(to);
  }
}
