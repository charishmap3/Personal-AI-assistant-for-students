package com.studentassistant.model;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;
    private int correctOption;
    private String explanation;

    public QuizQuestion() {}

    public QuizQuestion(String question, List<String> options, int correctOption, String explanation) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
        this.explanation = explanation;
    }

    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectOption() { return correctOption; }
    public String getExplanation() { return explanation; }

    public void setQuestion(String question) { this.question = question; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectOption(int correctOption) { this.correctOption = correctOption; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
