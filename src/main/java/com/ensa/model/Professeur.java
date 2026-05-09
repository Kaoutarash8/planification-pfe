package com.ensa.model;

public class Professeur {
    private String id;
    private String nom;
    private String prenom;
    private String specialite;
    private int nbEtudiantsAffectes;
    
    // Compteur statique pour générer des IDs uniques
    private static int compteurId = 0;
    
    public Professeur() {
        this.id = String.valueOf(++compteurId);
        this.nbEtudiantsAffectes = 0;
    }
    
    public Professeur(String nom, String prenom, String specialite) {
        this.id = String.valueOf(++compteurId);
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.nbEtudiantsAffectes = 0;
    }
    
    public Professeur(String id, String nom, String prenom, String specialite) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.nbEtudiantsAffectes = 0;
    }
    
    // Getters et Setters
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }
    
    public String getNom() { 
        return nom; 
    }
    
    public void setNom(String nom) { 
        this.nom = nom; 
    }
    
    public String getPrenom() { 
        return prenom; 
    }
    
    public void setPrenom(String prenom) { 
        this.prenom = prenom; 
    }
    
    public String getSpecialite() { 
        return specialite; 
    }
    
    public void setSpecialite(String specialite) { 
        this.specialite = specialite; 
    }
    
    public int getNbEtudiantsAffectes() { 
        return nbEtudiantsAffectes; 
    }
    
    public void setNbEtudiantsAffectes(int nbEtudiantsAffectes) { 
        this.nbEtudiantsAffectes = nbEtudiantsAffectes; 
    }
    
    public String getNomComplet() { 
        return prenom + " " + nom; 
    }
    
    public void incrementerNbEtudiants() { 
        this.nbEtudiantsAffectes++; 
    }
    
    @Override
    public String toString() {
        return getNomComplet() + " (" + specialite + ") - " + nbEtudiantsAffectes + " étudiants";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Professeur other = (Professeur) obj;
        return id != null && id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}