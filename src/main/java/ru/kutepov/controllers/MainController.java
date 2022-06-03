package ru.kutepov.controllers;

import lombok.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class MainController {
    @RequestMapping("/admin")
    public String index() {
        return "index";
    }

}
