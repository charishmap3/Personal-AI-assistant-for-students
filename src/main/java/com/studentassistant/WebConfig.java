package com.studentassistant;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // Map URL → templateName (without .html)

        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/signup").setViewName("signup");
        registry.addViewController("/home").setViewName("home");

        // Summary page
        registry.addViewController("/summary").setViewName("summary");

        // Quiz upload page
        registry.addViewController("/quiz_upload").setViewName("quiz_upload");

        // Flashcards
        registry.addViewController("/flashcards").setViewName("flashcards");
    }
}
