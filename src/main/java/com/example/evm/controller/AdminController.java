package com.example.evm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "Welcome to Admin Dashboard!";
    }
}
