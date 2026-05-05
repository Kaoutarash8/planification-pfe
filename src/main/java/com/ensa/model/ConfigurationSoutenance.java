package com.ensa.model;

import java.util.List;

public class ConfigurationSoutenance {
    private int dureeSoutenanceMinutes;
    private String heureDebutMatin;
    private String heureFinMatin;
    private String heureDebutApresMidi;
    private String heureFinApresMidi;
    private int pauseEntreSoutenancesMinutes;
    private int pauseMaxSansSoutenanceHeures;
    private List<String> salles;
    private List<String> jours;
    private String dateDebutSoutenance;
    private int nbJours;
    
    // Constructeurs
    public ConfigurationSoutenance() {
        this.dureeSoutenanceMinutes = 60;
        this.heureDebutMatin = "09:00";
        this.heureFinMatin = "12:00";
        this.heureDebutApresMidi = "14:00";
        this.heureFinApresMidi = "18:00";
        this.pauseEntreSoutenancesMinutes = 15;
        this.pauseMaxSansSoutenanceHeures = 3;
        this.nbJours = 3;
    }
    
    // Getters et Setters
    public int getDureeSoutenanceMinutes() { return dureeSoutenanceMinutes; }
    public void setDureeSoutenanceMinutes(int dureeSoutenanceMinutes) { this.dureeSoutenanceMinutes = dureeSoutenanceMinutes; }
    
    public String getHeureDebutMatin() { return heureDebutMatin; }
    public void setHeureDebutMatin(String heureDebutMatin) { this.heureDebutMatin = heureDebutMatin; }
    
    public String getHeureFinMatin() { return heureFinMatin; }
    public void setHeureFinMatin(String heureFinMatin) { this.heureFinMatin = heureFinMatin; }
    
    public String getHeureDebutApresMidi() { return heureDebutApresMidi; }
    public void setHeureDebutApresMidi(String heureDebutApresMidi) { this.heureDebutApresMidi = heureDebutApresMidi; }
    
    public String getHeureFinApresMidi() { return heureFinApresMidi; }
    public void setHeureFinApresMidi(String heureFinApresMidi) { this.heureFinApresMidi = heureFinApresMidi; }
    
    public int getPauseEntreSoutenancesMinutes() { return pauseEntreSoutenancesMinutes; }
    public void setPauseEntreSoutenancesMinutes(int pauseEntreSoutenancesMinutes) { this.pauseEntreSoutenancesMinutes = pauseEntreSoutenancesMinutes; }
    
    public int getPauseMaxSansSoutenanceHeures() { return pauseMaxSansSoutenanceHeures; }
    public void setPauseMaxSansSoutenanceHeures(int pauseMaxSansSoutenanceHeures) { this.pauseMaxSansSoutenanceHeures = pauseMaxSansSoutenanceHeures; }
    
    public List<String> getSalles() { return salles; }
    public void setSalles(List<String> salles) { this.salles = salles; }
    
    public List<String> getJours() { return jours; }
    public void setJours(List<String> jours) { this.jours = jours; }
    
    public String getDateDebutSoutenance() { return dateDebutSoutenance; }
    public void setDateDebutSoutenance(String dateDebutSoutenance) { this.dateDebutSoutenance = dateDebutSoutenance; }
    
    public int getNbJours() { return nbJours; }
    public void setNbJours(int nbJours) { this.nbJours = nbJours; }
}