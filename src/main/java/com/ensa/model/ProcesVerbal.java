package com.ensa.model;

public class ProcesVerbal {
    private String cne;
    private String nom;
    private String prenom;
    private String filiere;
    private String intituleRapport;
    private String encadrant;
    private String president;
    private String rapporteur1;
    private String rapporteur2;
    private String date;
    
    // Constructeurs
    public ProcesVerbal() {}
    
    // Getters et Setters
    public String getCne() { return cne; }
    public void setCne(String cne) { this.cne = cne; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    
    public String getIntituleRapport() { return intituleRapport; }
    public void setIntituleRapport(String intituleRapport) { this.intituleRapport = intituleRapport; }
    
    public String getEncadrant() { return encadrant; }
    public void setEncadrant(String encadrant) { this.encadrant = encadrant; }
    
    public String getPresident() { return president; }
    public void setPresident(String president) { this.president = president; }
    
    public String getRapporteur1() { return rapporteur1; }
    public void setRapporteur1(String rapporteur1) { this.rapporteur1 = rapporteur1; }
    
    public String getRapporteur2() { return rapporteur2; }
    public void setRapporteur2(String rapporteur2) { this.rapporteur2 = rapporteur2; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getNomComplet() { return prenom + " " + nom; }
}