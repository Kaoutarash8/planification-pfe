package com.ensa.model;

import java.util.ArrayList;
import java.util.List;

public class ScheduleResult {
    private boolean success;
    private List<ScheduledDefense> soutenances;
    private String message;
    private int totalScore;
    private int hardConstraintsViolations;
    private int softConstraintsViolations;
    private long executionTimeMs;
    private int backtrackingSteps;
    private List<String> suggestions;
    
    public ScheduleResult() {
        this.soutenances = new ArrayList<>();
        this.success = false;
        this.hardConstraintsViolations = 0;
        this.softConstraintsViolations = 0;
        this.backtrackingSteps = 0;
    }
    
    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<String> getSuggestions() { return suggestions; }  // Ajouté
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }  // Ajouté
    public void addSuggestion(String suggestion) { this.suggestions.add(suggestion); }  // Ajouté
    
    public List<ScheduledDefense> getSoutenances() { return soutenances; }
    public void setSoutenances(List<ScheduledDefense> soutenances) { this.soutenances = soutenances; }
    public void addSoutenance(ScheduledDefense defense) { this.soutenances.add(defense); }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    
    public int getHardConstraintsViolations() { return hardConstraintsViolations; }
    public void setHardConstraintsViolations(int hardConstraintsViolations) { 
        this.hardConstraintsViolations = hardConstraintsViolations; 
    }
    
    public int getSoftConstraintsViolations() { return softConstraintsViolations; }
    public void setSoftConstraintsViolations(int softConstraintsViolations) { 
        this.softConstraintsViolations = softConstraintsViolations; 
    }
    
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    
    public int getBacktrackingSteps() { return backtrackingSteps; }
    public void setBacktrackingSteps(int backtrackingSteps) { this.backtrackingSteps = backtrackingSteps; }
    public void incrementBacktrackingSteps() { this.backtrackingSteps++; }

	
}