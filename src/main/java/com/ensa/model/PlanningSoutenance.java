package com.ensa.model;

public class PlanningSoutenance {
    private String date;
    private String heure;
    private String salle;
    private String etudiantCNE;
    private String etudiantNom;
    private String etudiantPrenom;
    private String filiere;
    private String encadrant;
    private String jury1;
    private String jury2;
    private String rapporteur;
    
    public PlanningSoutenance() {}
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }
    
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    
    public String getEtudiantCNE() { return etudiantCNE; }
    public void setEtudiantCNE(String etudiantCNE) { this.etudiantCNE = etudiantCNE; }
    
    public String getEtudiantNom() { return etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }
    
    public String getEtudiantPrenom() { return etudiantPrenom; }
    public void setEtudiantPrenom(String etudiantPrenom) { this.etudiantPrenom = etudiantPrenom; }
    
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    
    public String getEncadrant() { return encadrant; }
    public void setEncadrant(String encadrant) { this.encadrant = encadrant; }
    
    public String getJury1() { return jury1; }
    public void setJury1(String jury1) { this.jury1 = jury1; }
    
    public String getJury2() { return jury2; }
    public void setJury2(String jury2) { this.jury2 = jury2; }
    
    public String getRapporteur() { return rapporteur; }
    public void setRapporteur(String rapporteur) { this.rapporteur = rapporteur; }
}