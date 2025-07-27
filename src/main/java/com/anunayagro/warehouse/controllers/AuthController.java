package com.anunayagro.warehouse.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String login() {
        return "login"; // This will render src/main/resources/templates/login.html
    }
}