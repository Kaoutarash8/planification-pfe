package com.ensa.model;

import java.util.*;

public class Affectation {
    private Map<Professeur, List<Etudiant>> affectations;
    private Date dateCreation;
    private int totalEtudiants;
    private int totalProfesseurs;
    
    public Affectation() {
        this.affectations = new LinkedHashMap<>();
        this.dateCreation = new Date();
    }
    
    public Map<Professeur, List<Etudiant>> getAffectations() { return affectations; }
    public void setAffectations(Map<Professeur, List<Etudiant>> affectations) { 
        this.affectations = affectations; 
    }
    
    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }
    
    public int getTotalEtudiants() { return totalEtudiants; }
    public void setTotalEtudiants(int totalEtudiants) { this.totalEtudiants = totalEtudiants; }
    
    public int getTotalProfesseurs() { return totalProfesseurs; }
    public void setTotalProfesseurs(int totalProfesseurs) { this.totalProfesseurs = totalProfesseurs; }
    
    public int getMinParProfesseur() {
        return affectations.values().stream()
            .mapToInt(List::size)
            .min()
            .orElse(0);
    }
    
    public int getMaxParProfesseur() {
        return affectations.values().stream()
            .mapToInt(List::size)
            .max()
            .orElse(0);
    }
    
    public boolean estEquilibree() {
        return (getMaxParProfesseur() - getMinParProfesseur()) <= 1;
    }
}