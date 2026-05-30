package com.ensa.algorithmverification;

import com.ensa.model.PlanningSoutenance;
import com.ensa.model.Professeur;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class VerificationPlanning {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private List<String> erreurs;
    private List<String> avertissements;
    
    public VerificationPlanning() {
        this.erreurs = new ArrayList<>();
        this.avertissements = new ArrayList<>();
    }
    
    public boolean verifierToutesContraintes(List<PlanningSoutenance> planning, List<Professeur> professeurs) {
        erreurs.clear();
        avertissements.clear();
        
        System.out.println("\n========================================");
        System.out.println("VERIFICATION DU PLANNING");
        System.out.println("========================================");
        
        if (planning == null || planning.isEmpty()) {
            erreurs.add("Le planning est vide");
            return false;
        }
        
        verifierConflitsSalles(planning);
        verifierConflitsProfesseurs(planning);
        verifierPauseEntreSoutenances(planning);
        verifierSpecialiteJurys(planning, professeurs);
        verifierChargeParJour(planning);
        verifierRepartitionParHeure(planning);
        
        System.out.println("\n========================================");
        System.out.println("RESUME");
        System.out.println("========================================");
        
        if (erreurs.isEmpty()) {
            System.out.println("STATUT: AUCUNE ERREUR");
            return true;
        } else {
            System.out.println("STATUT: " + erreurs.size() + " ERREUR(S)");
            afficherRapport();
            return false;
        }
    }
    
    private void verifierConflitsSalles(List<PlanningSoutenance> planning) {
        System.out.println("\n--- CONFLITS DE SALLES ---");
        Map<String, List<PlanningSoutenance>> salleParCreneau = new HashMap<>();
        
        for (PlanningSoutenance ps : planning) {
            String key = ps.getDate() + "_" + ps.getHeure() + "_" + ps.getSalle();
            salleParCreneau.computeIfAbsent(key, k -> new ArrayList<>()).add(ps);
        }
        
        int conflits = 0;
        for (Map.Entry<String, List<PlanningSoutenance>> entry : salleParCreneau.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflits++;
                erreurs.add("Conflit salle: " + entry.getKey());
            }
        }
        System.out.println("  " + (conflits == 0 ? "OK" : "ERREUR") + " - " + conflits + " conflit(s)");
    }
    
    private void verifierConflitsProfesseurs(List<PlanningSoutenance> planning) {
        System.out.println("\n--- CONFLITS PROFESSEURS ---");
        Map<String, Set<String>> profParCreneau = new HashMap<>();
        
        for (PlanningSoutenance ps : planning) {
            String key = ps.getDate() + "_" + ps.getHeure();
            for (String prof : Arrays.asList(ps.getEncadrant(), ps.getJury1(), ps.getJury2())) {
                profParCreneau.computeIfAbsent(key + "_" + prof, k -> new HashSet<>()).add(ps.getSalle());
            }
        }
        
        int conflits = 0;
        for (Map.Entry<String, Set<String>> entry : profParCreneau.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflits++;
                erreurs.add("Conflit professeur: " + entry.getKey());
            }
        }
        System.out.println("  " + (conflits == 0 ? "OK" : "ERREUR") + " - " + conflits + " conflit(s)");
    }
    
    private void verifierPauseEntreSoutenances(List<PlanningSoutenance> planning) {
        System.out.println("\n--- PAUSE ENTRE SOUTENANCES ---");
        Map<String, List<String>> planningProf = new HashMap<>();
        
        for (PlanningSoutenance ps : planning) {
            for (String prof : Arrays.asList(ps.getEncadrant(), ps.getJury1(), ps.getJury2())) {
                String key = prof + "_" + ps.getDate();
                planningProf.computeIfAbsent(key, k -> new ArrayList<>()).add(ps.getHeure());
            }
        }
        
        int violations = 0;
        for (Map.Entry<String, List<String>> entry : planningProf.entrySet()) {
            List<String> heures = entry.getValue();
            Collections.sort(heures);
            for (int i = 0; i < heures.size() - 1; i++) {
                int ecart = Math.abs(heureToMinutes(heures.get(i)) - heureToMinutes(heures.get(i + 1)));
                if (ecart < 60) {
                    violations++;
                    avertissements.add("Pause <1h pour " + entry.getKey());
                    break;
                }
            }
        }
        System.out.println("  " + (violations == 0 ? "OK" : "ATTENTION") + " - " + violations + " violation(s)");
    }
    
    private void verifierSpecialiteJurys(List<PlanningSoutenance> planning, List<Professeur> professeurs) {
        System.out.println("\n--- SPECIALITE DES JURYS ---");
        Map<String, String> specialiteParNom = new HashMap<>();
        for (Professeur p : professeurs) {
            String nomComplet = p.getPrenom() + " " + p.getNom().toUpperCase();
            specialiteParNom.put(nomComplet, p.getSpecialite());
        }
        
        int invalides = 0;
        for (PlanningSoutenance ps : planning) {
            int nbInfo = 0;
            for (String membre : Arrays.asList(ps.getEncadrant(), ps.getJury1(), ps.getJury2())) {
                String specialite = specialiteParNom.get(membre);
                if (specialite != null && (specialite.toLowerCase().contains("info") || specialite.toLowerCase().contains("informatique"))) {
                    nbInfo++;
                }
            }
            if (nbInfo < 2) {
                invalides++;
                erreurs.add("Jury sans 2 informaticiens: " + ps.getEncadrant());
            }
        }
        System.out.println("  " + (invalides == 0 ? "OK" : "ERREUR") + " - " + invalides + " jury(s) invalide(s)");
    }
    
    private void verifierChargeParJour(List<PlanningSoutenance> planning) {
        System.out.println("\n--- CHARGE PAR JOUR ---");
        Map<String, Map<String, Integer>> chargeProfParJour = new HashMap<>();
        
        for (PlanningSoutenance ps : planning) {
            for (String prof : Arrays.asList(ps.getEncadrant(), ps.getJury1(), ps.getJury2())) {
                chargeProfParJour.computeIfAbsent(prof, k -> new HashMap<>());
                chargeProfParJour.get(prof).put(ps.getDate(), 
                    chargeProfParJour.get(prof).getOrDefault(ps.getDate(), 0) + 1);
            }
        }
        
        int maxRecommandee = 4;
        int violations = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : chargeProfParJour.entrySet()) {
            for (Map.Entry<String, Integer> jourCharge : entry.getValue().entrySet()) {
                if (jourCharge.getValue() > maxRecommandee) {
                    violations++;
                    avertissements.add(entry.getKey() + " a " + jourCharge.getValue() + " soutenances le " + jourCharge.getKey());
                }
            }
        }
        System.out.println("  " + (violations == 0 ? "OK" : "ATTENTION") + " - " + violations + " violation(s)");
    }
    
    private void verifierRepartitionParHeure(List<PlanningSoutenance> planning) {
        System.out.println("\n--- REPARTITION PAR HEURE ---");
        Map<String, Integer> parHeure = new LinkedHashMap<>();
        
        for (PlanningSoutenance ps : planning) {
            parHeure.put(ps.getHeure(), parHeure.getOrDefault(ps.getHeure(), 0) + 1);
        }
        
        List<String> heuresTriees = new ArrayList<>(parHeure.keySet());
        Collections.sort(heuresTriees);
        
        int total = planning.size();
        int nbHeures = heuresTriees.size();
        int ideal = nbHeures > 0 ? total / nbHeures : 0;
        
        System.out.println("  Repartition des soutenances par heure:");
        for (String heure : heuresTriees) {
            int count = parHeure.get(heure);
            String indicateur = "";
            if (Math.abs(count - ideal) > ideal * 0.3) {
                indicateur = " [DESEQUILIBRE]";
                avertissements.add("Heure " + heure + " a " + count + " soutenances (moyenne: " + ideal + ")");
            }
            System.out.println("    " + heure + ": " + count + indicateur);
        }
    }
    
    private int heureToMinutes(String heure) {
        try {
            String[] parts = heure.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void afficherRapport() {
        if (!erreurs.isEmpty()) {
            System.out.println("\n--- ERREURS ---");
            for (String e : erreurs) {
                System.out.println("  X " + e);
            }
        }
        if (!avertissements.isEmpty()) {
            System.out.println("\n--- AVERTISSEMENTS ---");
            for (String a : avertissements) {
                System.out.println("  ! " + a);
            }
        }
    }
    
    public List<String> getErreurs() { return erreurs; }
    public List<String> getAvertissements() { return avertissements; }
    public boolean isValide() { return erreurs.isEmpty(); }
}