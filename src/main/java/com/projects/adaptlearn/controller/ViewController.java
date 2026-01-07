package com.projects.adaptlearn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "forward:/login.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }
    @GetMapping("/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/assessment")
    public String assessment() {
        return "forward:/assessment.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/topic-selection")
    public String topicSelection() {
        return "forward:/topic-selection.html";
    }

    @GetMapping("/chat")
    public String chat() {
        return "forward:/chat.html";
    }
}