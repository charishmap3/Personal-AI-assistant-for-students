package com.studentassistant.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlashcardService {

    public static class Flashcard {
        private final String question;
        private final String answer;

        public Flashcard(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }

    // Generates ONLY questions
    public List<Flashcard> generateFlashcards(String text) {
    List<Flashcard> cards = new ArrayList<>();

    if (text == null || text.isBlank()) {
        return cards;
    }

    String[] lines = text.split("\\R");

    for (int i = 0; i < lines.length; i++) {
        String line = lines[i].trim();

        // If this line is a QUESTION
        if (!line.isEmpty() && line.endsWith("?")) {
            String answer = "";

            // Find the next non-empty line as ANSWER
            int j = i + 1;
            while (j < lines.length) {
                String nextLine = lines[j].trim();
                if (!nextLine.isEmpty()) {
                    answer = nextLine;
                    break;
                }
                j++;
            }

            cards.add(new Flashcard(line, answer));
        }
    }

    return cards;
    }
}
