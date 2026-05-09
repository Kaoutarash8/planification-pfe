package com.ensa.model;

import java.util.*;

public class ConfigurationSoutenance {
    private int dureeSoutenanceMinutes;
    private String heureDebutMatin;
    private String heureFinMatin;
    private String heureDebutApresMidi;
    private String heureFinApresMidi;
    private int pauseMaxSansSoutenanceHeures;
    private List<String> salles;
    private String dateDebutSoutenance;
    private int nbJours;
    private int maxSoutenancesParJourParProf;
    private int minInfoParJury;
    private int capaciteParJour; // Nombre max de soutenances par jour (calculé)
    private int capaciteTotale;   // Nombre max total de soutenances
    
    public ConfigurationSoutenance() {
        this.dureeSoutenanceMinutes = 60;
        this.heureDebutMatin = "09:00";
        this.heureFinMatin = "12:00";
        this.heureDebutApresMidi = "14:00";
        this.heureFinApresMidi = "18:00";
        this.pauseMaxSansSoutenanceHeures = 2;
        this.salles = new ArrayList<>();
        this.dateDebutSoutenance = "22/06/2026";
        this.nbJours = 0; // 0 = auto-calcul
        this.maxSoutenancesParJourParProf = 4;
        this.minInfoParJury = 2;
        this.capaciteParJour = 0;
        this.capaciteTotale = 0;
    }
    
    // Calculer le nombre de creneaux par heure (basé sur duree)
    public int getNbCreneauxParHeure() {
        int duree = getDureeSoutenanceMinutes();
        if (duree <= 0) return 1;
        return 60 / duree;
    }
    
    // Calculer le nombre d'heures par jour
    public int getNbHeuresParJour() {
        try {
            String[] debutMatinParts = heureDebutMatin.split(":");
            String[] finMatinParts = heureFinMatin.split(":");
            String[] debutApresMidiParts = heureDebutApresMidi.split(":");
            String[] finApresMidiParts = heureFinApresMidi.split(":");
            
            int debutMatin = Integer.parseInt(debutMatinParts[0]);
            int finMatin = Integer.parseInt(finMatinParts[0]);
            int debutApresMidi = Integer.parseInt(debutApresMidiParts[0]);
            int finApresMidi = Integer.parseInt(finApresMidiParts[0]);
            
            int heuresMatin = Math.max(0, finMatin - debutMatin);
            int heuresApresMidi = Math.max(0, finApresMidi - debutApresMidi);
            
            return heuresMatin + heuresApresMidi;
        } catch (Exception e) {
            return 7; // 7 heures par jour (09-12: 3h, 14-18: 4h)
        }
    }
    
    // Calculer la capacite par jour (nombre max de soutenances par jour)
    public int calculerCapaciteParJour() {
        int nbHeures = getNbHeuresParJour();
        int nbSalles = salles.size();
        int nbCreneauxParHeure = getNbCreneauxParHeure();
        return nbHeures * nbSalles * nbCreneauxParHeure;
    }
    
    // Calculer la capacite totale
    public int calculerCapaciteTotale() {
        return calculerCapaciteParJour() * getNbJours();
    }
    
    // Getters et Setters
    public int getDureeSoutenanceMinutes() { return dureeSoutenanceMinutes; }
    public void setDureeSoutenanceMinutes(int duree) { this.dureeSoutenanceMinutes = duree; }
    
    public String getHeureDebutMatin() { return heureDebutMatin; }
    public void setHeureDebutMatin(String heure) { this.heureDebutMatin = heure; }
    
    public String getHeureFinMatin() { return heureFinMatin; }
    public void setHeureFinMatin(String heure) { this.heureFinMatin = heure; }
    
    public String getHeureDebutApresMidi() { return heureDebutApresMidi; }
    public void setHeureDebutApresMidi(String heure) { this.heureDebutApresMidi = heure; }
    
    public String getHeureFinApresMidi() { return heureFinApresMidi; }
    public void setHeureFinApresMidi(String heure) { this.heureFinApresMidi = heure; }
    
    public int getPauseMaxSansSoutenanceHeures() { return pauseMaxSansSoutenanceHeures; }
    public void setPauseMaxSansSoutenanceHeures(int pause) { this.pauseMaxSansSoutenanceHeures = pause; }
    
    public List<String> getSalles() { return salles; }
    public void setSalles(List<String> salles) { this.salles = salles; }
    
    public String getDateDebutSoutenance() { return dateDebutSoutenance; }
    public void setDateDebutSoutenance(String date) { this.dateDebutSoutenance = date; }
    
    public int getNbJours() { return nbJours; }
    public void setNbJours(int nb) { this.nbJours = nb; }
    
    public int getMaxSoutenancesParJourParProf() { return maxSoutenancesParJourParProf; }
    public void setMaxSoutenancesParJourParProf(int max) { this.maxSoutenancesParJourParProf = max; }
    
    public int getMinInfoParJury() { return minInfoParJury; }
    public void setMinInfoParJury(int min) { this.minInfoParJury = min; }
    
    public int getCapaciteParJour() { 
        if (capaciteParJour == 0) capaciteParJour = calculerCapaciteParJour();
        return capaciteParJour; 
    }
    
    public int getCapaciteTotale() {
        if (capaciteTotale == 0) capaciteTotale = calculerCapaciteTotale();
        return capaciteTotale;
    }
}