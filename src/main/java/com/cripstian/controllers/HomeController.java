package com.cripstian.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping(value = "/")
    public String home() {
        return "index";
    }

    @RequestMapping(value = "/hello")
    public String hello(final Model model) {
        model.addAttribute("name", "CriPstian");
        return "hello";
    }

}
