package com.ensa.model;

public class Etudiant {
    private String cne;
    private String nom;
    private String prenom;
    private String emailPersonnel;
    private String emailAcademique;
    private String filiere;
    private String encadrantNom;
    private String encadrantPrenom;
    
    // Constructeurs
    public Etudiant() {}
    
    public Etudiant(String nom, String prenom, String filiere) {
        this.nom = nom;
        this.prenom = prenom;
        this.filiere = filiere;
    }
    
    // Getters et Setters
    public String getCne() { return cne; }
    public void setCne(String cne) { this.cne = cne; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getEmailPersonnel() { return emailPersonnel; }
    public void setEmailPersonnel(String emailPersonnel) { this.emailPersonnel = emailPersonnel; }
    
    public String getEmailAcademique() { return emailAcademique; }
    public void setEmailAcademique(String emailAcademique) { this.emailAcademique = emailAcademique; }
    
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    
    public String getEncadrantNom() { return encadrantNom; }
    public void setEncadrantNom(String encadrantNom) { this.encadrantNom = encadrantNom; }
    
    public String getEncadrantPrenom() { return encadrantPrenom; }
    public void setEncadrantPrenom(String encadrantPrenom) { this.encadrantPrenom = encadrantPrenom; }
    
    public String getNomComplet() { 
        return prenom + " " + nom; 
    }
    
    @Override
    public String toString() {
        return getNomComplet() + " (" + filiere + ")";
    }
}