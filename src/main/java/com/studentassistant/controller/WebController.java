// src/main/java/com/studentassistant/controller/WebController.java
package com.studentassistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/quiz/upload")
    public String redirectToUpload() {
        return "redirect:/quiz_upload.html";
    }
}
