package com.ensa.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une soutenance planifiée
 */
public class ScheduledDefense {
    private Etudiant etudiant;
    private Professeur encadrant;
    private List<Professeur> jurys;
    private TimeSlot timeSlot;
    private int score;
    private List<String> violationsSoftConstraints;
    
    public ScheduledDefense() {
        this.jurys = new ArrayList<>();
        this.violationsSoftConstraints = new ArrayList<>();
        this.score = 0;
    }
    
    public ScheduledDefense(Etudiant etudiant, Professeur encadrant, 
                            List<Professeur> jurys, TimeSlot timeSlot) {
        this();
        this.etudiant = etudiant;
        this.encadrant = encadrant;
        this.jurys = jurys;
        this.timeSlot = timeSlot;
    }
    
    // Getters et Setters
    public Etudiant getEtudiant() { return etudiant; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }
    
    public Professeur getEncadrant() { return encadrant; }
    public void setEncadrant(Professeur encadrant) { this.encadrant = encadrant; }
    
    public List<Professeur> getJurys() { return jurys; }
    public void setJurys(List<Professeur> jurys) { this.jurys = jurys; }
    
    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public List<String> getViolationsSoftConstraints() { return violationsSoftConstraints; }
    public void addViolation(String violation) { this.violationsSoftConstraints.add(violation); }
    
    public boolean hasEncadrant(Professeur professeur) {
        return encadrant.equals(professeur);
    }
    
    public boolean hasJury(Professeur professeur) {
        return jurys.contains(professeur);
    }
    
    public boolean hasProfesseur(Professeur professeur) {
        return hasEncadrant(professeur) || hasJury(professeur);
    }
}