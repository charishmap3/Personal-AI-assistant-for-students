package com.studentassistant.controller;

import com.studentassistant.model.User;
import com.studentassistant.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Web pages
    @GetMapping("/signup")
    public String showSignup() { return "signup"; }

    @GetMapping("/login")
    public String showLogin() { return "login"; }

    // Form signup (redirect)
    @PostMapping("/api/user/signup")
    public RedirectView signup(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password) {

        if (userRepository.findByEmail(email) != null) {
            return new RedirectView("/signup?error=Email+already+exists");
        }
        if (password.length() < 8) {
            return new RedirectView("/signup?error=Password+must+be+at+least+8+characters");
        }

        String encryptedPassword = passwordEncoder.encode(password);
        // make sure your User constructor order matches (name,email,password or email,name,password)
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(encryptedPassword);


        userRepository.save(newUser);
        return new RedirectView("/login?success=Account+created+successfully");
    }

    // Form login (redirect) — uses email as session id (since no numeric id)
    @PostMapping("/api/user/login")
    public RedirectView login(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session) {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return new RedirectView("/login?error=Account+not+found.+Please+sign+up");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            // use email as the session 'userId' because entity uses email as PK
            session.setAttribute("userId", user.getEmail());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());

            session.setAttribute("showMoodPrompt", true);
            return new RedirectView("/home?askMood=1&success=Welcome+" + user.getName());
        } else {
            return new RedirectView("/login?error=Invalid+password");
        }
    }

    @PostMapping("/user/mood")
    @ResponseBody
    public Map<String, String> setMood(@RequestParam("mood") String mood, HttpSession session) {
        if (mood == null) mood = "neutral";
        switch (mood) {
            case "happy": case "calm": case "energetic": case "sad": case "neutral":
                session.setAttribute("mood", mood);
                session.removeAttribute("showMoodPrompt");
                return Map.of("status","ok","mood",mood);
            default:
                session.setAttribute("mood","neutral");
                session.removeAttribute("showMoodPrompt");
                return Map.of("status","ok","mood","neutral");
        }
    }

    @GetMapping("/logout")
    public RedirectView logout(HttpSession session) {
        session.invalidate();
        return new RedirectView("/login?success=Logged+out+successfully");
    }

    @GetMapping({"/", "/home"})
    public String showHome() { return "home_mood"; }

    // REST JSON endpoints
    @PostMapping(path = "/api/user/add", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> addUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error","Email is required"));
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body(Map.of("error","Email already exists"));
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error","Password must be at least 8 characters"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        saved.setPassword("");
        return ResponseEntity.ok(saved);
    }

    @GetMapping(path = "/api/user/{email}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return ResponseEntity.notFound().build();
        user.setPassword("");
        return ResponseEntity.ok(user);
    }
}
