package com.studentassistant.controller;

import com.studentassistant.model.QuizQuestion;
import com.studentassistant.service.QuizGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizGeneratorService quizService;

    @PostMapping(
            value = "/generate",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ResponseEntity<List<QuizQuestion>> generateQuiz(
            @RequestParam("file") MultipartFile file,
            @RequestParam("count") int count) {

        // empty file → just return empty list
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<QuizQuestion> quiz = quizService.generateQuiz(file, count);
        return ResponseEntity.ok(quiz);
    }
}
