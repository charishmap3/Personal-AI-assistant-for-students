package com.studentassistant.service;

import com.studentassistant.model.QuizQuestion;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuizGeneratorService {

    public List<QuizQuestion> generateQuiz(MultipartFile file, int questionCount) {
        System.out.println("🎯 Incoming file: " + file.getOriginalFilename());
        System.out.println("📦 Size: " + file.getSize() + " bytes");
        System.out.println("📄 Type: " + file.getContentType());

        List<QuizQuestion> quiz = new ArrayList<>();
        String text = "";

        try {
            String fileType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();
            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();

            // Determine type more robustly
            boolean isPresentation = fileType.contains("presentation") ||
                    fileType.contains("ms-powerpoint") ||
                    fileType.contains("vnd.openxmlformats-officedocument.presentationml.presentation") ||
                    fileName.endsWith(".pptx") || fileName.endsWith(".ppt");

            boolean isDocOrPdf = fileType.contains("pdf") ||
                    fileType.contains("word") ||
                    fileType.contains("officedocument") ||
                    fileType.contains("text") ||
                    fileName.endsWith(".pdf") || fileName.endsWith(".doc") ||
                    fileName.endsWith(".docx") || fileName.endsWith(".txt");

            String poiText = "";
            String tikaText = "";

            if (isPresentation) {
                poiText = extractTextFromPPTX(file);
                tikaText = extractUsingTika(file);
                text = (poiText + " " + tikaText).trim();
                System.out.println("✅ Extracted PPTX text (POI: " + poiText.length() + " chars, Tika: " + tikaText.length() + " chars)");
            } else if (isDocOrPdf) {
                text = extractUsingTika(file);
                System.out.println("✅ Extracted text via Tika: " + text.length() + " chars");
            } else {
                return fallback("Unsupported file type: " + fileType);
            }

            if (text == null || text.trim().isEmpty()) {
                return fallback("No readable text found — this file may contain only images.");
            }

            // Normalize and clean text
            text = text.replaceAll("\\s+", " ")
                       .replaceAll("[^A-Za-z0-9., ]", " ")
                       .trim();

            List<String> sentences = Arrays.stream(text.split("(?<=[.!?])\\s+"))
                    .filter(s -> s.split(" ").length > 5 && s.length() < 300)
                    .collect(Collectors.toList());

            if (sentences.size() < 3) {
                return fallback("Not enough meaningful text to create quiz questions.");
            }

            Set<String> stopwords = Set.of("the", "is", "are", "was", "were", "in", "at", "on", "to",
                    "a", "an", "and", "for", "with", "from", "by", "of", "as", "that", "this",
                    "it", "be", "or", "if", "but");

            Map<String, Integer> freq = new HashMap<>();
            for (String word : text.toLowerCase().split("\\W+")) {
                if (!stopwords.contains(word) && word.length() > 4) {
                    freq.put(word, freq.getOrDefault(word, 0) + 1);
                }
            }

            // 🔹 Get top keywords
            List<String> keywords = freq.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(25)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            Random rand = new Random();  // new random each call

            // 🔀 Shuffle keywords so same file gives different subset/order each time
            Collections.shuffle(keywords, rand);

            int total = Math.min(questionCount, Math.max(1, keywords.size()));

            for (int i = 0; i < total; i++) {
                String keyword = keywords.get(i);

                // 🔀 find ALL sentences containing this keyword, then pick one randomly
                List<String> matchingSentences = sentences.stream()
                        .filter(s -> Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE)
                                .matcher(s).find())
                        .collect(Collectors.toList());

                String base;
                if (!matchingSentences.isEmpty()) {
                    base = matchingSentences.get(rand.nextInt(matchingSentences.size()));
                } else {
                    base = "The document discusses the concept of " + keyword + ".";
                }

                int questionType = rand.nextInt(3);
                String question;
                List<String> options = new ArrayList<>();
                String explanation;

                switch (questionType) {
                    case 0 -> {
                        question = "What best defines '" + keyword + "'?";
                        options.add(base);
                        options.add("It refers to an unrelated concept.");
                        options.add("A random term not mentioned in the text.");
                        options.add("None of the above.");
                        explanation = "‘" + keyword + "’ is explained in: " + base;
                    }
                    case 1 -> {
                        question = "Which of the following is true about '" + keyword + "'?";
                        options.add(base);
                        options.add("It is not related to the discussed context.");
                        options.add("It is purely fictional.");
                        options.add("It has no significance.");
                        explanation = "The sentence '" + base + "' describes '" + keyword + "' in context.";
                    }
                    default -> {
                        question = "Why is '" + keyword + "' important in this topic?";
                        options.add("Because " + base);
                        options.add("It is not relevant.");
                        options.add("It was only mentioned randomly.");
                        options.add("None of the above.");
                        explanation = "Importance of '" + keyword + "' is described as: " + base;
                    }
                }

                // 🔀 shuffle options for randomness
                Collections.shuffle(options, rand);

                int correctIndex = 0;
                for (int j = 0; j < options.size(); j++) {
                    if (options.get(j).equals(base) || options.get(j).equals("Because " + base)) {
                        correctIndex = j;
                        break;
                    }
                }

                quiz.add(new QuizQuestion(question, options, correctIndex, explanation));
            }

            // 🔀 final shuffle of questions so even order changes
            Collections.shuffle(quiz, rand);

        } catch (Exception e) {
            e.printStackTrace();
            return fallback("Error reading file: " + e.getMessage());
        }

        System.out.println("✅ Quiz generated successfully (" + quiz.size() + " questions)");
        return quiz;
    }

    private String extractTextFromPPTX(MultipartFile file) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = file.getInputStream();
             XMLSlideShow ppt = new XMLSlideShow(is)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String slideText = textShape.getText();
                        if (slideText != null && !slideText.isBlank()) {
                            sb.append(slideText).append(". ");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ PPTX direct parsing failed: " + e.getMessage());
        }
        return sb.toString().trim();
    }

    private String extractUsingTika(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            parser.parse(is, handler, metadata, context);
            return handler.toString();
        } catch (Exception e) {
            System.err.println("⚠ Tika parsing failed: " + e.getMessage());
            return "";
        }
    }

    /**
     * When we can't generate a quiz (empty file, unsupported type, etc.),
     * just log the reason and return an EMPTY LIST.
     * The controller / frontend will detect "no questions" and show a friendly error.
     */
    private List<QuizQuestion> fallback(String msg) {
        System.out.println("⚠ Quiz generation fallback: " + msg);
        return Collections.emptyList();
    }
}
