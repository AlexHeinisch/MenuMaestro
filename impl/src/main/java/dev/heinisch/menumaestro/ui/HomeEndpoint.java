package dev.heinisch.menumaestro.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeEndpoint {

    @GetMapping
    public String home() {
        return "redirect:/ui";
    }
}
