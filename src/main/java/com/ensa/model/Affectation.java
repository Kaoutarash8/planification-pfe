package com.ensa.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Affectation {
    private Date dateAffectation;
    private List<Etudiant> etudiants;
    private List<Professeur> professeurs;
    private Map<Professeur, List<Etudiant>> affectations;
    private int totalEtudiants;
    private int totalProfesseurs;
    private int minParProf;
    private int maxParProf;

    public Affectation() {}

    // Getters et Setters
    public Date getDateAffectation() { return dateAffectation; }
    public void setDateAffectation(Date dateAffectation) { this.dateAffectation = dateAffectation; }
    public List<Etudiant> getEtudiants() { return etudiants; }
    public void setEtudiants(List<Etudiant> etudiants) { this.etudiants = etudiants; }
    public List<Professeur> getProfesseurs() { return professeurs; }
    public void setProfesseurs(List<Professeur> professeurs) { this.professeurs = professeurs; }
    public Map<Professeur, List<Etudiant>> getAffectations() { return affectations; }
    public void setAffectations(Map<Professeur, List<Etudiant>> affectations) { this.affectations = affectations; }
    public int getTotalEtudiants() { return totalEtudiants; }
    public void setTotalEtudiants(int totalEtudiants) { this.totalEtudiants = totalEtudiants; }
    public int getTotalProfesseurs() { return totalProfesseurs; }
    public void setTotalProfesseurs(int totalProfesseurs) { this.totalProfesseurs = totalProfesseurs; }
    public int getMinParProf() { return minParProf; }
    public void setMinParProf(int minParProf) { this.minParProf = minParProf; }
    public int getMaxParProf() { return maxParProf; }
    public void setMaxParProf(int maxParProf) { this.maxParProf = maxParProf; }
}