package com.ensa.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Suivi de la charge de travail d'un professeur
 */
public class ProfessorWorkload {
    private Professeur professeur;
    private Map<LocalDate, List<ScheduledDefense>> defensesParJour;
    private int totalDefenses;
    
    public ProfessorWorkload(Professeur professeur) {
        this.professeur = professeur;
        this.defensesParJour = new HashMap<>();
        this.totalDefenses = 0;
    }
    
    public void addDefense(ScheduledDefense defense) {
        totalDefenses++;
        LocalDate date = defense.getTimeSlot().getDate();
        defensesParJour.computeIfAbsent(date, k -> new ArrayList<>()).add(defense);
    }
    
    public void removeDefense(ScheduledDefense defense) {
        totalDefenses--;
        LocalDate date = defense.getTimeSlot().getDate();
        List<ScheduledDefense> defenses = defensesParJour.get(date);
        if (defenses != null) {
            defenses.remove(defense);
            if (defenses.isEmpty()) {
                defensesParJour.remove(date);
            }
        }
    }
    
    public int getDefensesCountOnDate(LocalDate date) {
        List<ScheduledDefense> defenses = defensesParJour.get(date);
        return defenses == null ? 0 : defenses.size();
    }
    
    public boolean isAvailable(TimeSlot timeSlot) {
        LocalDate date = timeSlot.getDate();
        List<ScheduledDefense> defenses = defensesParJour.get(date);
        if (defenses == null) return true;
        
        LocalTime newStart = timeSlot.getHeureDebut();
        
        for (ScheduledDefense defense : defenses) {
            TimeSlot existing = defense.getTimeSlot();
            if (existing.getHeureDebut().equals(newStart)) {
                return false;
            }
        }
        return true;
    }
    
    public ScheduledDefense getDefenseAtTime(LocalDate date, LocalTime time) {
        List<ScheduledDefense> defenses = defensesParJour.get(date);
        if (defenses == null) return null;
        
        for (ScheduledDefense defense : defenses) {
            if (defense.getTimeSlot().getHeureDebut().equals(time)) {
                return defense;
            }
        }
        return null;
    }
    
    public List<ScheduledDefense> getDefensesByDate(LocalDate date) {
        return defensesParJour.getOrDefault(date, new ArrayList<>());
    }
    
    /**
     * Retourne la map des défenses par jour
     */
    public Map<LocalDate, List<ScheduledDefense>> getDefensesParJour() {
        return defensesParJour;
    }
    
    public int getTotalDefenses() {
        return totalDefenses;
    }
    
    public Professeur getProfesseur() {
        return professeur;
    }
    
    /**
     * Compte le nombre de soutenances consécutives pour un jour donné
     */
    public int getConsecutiveDefensesCount(LocalDate date) {
        List<ScheduledDefense> defenses = defensesParJour.get(date);
        if (defenses == null || defenses.size() < 2) return 0;
        
        defenses.sort(Comparator.comparing(d -> d.getTimeSlot().getHeureDebut()));
        int consecutive = 0;
        
        for (int i = 0; i < defenses.size() - 1; i++) {
            LocalTime currentEnd = defenses.get(i).getTimeSlot().getHeureFin();
            LocalTime nextStart = defenses.get(i + 1).getTimeSlot().getHeureDebut();
            
            if (currentEnd.equals(nextStart)) {
                consecutive++;
            }
        }
        return consecutive;
    }
    
    /**
     * Calcule la pause maximale entre deux soutenances pour un jour donné
     */
    public int getMaxPauseBetweenDefenses(LocalDate date) {
        List<ScheduledDefense> defenses = defensesParJour.get(date);
        if (defenses == null || defenses.size() < 2) return 0;
        
        defenses.sort(Comparator.comparing(d -> d.getTimeSlot().getHeureDebut()));
        int maxPause = 0;
        
        for (int i = 0; i < defenses.size() - 1; i++) {
            LocalTime currentEnd = defenses.get(i).getTimeSlot().getHeureFin();
            LocalTime nextStart = defenses.get(i + 1).getTimeSlot().getHeureDebut();
            
            int pauseMinutes = (int) java.time.Duration.between(currentEnd, nextStart).toMinutes();
            if (pauseMinutes > maxPause) {
                maxPause = pauseMinutes;
            }
        }
        return maxPause;
    }
    
    @Override
    public String toString() {
        return professeur.getNom() + ": " + totalDefenses + " soutenances";
    }
}