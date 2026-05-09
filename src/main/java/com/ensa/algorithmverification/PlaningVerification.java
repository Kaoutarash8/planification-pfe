package com.ensa.algorithmverification;

import com.ensa.model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PlaningVerification {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private List<ScheduledDefense> soutenances;
    private ConfigurationSoutenance config;
    private List<Professeur> tousProfesseurs;
    
    public PlaningVerification(List<ScheduledDefense> soutenances, 
                                 ConfigurationSoutenance config,
                                 List<Professeur> tousProfesseurs) {
        this.soutenances = soutenances;
        this.config = config;
        this.tousProfesseurs = tousProfesseurs;
    }
    
    public VerificationResult verify() {
        VerificationResult result = new VerificationResult();
        result.setValid(true);
        
        // 1. Vérifier qu'il y a au moins 2 INFO par soutenance
        verifier2InfosParSoutenance(result);
        
        // 2. Vérifier max soutenances/professeur/jour
        verifierMaxSoutenancesParJour(result);
        
        // 3. Vérifier pas de conflits de salle
        verifierConflitsSalle(result);
        
        // 4. Vérifier les horaires dans les plages définies
        verifierHorairesDansPlages(result);
        
        // 5. Vérifier qu'un étudiant n'a qu'une seule soutenance
        verifierEtudiantUnique(result);
        
        // 6. Vérifier diversité des jurys (SOFT)
        verifierDiversiteJurys(result);
        
        // 7. Vérifier pause minimale (SOFT)
        verifierPauseMinimale(result);
        
        return result;
    }
    
    private void verifier2InfosParSoutenance(VerificationResult result) {
        int erreurs = 0;
        List<String> details = new ArrayList<>();
        
        for (ScheduledDefense defense : soutenances) {
            int infoCount = isInfo(defense.getEncadrant()) ? 1 : 0;
            for (Professeur jury : defense.getJurys()) {
                if (isInfo(jury)) infoCount++;
            }
            
            if (infoCount < 2) {
                erreurs++;
                details.add(defense.getEtudiant().getNom() + " " + defense.getEtudiant().getPrenom() + 
                    ": " + infoCount + " INFO (minimum 2 requis)");
            }
        }
        
        if (erreurs == 0) {
            result.addHardCheck("Au moins 2 INFO par soutenance", true, "Toutes les soutenances sont conformes");
        } else {
            result.addHardCheck("Au moins 2 INFO par soutenance", false, 
                erreurs + " soutenance(s) sans 2 INFO");
            result.setValid(false);
        }
        result.setSoutenancesSans2InfoDetails(details);
    }
    
    private void verifierMaxSoutenancesParJour(VerificationResult result) {
        Map<Professeur, Map<LocalDate, Integer>> profParJour = new HashMap<>();
        
        for (ScheduledDefense defense : soutenances) {
            LocalDate date = defense.getTimeSlot().getDate();
            
            profParJour.computeIfAbsent(defense.getEncadrant(), k -> new HashMap<>())
                .merge(date, 1, Integer::sum);
            
            for (Professeur jury : defense.getJurys()) {
                profParJour.computeIfAbsent(jury, k -> new HashMap<>())
                    .merge(date, 1, Integer::sum);
            }
        }
        
        int maxParJour = config.getMaxSoutenancesParJourParProf();
        int violations = 0;
        List<String> details = new ArrayList<>();
        
        for (Map.Entry<Professeur, Map<LocalDate, Integer>> entry : profParJour.entrySet()) {
            for (Map.Entry<LocalDate, Integer> jourEntry : entry.getValue().entrySet()) {
                if (jourEntry.getValue() > maxParJour) {
                    violations++;
                    details.add(entry.getKey().getNom() + " le " + 
                        DATE_FORMATTER.format(jourEntry.getKey()) + ": " + jourEntry.getValue() + " soutenances");
                }
            }
        }
        
        if (violations == 0) {
            result.addHardCheck("Max " + maxParJour + " soutenances/prof/jour", true, "Aucune violation");
        } else {
            result.addHardCheck("Max " + maxParJour + " soutenances/prof/jour", false, 
                violations + " violation(s)");
            result.setValid(false);
        }
        result.setMaxParJourDetails(details);
    }
    
    private void verifierConflitsSalle(VerificationResult result) {
        Map<String, Map<LocalDate, Set<LocalTime>>> salleParCreneau = new HashMap<>();
        int conflits = 0;
        List<String> details = new ArrayList<>();
        
        for (ScheduledDefense defense : soutenances) {
            TimeSlot slot = defense.getTimeSlot();
            String salle = slot.getSalle();
            LocalDate date = slot.getDate();
            LocalTime heure = slot.getHeureDebut();
            
            if (salleParCreneau.computeIfAbsent(salle, k -> new HashMap<>())
                .computeIfAbsent(date, k -> new HashSet<>())
                .contains(heure)) {
                conflits++;
                details.add("Salle " + salle + " le " + DATE_FORMATTER.format(date) + " à " + TIME_FORMATTER.format(heure));
            }
            salleParCreneau.get(salle).get(date).add(heure);
        }
        
        if (conflits == 0) {
            result.addHardCheck("Pas de conflits de salle", true, "Toutes les salles disponibles aux bons créneaux");
        } else {
            result.addHardCheck("Pas de conflits de salle", false, conflits + " conflit(s) détecté(s)");
            result.setValid(false);
        }
        result.setConflitsSalleDetails(details);
    }
    
    private void verifierHorairesDansPlages(VerificationResult result) {
        LocalTime morningStart = LocalTime.parse(config.getHeureDebutMatin());
        LocalTime morningEnd = LocalTime.parse(config.getHeureFinMatin());
        LocalTime afternoonStart = LocalTime.parse(config.getHeureDebutApresMidi());
        LocalTime afternoonEnd = LocalTime.parse(config.getHeureFinApresMidi());
        
        int violations = 0;
        List<String> details = new ArrayList<>();
        
        for (ScheduledDefense defense : soutenances) {
            LocalTime heure = defense.getTimeSlot().getHeureDebut();
            int duree = config.getDureeSoutenanceMinutes();
            
            boolean dansMatin = heure.isAfter(morningStart) && 
                                heure.plusMinutes(duree).isBefore(morningEnd);
            boolean dansApresMidi = heure.isAfter(afternoonStart) && 
                                    heure.plusMinutes(duree).isBefore(afternoonEnd);
            
            if (!dansMatin && !dansApresMidi) {
                violations++;
                details.add("Soutenance à " + TIME_FORMATTER.format(heure) + " hors des plages " +
                    morningStart + "-" + morningEnd + " / " + afternoonStart + "-" + afternoonEnd);
            }
        }
        
        if (violations == 0) {
            result.addHardCheck("Horaires dans les plages définies", true, "Toutes les soutenances sont dans les plages");
        } else {
            result.addHardCheck("Horaires dans les plages définies", false, violations + " soutenance(s) hors plage");
            result.setValid(false);
        }
        result.setHorsPlageDetails(details);
    }
    
    private void verifierEtudiantUnique(VerificationResult result) {
        Set<String> cnesVus = new HashSet<>();
        int doublons = 0;
        List<String> details = new ArrayList<>();
        
        for (ScheduledDefense defense : soutenances) {
            String cne = defense.getEtudiant().getCne();
            if (cnesVus.contains(cne)) {
                doublons++;
                details.add(defense.getEtudiant().getNom() + " " + defense.getEtudiant().getPrenom());
            }
            cnesVus.add(cne);
        }
        
        if (doublons == 0) {
            result.addHardCheck("Un étudiant = une seule soutenance", true, "Aucun doublon détecté");
        } else {
            result.addHardCheck("Un étudiant = une seule soutenance", false, doublons + " étudiant(s) en double");
            result.setValid(false);
        }
        result.setDoublonsEtudiantsDetails(details);
    }
    
    private void verifierDiversiteJurys(VerificationResult result) {
        Map<String, Integer> duosCount = new HashMap<>();
        
        for (ScheduledDefense defense : soutenances) {
            List<String> profs = new ArrayList<>();
            profs.add(defense.getEncadrant().getId());
            for (Professeur jury : defense.getJurys()) {
                profs.add(jury.getId());
            }
            profs.sort(String::compareTo);
            for (int i = 0; i < profs.size(); i++) {
                for (int j = i + 1; j < profs.size(); j++) {
                    String duo = profs.get(i) + "-" + profs.get(j);
                    duosCount.merge(duo, 1, Integer::sum);
                }
            }
        }
        
        List<String> details = new ArrayList<>();
        int duosFrequents = 0;
        int seuil = 5;
        
        for (Map.Entry<String, Integer> entry : duosCount.entrySet()) {
            if (entry.getValue() > seuil) {
                duosFrequents++;
                details.add("Duo " + entry.getKey() + " apparaît " + entry.getValue() + " fois");
            }
        }
        
        if (duosFrequents == 0) {
            result.addSoftCheck("Diversité des jurys (même duo max " + seuil + " fois)", true, "Bonne diversité");
        } else {
            result.addSoftCheck("Diversité des jurys (même duo max " + seuil + " fois)", false, 
                duosFrequents + " duo(s) trop fréquent(s)");
        }
        result.setDuosFrequentsDetails(details);
    }
    
    private void verifierPauseMinimale(VerificationResult result) {
        Map<Professeur, List<LocalTime>> horairesParProf = new HashMap<>();
        
        for (ScheduledDefense defense : soutenances) {
            horairesParProf.computeIfAbsent(defense.getEncadrant(), k -> new ArrayList<>())
                .add(defense.getTimeSlot().getHeureDebut());
            for (Professeur jury : defense.getJurys()) {
                horairesParProf.computeIfAbsent(jury, k -> new ArrayList<>())
                    .add(defense.getTimeSlot().getHeureDebut());
            }
        }
        
        int dureeSoutenance = config.getDureeSoutenanceMinutes();
        List<String> details = new ArrayList<>();
        int violations = 0;
        
        for (Map.Entry<Professeur, List<LocalTime>> entry : horairesParProf.entrySet()) {
            List<LocalTime> horaires = new ArrayList<>(entry.getValue());
            horaires.sort(LocalTime::compareTo);
            for (int i = 0; i < horaires.size() - 1; i++) {
                long diffMinutes = java.time.Duration.between(horaires.get(i), horaires.get(i+1)).toMinutes();
                if (diffMinutes < dureeSoutenance) {
                    violations++;
                    details.add(entry.getKey().getNom() + ": " + TIME_FORMATTER.format(horaires.get(i)) + 
                        " → " + TIME_FORMATTER.format(horaires.get(i+1)) + " (écart " + diffMinutes + " min)");
                }
            }
        }
        
        if (violations == 0) {
            result.addSoftCheck("Pause minimale entre soutenances", true, "Tous les professeurs ont une pause suffisante");
        } else {
            result.addSoftCheck("Pause minimale entre soutenances", false, 
                violations + " intervalle(s) trop court(s)");
        }
        result.setPauseInsuffisanteDetails(details);
    }
    
    private boolean isInfo(Professeur p) {
        if (p == null || p.getSpecialite() == null) return false;
        String spec = p.getSpecialite().toLowerCase();
        return spec.contains("info") || spec.contains("informatique");
    }
    
    /**
     * Classe résultat à envoyer à l'utilisateur
     */
    public static class VerificationResult {
        private boolean isValid; // false si une contrainte HARD est violée
        private List<CheckResult> hardChecks = new ArrayList<>();
        private List<CheckResult> softChecks = new ArrayList<>();
        
        // Détails pour chaque vérification
        private List<String> soutenancesSans2InfoDetails = new ArrayList<>();
        private List<String> maxParJourDetails = new ArrayList<>();
        private List<String> conflitsSalleDetails = new ArrayList<>();
        private List<String> horsPlageDetails = new ArrayList<>();
        private List<String> doublonsEtudiantsDetails = new ArrayList<>();
        private List<String> duosFrequentsDetails = new ArrayList<>();
        private List<String> pauseInsuffisanteDetails = new ArrayList<>();
        
        public void addHardCheck(String nom, boolean passe, String message) {
            hardChecks.add(new CheckResult(nom, passe, message));
        }
        
        public void addSoftCheck(String nom, boolean passe, String message) {
            softChecks.add(new CheckResult(nom, passe, message));
        }
        
        // Getters
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public List<CheckResult> getHardChecks() { return hardChecks; }
        public List<CheckResult> getSoftChecks() { return softChecks; }
        
        public List<String> getSoutenancesSans2InfoDetails() { return soutenancesSans2InfoDetails; }
        public void setSoutenancesSans2InfoDetails(List<String> details) { this.soutenancesSans2InfoDetails = details; }
        
        public List<String> getMaxParJourDetails() { return maxParJourDetails; }
        public void setMaxParJourDetails(List<String> details) { this.maxParJourDetails = details; }
        
        public List<String> getConflitsSalleDetails() { return conflitsSalleDetails; }
        public void setConflitsSalleDetails(List<String> details) { this.conflitsSalleDetails = details; }
        
        public List<String> getHorsPlageDetails() { return horsPlageDetails; }
        public void setHorsPlageDetails(List<String> details) { this.horsPlageDetails = details; }
        
        public List<String> getDoublonsEtudiantsDetails() { return doublonsEtudiantsDetails; }
        public void setDoublonsEtudiantsDetails(List<String> details) { this.doublonsEtudiantsDetails = details; }
        
        public List<String> getDuosFrequentsDetails() { return duosFrequentsDetails; }
        public void setDuosFrequentsDetails(List<String> details) { this.duosFrequentsDetails = details; }
        
        public List<String> getPauseInsuffisanteDetails() { return pauseInsuffisanteDetails; }
        public void setPauseInsuffisanteDetails(List<String> details) { this.pauseInsuffisanteDetails = details; }
    }
    
    public static class CheckResult {
        private String nom;
        private boolean passe;
        private String message;
        
        public CheckResult(String nom, boolean passe, String message) {
            this.nom = nom;
            this.passe = passe;
            this.message = message;
        }
        
        public String getNom() { return nom; }
        public boolean isPasse() { return passe; }
        public String getMessage() { return message; }
    }
}