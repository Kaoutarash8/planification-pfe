package com.ensa.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Représente un créneau horaire disponible pour une soutenance
 */
public class TimeSlot implements Comparable<TimeSlot> {
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String salle;
    private boolean estOccupe;
    
    public TimeSlot(LocalDate date, LocalTime heureDebut, LocalTime heureFin, String salle) {
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
        this.estOccupe = false;
    }
    
    // Getters et Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    
    public boolean isEstOccupe() { return estOccupe; }
    public void setEstOccupe(boolean estOccupe) { this.estOccupe = estOccupe; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(date, timeSlot.date) &&
               Objects.equals(heureDebut, timeSlot.heureDebut) &&
               Objects.equals(salle, timeSlot.salle);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(date, heureDebut, salle);
    }
    
    @Override
    public int compareTo(TimeSlot other) {
        int dateCompare = this.date.compareTo(other.date);
        if (dateCompare != 0) return dateCompare;
        return this.heureDebut.compareTo(other.heureDebut);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s - %s (%s)", 
            date, heureDebut, heureFin, salle);
    }
}