package com.project.back_end.mvc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

public class DashboardController {
    @Autowired
    Service service;

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String, String> res = service.validateToken(token, "admin").getBody();

        if (res.isEmpty()) {
            return "admin/adminDashboard";
        }
        return "redirect:http://localhost:8080";

    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        Map<String, String> res = service.validateToken(token, "doctor").getBody();

        if (res.isEmpty()) {
            return "doctor/doctorDashboard";
        }

        return "redirect:http://localhost:8080";

    }
}
