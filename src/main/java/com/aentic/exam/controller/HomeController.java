package com.aentic.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "index";
    }
    
    @GetMapping("/generator")
    public String generator() {
        return "generator/index";
    }
}
