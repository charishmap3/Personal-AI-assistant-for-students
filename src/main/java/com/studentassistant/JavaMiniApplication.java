package com.studentassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class JavaMiniApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaMiniApplication.class, args);

        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8080/login.html"));
        } catch (Exception e) {
            System.out.println("Could not open browser: " + e.getMessage());
        }
    }
}
