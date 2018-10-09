package com.amido.healthchecker;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @RequestMapping("/")
    public String index() {
        return "Amido healthchecker.";
    }

}