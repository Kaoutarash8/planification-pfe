package com.ensa.model;

public class Professeur {
    private String nom;
    private String prenom;
    private String specialite;
    private int nbEtudiantsAffectes;
    
    public Professeur() {
        this.nbEtudiantsAffectes = 0;
    }
    
    public Professeur(String nom, String prenom, String specialite) {
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.nbEtudiantsAffectes = 0;
    }
    
    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    
    public int getNbEtudiantsAffectes() { return nbEtudiantsAffectes; }
    public void setNbEtudiantsAffectes(int nbEtudiantsAffectes) { 
        this.nbEtudiantsAffectes = nbEtudiantsAffectes; 
    }
    
    public String getNomComplet() { return prenom + " " + nom; }
    
    public void incrementerNbEtudiants() { 
        this.nbEtudiantsAffectes++; 
    }
    
    @Override
    public String toString() {
        return getNomComplet() + " (" + specialite + ") - " + nbEtudiantsAffectes + " étudiants";
    }
}