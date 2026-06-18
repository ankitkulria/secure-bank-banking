package com.training.controller;


import com.training.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final EmailService emailService;

    @GetMapping("/")
    public String index()
    {
        return "Application is UP and RUNNING";
    }

    @GetMapping("/test-mail")
    public String testMail() {

        emailService.sendEmail(
                "digitaldove40@gmail.com",
                "Spring Boot Test",
                "Email Working Successfully"
        );

        return "Mail Sent";
    }
}
