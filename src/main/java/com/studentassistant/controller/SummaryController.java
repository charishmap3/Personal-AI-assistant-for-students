package com.studentassistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SummaryController {

    @GetMapping("/summary")
    public String showSummary() {
        return "summary"; // loads summary.html
    }
}
