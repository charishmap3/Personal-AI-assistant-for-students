package com.studentassistant.controller;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/summary")
public class FileAIController {

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> summarizeFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        String text = "";

        try {
            String fileType = Optional.ofNullable(file.getContentType()).orElse("unknown");
            System.out.println("📄 File Type Detected: " + fileType);

            // ✅ Extract text depending on file type
            if (fileType.contains("presentation") ||
                fileType.contains("ms-powerpoint") ||
                fileType.contains("vnd.openxmlformats-officedocument.presentationml.presentation")) {

                text = extractTextFromPPTX(file);
                if (text.trim().isEmpty()) {
                    text = extractUsingTika(file);
                }
                System.out.println("✅ Extracted PPTX text length: " + text.length());

            } else if (fileType.contains("pdf") ||
                       fileType.contains("word") ||
                       fileType.contains("officedocument") ||
                       fileType.contains("text")) {

                text = extractUsingTika(file);
                System.out.println("✅ Extracted text length: " + text.length());
            } else {
                response.put("summary", "⚠ Unsupported file type: " + fileType);
                return ResponseEntity.ok(response);
            }

            if (text.isEmpty()) {
                response.put("summary", "⚠ No readable text found — file may contain only images.");
                return ResponseEntity.ok(response);
            }

            String summary = generateSummary(text);
            response.put("summary", summary);

            System.out.println("✅ Summary generated successfully (" + summary.length() + " chars)");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("summary", "⚠ Error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ✅ Extract text from PPTX (slides + notes)
    private String extractTextFromPPTX(MultipartFile file) {
        StringBuilder sb = new StringBuilder();

        try (InputStream is = file.getInputStream();
             XMLSlideShow ppt = new XMLSlideShow(is)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                sb.append("\nSlide ").append(slide.getSlideNumber()).append(": ");

                // ✅ Extract visible text
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String txt = textShape.getText();
                        if (txt != null && !txt.isBlank()) {
                            sb.append(txt).append(" ");
                        }
                    }
                }

                // ✅ Extract from slide notes (works in POI 5.2.x)
                XSLFNotes notes = slide.getNotes();
                if (notes != null) {
                    for (XSLFShape nShape : notes.getShapes()) {
                        if (nShape instanceof XSLFTextShape noteShape) {
                            String noteText = noteShape.getText();
                            if (noteText != null && !noteText.isBlank()) {
                                sb.append(" Notes: ").append(noteText).append(" ");
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠ PPTX extraction failed: " + e.getMessage());
        }

        return sb.toString().trim();
    }

    // ✅ Apache Tika fallback for PDFs / Word files
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

    // ✅ Simple summary generator (frequency-based)
    private String generateSummary(String text) {
        text = text.replaceAll("\\s+", " ");
        String[] sentences = text.split("(?<=[.!?])\\s+");

        Map<String, Integer> freq = new HashMap<>();
        for (String word : text.toLowerCase().split("\\W+")) {
            if (word.length() > 4)
                freq.put(word, freq.getOrDefault(word, 0) + 1);
        }

        List<String> topSentences = Arrays.stream(sentences)
                .sorted((a, b) -> Integer.compare(scoreSentence(b, freq), scoreSentence(a, freq)))
                .limit(Math.min(5, sentences.length))
                .toList();

        return String.join(" ", topSentences);
    }

    private int scoreSentence(String sentence, Map<String, Integer> freq) {
        int score = 0;
        for (String word : sentence.toLowerCase().split("\\W+")) {
            score += freq.getOrDefault(word, 0);
        }
        return score;
    }
}
