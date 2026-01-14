package com.studentassistant.controller;

import com.studentassistant.service.FlashcardService;
import com.studentassistant.service.FlashcardService.Flashcard;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/flashcards")
public class FlashcardController {

    @Autowired
    private FlashcardService flashcardService;

    // GET /flashcards  -> show page
    @GetMapping
    public String showPage() {
        return "flashcards";  // flashcards.html in templates
    }

    // POST /flashcards/upload  -> handle file upload
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {

        // 1️⃣ No file or empty file
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Please upload a file with some content.");
            return "flashcards";
        }

        try {
            // 2️⃣ Extract text from file (PPT / PDF / DOCX / TXT)
            String text = extractTextFromFile(file);

            if (text == null || text.trim().isEmpty()) {
                model.addAttribute("fileName", file.getOriginalFilename());
                model.addAttribute("error",
                        "Unable to read text from this file. It may be empty or contain only images.");
                return "flashcards";
            }

            // 3️⃣ Generate flashcards from text
            List<Flashcard> cards = flashcardService.generateFlashcards(text);

            if (cards == null || cards.isEmpty()) {
                model.addAttribute("fileName", file.getOriginalFilename());
                model.addAttribute("error",
                        "Not enough useful text to generate flashcards from this file.");
                return "flashcards";
            }

            // 4️⃣ Success – send cards + filename to template
            model.addAttribute("fileName", file.getOriginalFilename());
            model.addAttribute("flashcards", cards);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error while processing file: " + e.getMessage());
        }

        return "flashcards";
    }

    // --------- helpers to read text ----------

    private String extractTextFromFile(MultipartFile file) {
        String fileType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();

        boolean isPresentation = fileType.contains("presentation")
                || fileType.contains("ms-powerpoint")
                || fileType.contains("vnd.openxmlformats-officedocument.presentationml.presentation")
                || fileName.endsWith(".pptx")
                || fileName.endsWith(".ppt");

        boolean isDocOrPdf = fileType.contains("pdf")
                || fileType.contains("word")
                || fileType.contains("officedocument")
                || fileType.contains("text")
                || fileName.endsWith(".pdf")
                || fileName.endsWith(".doc")
                || fileName.endsWith(".docx")
                || fileName.endsWith(".txt");

        String text = "";

        if (isPresentation) {
            text = extractFromPptx(file);
            if (text.trim().isEmpty()) {
                text = extractWithTika(file);
            }
        } else if (isDocOrPdf) {
            text = extractWithTika(file);
        }

        return text;
    }

    private String extractFromPptx(MultipartFile file) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = file.getInputStream();
             XMLSlideShow ppt = new XMLSlideShow(is)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String txt = textShape.getText();
                        if (txt != null && !txt.isBlank()) {
                            sb.append(txt).append("\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ PPTX parsing failed: " + e.getMessage());
        }
        return sb.toString();
    }

    private String extractWithTika(MultipartFile file) {
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
}
