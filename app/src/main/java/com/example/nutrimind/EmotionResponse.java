package com.example.nutrimind;
import java.util.Map;
public class EmotionResponse {

    private String text;
    private String emotion;
    private double confidence;
    private String suggestion;   //for suggestion
    private Map<String, Double> probabilities;

    public String getText() {
        return text;
    }

    public String getEmotion() {
        return emotion;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

}