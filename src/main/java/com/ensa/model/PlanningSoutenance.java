package com.ensa.model;

public class PlanningSoutenance {
    private String encadrant;      // Format: "Prénom NOM"
    private String jury1;          // Format: "Prénom NOM"
    private String jury2;          // Format: "Prénom NOM"
    private String date;
    private String heure;
    private String salle;
    private String etudiantComplet; // Format: "Prénom NOM"
    private String filiere;
    
    // Getters et Setters...
    public String getEncadrant() { return encadrant; }
    public void setEncadrant(String encadrant) { this.encadrant = encadrant; }
    
    public String getJury1() { return jury1; }
    public void setJury1(String jury1) { this.jury1 = jury1; }
    
    public String getJury2() { return jury2; }
    public void setJury2(String jury2) { this.jury2 = jury2; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }
    
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    
    public String getEtudiantComplet() { return etudiantComplet; }
    public void setEtudiantComplet(String etudiantComplet) { this.etudiantComplet = etudiantComplet; }
    
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
}