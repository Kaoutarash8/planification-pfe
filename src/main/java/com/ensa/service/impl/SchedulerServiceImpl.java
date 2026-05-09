package com.ensa.service.impl;

import com.ensa.model.*;
import com.ensa.service.interfaces.SchedulerService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulerServiceImpl implements SchedulerService {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Random random = new Random();
    
    private ConfigurationSoutenance config;
    private List<TimeSlot> allTimeSlots;
    private Map<Professeur, ProfessorWorkload> workloads;
    private Map<String, RoomSchedule> roomSchedules;
    private List<ScheduledDefense> solution;
    private int totalDefenses;
    private long startTime;
    
    // Compteurs pour les statistiques
    private Map<LocalDate, Integer> defensesParJour;
    private Map<LocalDate, Map<String, Integer>> repartitionFilieresParJour;
    private Map<LocalDate, Map<String, Integer>> objectifFilieresParJour;
    private Map<Professeur, Integer> defensesParProfesseur;
    private int totalEchecsPlacement = 0;
    
    // Charges théoriques
    private double chargeTheoriqueInfo;
    private double chargeTheoriqueNonInfo;
    private int nbProfesseursInfo;
    private int nbProfesseursNonInfo;
    
    @Override
    public ScheduleResult genererPlanning(Map<Professeur, List<Etudiant>> affectation,
                                          List<Professeur> tousProfesseurs,
                                          Map<String, Etudiant> mapEtudiantsParCNE,
                                          ConfigurationSoutenance config) {
        
        this.startTime = System.currentTimeMillis();
        this.config = config;
        this.workloads = new HashMap<>();
        this.roomSchedules = new HashMap<>();
        this.solution = new ArrayList<>();
        this.totalDefenses = affectation.values().stream().mapToInt(List::size).sum();
        
        // Initialiser les statistiques
        this.defensesParJour = new HashMap<>();
        this.repartitionFilieresParJour = new HashMap<>();
        this.objectifFilieresParJour = new HashMap<>();
        this.defensesParProfesseur = new HashMap<>();
        
        System.out.println("=== PLANIFICATION DES SOUTENANCES PFE ===");
        System.out.println("Total soutenances: " + totalDefenses);
        System.out.println("Configuration:");
        System.out.println("  - Durée soutenance: " + config.getDureeSoutenanceMinutes() + " minutes");
        System.out.println("  - Max soutenances/prof/jour: " + config.getMaxSoutenancesParJourParProf());
        System.out.println("  - Salles: " + String.join(", ", config.getSalles()));
        System.out.println("  - Période: " + config.getDateDebutSoutenance() + " (" + config.getNbJours() + " jours)");
        System.out.println();
        
        // 1. Initialisation et calcul des charges théoriques
        this.nbProfesseursInfo = 0;
        this.nbProfesseursNonInfo = 0;
        
        for (Professeur p : tousProfesseurs) {
            workloads.put(p, new ProfessorWorkload(p));
            defensesParProfesseur.put(p, 0);
            if (isInfo(p)) {
                nbProfesseursInfo++;
            } else {
                nbProfesseursNonInfo++;
            }
        }
        
        // Calcul mathématique des charges théoriques
        // Chaque soutenance nécessite 2 professeurs INFO (encadrant INFO + 2 jurys INFO OU encadrant NON-INFO + 2 jurys INFO)
        // Donc 2 présences INFO par soutenance
        this.chargeTheoriqueInfo = (2.0 * totalDefenses) / nbProfesseursInfo;
        
        // Chaque soutenance nécessite 1 professeur NON-INFO (seulement quand encadrant INFO et 1 jury NON-INFO)
        // Estimation: 50% des soutenances avec encadrant INFO peuvent avoir un jury NON-INFO
        this.chargeTheoriqueNonInfo = (1.0 * totalDefenses * 0.5) / nbProfesseursNonInfo;
        
        System.out.println("=== CHARGES THÉORIQUES (calcul mathématique) ===");
        System.out.println("Nombre de professeurs INFO: " + nbProfesseursInfo);
        System.out.println("Nombre de professeurs NON-INFO: " + nbProfesseursNonInfo);
        System.out.println("Charge théorique par prof INFO: " + String.format("%.2f", chargeTheoriqueInfo) + " soutenances");
        System.out.println("  (car 2 présences INFO par soutenance × " + totalDefenses + " soutenances / " + nbProfesseursInfo + " profs)");
        System.out.println("Charge théorique par prof NON-INFO: " + String.format("%.2f", chargeTheoriqueNonInfo) + " soutenances");
        System.out.println("  (car participation comme jury dans ~50% des soutenances)");
        System.out.println();
        
        for (String salle : config.getSalles()) {
            roomSchedules.put(salle, new RoomSchedule(salle));
        }
        
        // 2. Génération des créneaux
        generateAllTimeSlots();
        System.out.println("Créneaux disponibles: " + allTimeSlots.size());
        System.out.println();
        
        // 3. Construire la répartition des étudiants par filière
        Map<String, Integer> totalParFiliere = new HashMap<>();
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            for (Etudiant e : entry.getValue()) {
                totalParFiliere.merge(e.getFiliere(), 1, Integer::sum);
            }
        }
        
        // 4. Calculer l'objectif par jour pour chaque filière (répartition équitable)
        List<LocalDate> dates = generateDates();
        for (LocalDate date : dates) {
            objectifFilieresParJour.put(date, new HashMap<>());
            for (Map.Entry<String, Integer> filiere : totalParFiliere.entrySet()) {
                int objectif = (int) Math.round((double) filiere.getValue() / config.getNbJours());
                objectifFilieresParJour.get(date).put(filiere.getKey(), objectif);
            }
        }
        
        System.out.println("Objectifs de répartition par jour:");
        for (LocalDate date : dates) {
            System.out.println("  " + DATE_FORMATTER.format(date) + ":");
            for (Map.Entry<String, Integer> entry : objectifFilieresParJour.get(date).entrySet()) {
                System.out.println("    - " + entry.getKey() + ": " + entry.getValue());
            }
        }
        System.out.println();
        
        // 5. Construction de la liste des soutenances
        List<DefenseTask> tasks = new ArrayList<>();
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            for (Etudiant e : entry.getValue()) {
                tasks.add(new DefenseTask(e, entry.getKey()));
            }
        }
        
        // 6. Trier par priorité (filières en retard + professeurs ayant le moins de charge)
        tasks.sort((t1, t2) -> {
            // 1. Priorité aux filières en retard
            int diff1 = getEcartFiliere(t1.etudiant.getFiliere());
            int diff2 = getEcartFiliere(t2.etudiant.getFiliere());
            if (diff1 != diff2) return Integer.compare(diff2, diff1);
            
            // 2. Priorité aux professeurs avec moins de charge
            double charge1 = getEcartChargeProfesseur(t1.encadrant);
            double charge2 = getEcartChargeProfesseur(t2.encadrant);
            return Double.compare(charge2, charge1);
        });
        
        // 7. Algorithme glouton avec équilibrage mathématique
        boolean success = scheduleAll(tasks);
        
        // Afficher les statistiques finales
        afficherStatistiquesFinales();
        
        ScheduleResult result = new ScheduleResult();
        result.setSuccess(success);
        result.setSoutenances(solution);
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        
        System.out.println("Planning généré: " + solution.size() + "/" + totalDefenses);
        if (!success) {
            System.out.println("Échecs de placement: " + totalEchecsPlacement);
        }
        
        return result;
    }
    
    private double getEcartChargeProfesseur(Professeur p) {
        int chargeActuelle = defensesParProfesseur.getOrDefault(p, 0);
        if (isInfo(p)) {
            return chargeTheoriqueInfo - chargeActuelle;
        } else {
            return chargeTheoriqueNonInfo - chargeActuelle;
        }
    }
    
    private int getEcartFiliere(String filiere) {
        int totalActuel = 0;
        for (Map<String, Integer> jourMap : repartitionFilieresParJour.values()) {
            totalActuel += jourMap.getOrDefault(filiere, 0);
        }
        
        int totalObjectif = 0;
        for (Map<String, Integer> jourMap : objectifFilieresParJour.values()) {
            totalObjectif += jourMap.getOrDefault(filiere, 0);
        }
        
        return totalObjectif - totalActuel;
    }
    
    private boolean isFiliereEnRetard(String filiere, LocalDate date) {
        int actuelJour = repartitionFilieresParJour.getOrDefault(date, new HashMap<>()).getOrDefault(filiere, 0);
        int objectifJour = objectifFilieresParJour.get(date).getOrDefault(filiere, 0);
        return actuelJour < objectifJour;
    }
    
    private boolean isProfesseurEnRetard(Professeur p) {
        return getEcartChargeProfesseur(p) > 0;
    }
    
    private boolean scheduleAll(List<DefenseTask> tasks) {
        for (DefenseTask task : tasks) {
            System.out.println("--- Placement: " + task.etudiant.getPrenom() + " " + task.etudiant.getNom() + 
                " (Encadrant: " + task.encadrant.getNom() + " - " + (isInfo(task.encadrant) ? "INFO" : "NON-INFO") + 
                ", Filière: " + task.etudiant.getFiliere() + ") ---");
            
            boolean placed = false;
            int tentativeNum = 0;
            
            // Trier les créneaux par priorité
            List<TimeSlot> slotsPriorises = new ArrayList<>(allTimeSlots);
            slotsPriorises.sort((s1, s2) -> {
                // Priorité aux jours où la filière est en retard
                boolean priorite1 = isFiliereEnRetard(task.etudiant.getFiliere(), s1.getDate());
                boolean priorite2 = isFiliereEnRetard(task.etudiant.getFiliere(), s2.getDate());
                if (priorite1 != priorite2) return Boolean.compare(priorite2, priorite1);
                
                // Priorité aux jours avec moins de soutenances
                int count1 = defensesParJour.getOrDefault(s1.getDate(), 0);
                int count2 = defensesParJour.getOrDefault(s2.getDate(), 0);
                return Integer.compare(count1, count2);
            });
            
            for (TimeSlot slot : slotsPriorises) {
                if (slot.isEstOccupe()) continue;
                tentativeNum++;
                
                if (tentativeNum <= 3) {
                    System.out.println("  Essai créneau " + tentativeNum + ": " + DATE_FORMATTER.format(slot.getDate()) + 
                        " " + TIME_FORMATTER.format(slot.getHeureDebut()) + " Salle: " + slot.getSalle());
                } else if (tentativeNum == 4) {
                    System.out.println("  ... (recherche en cours)");
                }
                
                // Vérifier si la filière n'est pas déjà sur-représentée
                if (!isFiliereEnRetard(task.etudiant.getFiliere(), slot.getDate()) && 
                    getEcartFiliere(task.etudiant.getFiliere()) > 0) {
                    if (tentativeNum <= 3) System.out.println("    ⚠ Filière déjà bien représentée ce jour");
                    continue;
                }
                
                // Check disponibilité encadrant
                ProfessorWorkload encWork = workloads.get(task.encadrant);
                if (!encWork.isAvailable(slot)) {
                    if (tentativeNum <= 3) System.out.println("    ✗ Encadrant indisponible");
                    continue;
                }
                
                int encDefensesJour = encWork.getDefensesCountOnDate(slot.getDate());
                if (encDefensesJour >= config.getMaxSoutenancesParJourParProf()) {
                    if (tentativeNum <= 3) System.out.println("    ✗ Encadrant a déjà " + encDefensesJour + " soutenance(s) ce jour");
                    continue;
                }
                
                // Chercher des jurys compatibles
                JurySearchResult juryResult = findCompatibleJuriesWithDetails(task.encadrant, slot);
                if (juryResult == null || juryResult.jurys == null || juryResult.jurys.size() < 2) {
                    if (tentativeNum <= 3) {
                        if (juryResult != null && juryResult.message != null) {
                            System.out.println("    ✗ " + juryResult.message);
                        } else {
                            System.out.println("    ✗ Aucun jury compatible trouvé");
                        }
                    }
                    continue;
                }
                
                if (tentativeNum <= 3) {
                    System.out.println("    ✓ Jurys trouvés: " + 
                        juryResult.jurys.stream().map(p -> p.getNom() + "(" + (isInfo(p) ? "INFO" : "NON-INFO") + ")")
                            .collect(Collectors.joining(", ")));
                }
                
                // Créer la défense
                ScheduledDefense defense = new ScheduledDefense(task.etudiant, task.encadrant, juryResult.jurys, slot);
                
                // Valider toutes les contraintes
                ValidationResult validation = validateDefenseWithDetails(defense);
                if (!validation.valid) {
                    if (tentativeNum <= 3) System.out.println("    ✗ Validation échouée: " + validation.message);
                    continue;
                }
                
                // Placer la soutenance
                applyDefense(defense);
                solution.add(defense);
                placed = true;
                
                // Mettre à jour les statistiques
                defensesParJour.merge(slot.getDate(), 1, Integer::sum);
                String filiere = task.etudiant.getFiliere();
                repartitionFilieresParJour.computeIfAbsent(slot.getDate(), k -> new HashMap<>())
                    .merge(filiere, 1, Integer::sum);
                defensesParProfesseur.merge(task.encadrant, 1, Integer::sum);
                for (Professeur jury : juryResult.jurys) {
                    defensesParProfesseur.merge(jury, 1, Integer::sum);
                }
                
                System.out.println("    ✓✓ Soutenance placée!");
                System.out.println("      - " + DATE_FORMATTER.format(slot.getDate()) + " " + 
                    TIME_FORMATTER.format(slot.getHeureDebut()) + " Salle: " + slot.getSalle());
                System.out.println("      - Jury: " + juryResult.jurys.stream().map(p -> p.getNom() + "(" + 
                    (isInfo(p) ? "INFO" : "NON-INFO") + ")").collect(Collectors.joining(", ")));
                System.out.println();
                break;
            }
            
            if (!placed) {
                System.err.println("  ✗✗ IMPOSSIBLE de placer: " + task.etudiant.getPrenom() + " " + task.etudiant.getNom());
                totalEchecsPlacement++;
            }
        }
        return true;
    }
    
    private static class JurySearchResult {
        List<Professeur> jurys;
        String message;
        int infoCount;
        int nonInfoCount;
        
        JurySearchResult(List<Professeur> jurys, String message, int infoCount, int nonInfoCount) {
            this.jurys = jurys;
            this.message = message;
            this.infoCount = infoCount;
            this.nonInfoCount = nonInfoCount;
        }
    }
    
    private JurySearchResult findCompatibleJuriesWithDetails(Professeur encadrant, TimeSlot slot) {
        List<Professeur> disponibles = new ArrayList<>();
        
        for (Map.Entry<Professeur, ProfessorWorkload> entry : workloads.entrySet()) {
            Professeur p = entry.getKey();
            if (p.equals(encadrant)) continue;
            
            ProfessorWorkload w = entry.getValue();
            if (w.isAvailable(slot) && 
                w.getDefensesCountOnDate(slot.getDate()) < config.getMaxSoutenancesParJourParProf()) {
                disponibles.add(p);
            }
        }
        
        boolean encadrantInfo = isInfo(encadrant);
        
        // Séparer INFO et NON-INFO
        List<Professeur> infoList = disponibles.stream().filter(this::isInfo).collect(Collectors.toList());
        List<Professeur> nonInfoList = disponibles.stream().filter(p -> !isInfo(p)).collect(Collectors.toList());
        
        // Trier par écart de charge (priorité à ceux qui sont en retard sur leur charge théorique)
        infoList.sort(Comparator.comparingDouble(p -> 
            defensesParProfesseur.getOrDefault(p, 0) - chargeTheoriqueInfo));
        nonInfoList.sort(Comparator.comparingDouble(p -> 
            defensesParProfesseur.getOrDefault(p, 0) - chargeTheoriqueNonInfo));
        
        List<Professeur> jurys = new ArrayList<>();
        String message = "";
        int infoCount = encadrantInfo ? 1 : 0;
        int nonInfoCount = encadrantInfo ? 0 : 1;
        
        if (encadrantInfo) {
            // Encadrant INFO: besoin d'au moins 1 INFO jury
            if (infoList.isEmpty()) {
                message = "Aucun professeur INFO disponible";
                return new JurySearchResult(null, message, infoCount, nonInfoCount);
            }
            
            // 1er jury: toujours INFO (le moins chargé)
            jurys.add(infoList.get(0));
            infoCount++;
            
            // 2ème jury: choix entre INFO et NON-INFO selon la charge théorique
            // On favorise NON-INFO si leur charge théorique n'est pas atteinte
            double chargeMoyenneNonInfoActuelle = nonInfoList.stream()
                .mapToDouble(p -> defensesParProfesseur.getOrDefault(p, 0))
                .average().orElse(chargeTheoriqueNonInfo);
            
            boolean prendreNonInfo = !nonInfoList.isEmpty() && 
                chargeMoyenneNonInfoActuelle < chargeTheoriqueNonInfo &&
                (infoList.size() == 1 || random.nextDouble() < 0.6); // 60% de chance de prendre NON-INFO
            
            if (prendreNonInfo && !nonInfoList.isEmpty()) {
                jurys.add(nonInfoList.get(0));
                nonInfoCount++;
                message = "✅ 2ème jury NON-INFO pour équilibrer la charge";
            } else if (infoList.size() >= 2) {
                jurys.add(infoList.get(1));
                infoCount++;
                message = "2ème jury INFO (pas assez de NON-INFO ou charge NON-INFO déjà élevée)";
            } else {
                message = "Pas assez de professeurs disponibles";
                return new JurySearchResult(null, message, infoCount, nonInfoCount);
            }
        } else {
            // Encadrant NON-INFO: 2 jurys INFO obligatoires
            if (infoList.size() < 2) {
                message = "Besoin de 2 INFO jurys, seulement " + infoList.size() + " disponible(s)";
                return new JurySearchResult(null, message, infoCount, nonInfoCount);
            }
            jurys.add(infoList.get(0));
            jurys.add(infoList.get(1));
            infoCount += 2;
            message = "2 jurys INFO (obligatoire)";
        }
        
        return new JurySearchResult(jurys, message, infoCount, nonInfoCount);
    }
    
    private static class ValidationResult {
        boolean valid;
        String message;
        
        ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
    
    private ValidationResult validateDefenseWithDetails(ScheduledDefense defense) {
        TimeSlot slot = defense.getTimeSlot();
        
        for (Professeur j : defense.getJurys()) {
            ProfessorWorkload workload = workloads.get(j);
            if (workload == null || !workload.isAvailable(slot)) {
                return new ValidationResult(false, "Jury " + j.getNom() + " indisponible");
            }
        }
        
        RoomSchedule roomSchedule = roomSchedules.get(slot.getSalle());
        if (roomSchedule == null || !roomSchedule.isAvailable(slot)) {
            return new ValidationResult(false, "Salle non disponible");
        }
        
        return new ValidationResult(true, "OK");
    }
    
    private void applyDefense(ScheduledDefense defense) {
        TimeSlot slot = defense.getTimeSlot();
        workloads.get(defense.getEncadrant()).addDefense(defense);
        workloads.get(defense.getJurys().get(0)).addDefense(defense);
        workloads.get(defense.getJurys().get(1)).addDefense(defense);
        roomSchedules.get(slot.getSalle()).bookSlot(slot);
        slot.setEstOccupe(true);
    }
    
    private void generateAllTimeSlots() {
        allTimeSlots = new ArrayList<>();
        List<LocalDate> dates = generateDates();
        
        LocalTime morningStart = LocalTime.parse(config.getHeureDebutMatin());
        LocalTime morningEnd = LocalTime.parse(config.getHeureFinMatin());
        LocalTime afternoonStart = LocalTime.parse(config.getHeureDebutApresMidi());
        LocalTime afternoonEnd = LocalTime.parse(config.getHeureFinApresMidi());
        
        int duration = config.getDureeSoutenanceMinutes();
        
        for (LocalDate date : dates) {
            LocalTime current = morningStart;
            while (current.plusMinutes(duration).compareTo(morningEnd) <= 0) {
                for (String salle : config.getSalles()) {
                    allTimeSlots.add(new TimeSlot(date, current, current.plusMinutes(duration), salle));
                }
                current = current.plusMinutes(duration);
            }
            
            current = afternoonStart;
            while (current.plusMinutes(duration).compareTo(afternoonEnd) <= 0) {
                for (String salle : config.getSalles()) {
                    allTimeSlots.add(new TimeSlot(date, current, current.plusMinutes(duration), salle));
                }
                current = current.plusMinutes(duration);
            }
        }
        Collections.sort(allTimeSlots);
    }
    
    private List<LocalDate> generateDates() {
        List<LocalDate> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate start = LocalDate.parse(config.getDateDebutSoutenance(), formatter);
        for (int i = 0; i < config.getNbJours(); i++) {
            dates.add(start.plusDays(i));
        }
        return dates;
    }
    
    private boolean isInfo(Professeur p) {
        if (p == null || p.getSpecialite() == null) return false;
        String spec = p.getSpecialite().toLowerCase();
        return spec.contains("info") || spec.contains("informatique");
    }
    
    private void afficherStatistiquesFinales() {
        System.out.println();
        System.out.println("=== STATISTIQUES FINALES ===");
        System.out.println();
        
        // 1. Répartition par jour
        System.out.println("1. RÉPARTITION PAR JOUR:");
        for (Map.Entry<LocalDate, Integer> entry : defensesParJour.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
            System.out.println("   " + DATE_FORMATTER.format(entry.getKey()) + ": " + entry.getValue() + " soutenance(s)");
        }
        System.out.println();
        
        // 2. Distribution des filières
        System.out.println("2. DISTRIBUTION DES FILIÈRES PAR JOUR:");
        for (LocalDate date : generateDates()) {
            System.out.println("   " + DATE_FORMATTER.format(date) + ":");
            Map<String, Integer> repartition = repartitionFilieresParJour.getOrDefault(date, new HashMap<>());
            Map<String, Integer> objectifs = objectifFilieresParJour.get(date);
            
            for (Map.Entry<String, Integer> objectif : objectifs.entrySet()) {
                int actuel = repartition.getOrDefault(objectif.getKey(), 0);
                String indicateur = actuel >= objectif.getValue() ? "✓" : "⚠";
                System.out.println("      " + indicateur + " " + objectif.getKey() + ": " + actuel + "/" + objectif.getValue());
            }
        }
        System.out.println();
        
        // 3. Charge des professeurs VS théorique
        System.out.println("3. CHARGE DES PROFESSEURS VS THÉORIQUE:");
        System.out.println("   Charge théorique INFO: " + String.format("%.2f", chargeTheoriqueInfo));
        System.out.println("   Charge théorique NON-INFO: " + String.format("%.2f", chargeTheoriqueNonInfo));
        System.out.println();
        
        List<Map.Entry<Professeur, Integer>> sortedProfs = defensesParProfesseur.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .collect(Collectors.toList());
        
        double totalEcartInfo = 0;
        double totalEcartNonInfo = 0;
        int nbInfo = 0, nbNonInfo = 0;
        
        for (Map.Entry<Professeur, Integer> entry : sortedProfs) {
            Professeur p = entry.getKey();
            String type = isInfo(p) ? "INFO" : "NON-INFO";
            int charge = entry.getValue();
            double theorique = isInfo(p) ? chargeTheoriqueInfo : chargeTheoriqueNonInfo;
            double ecart = charge - theorique;
            
            if (isInfo(p)) {
                totalEcartInfo += Math.abs(ecart);
                nbInfo++;
            } else {
                totalEcartNonInfo += Math.abs(ecart);
                nbNonInfo++;
            }
            
            String barre = "";
            if (charge > 0) {
                int etoiles = Math.min(20, (int)(charge / (theorique + 1) * 20));
                barre = " [" + "█".repeat(etoiles) + " ".repeat(20 - etoiles) + "]";
            }
            
            String ecartStr = ecart > 0 ? "+" + String.format("%.1f", ecart) : String.format("%.1f", ecart);
            System.out.println("   - " + String.format("%-20s", p.getNom()) + " (" + type + "): " + charge + 
                " (théo: " + String.format("%.1f", theorique) + ", écart: " + ecartStr + ")" + barre);
        }
        
        System.out.println();
        System.out.println("   Écart moyen INFO: " + String.format("%.2f", totalEcartInfo / nbInfo));
        System.out.println("   Écart moyen NON-INFO: " + String.format("%.2f", totalEcartNonInfo / nbNonInfo));
        
        // 4. Participation des NON-INFO
        System.out.println();
        System.out.println("4. PARTICIPATION DES NON-INFO COMME JURY:");
        Map<Professeur, Integer> participationJury = new HashMap<>();
        for (ScheduledDefense defense : solution) {
            for (Professeur jury : defense.getJurys()) {
                if (!isInfo(jury)) {
                    participationJury.merge(jury, 1, Integer::sum);
                }
            }
        }
        
        if (participationJury.isEmpty()) {
            System.out.println("   ⚠ AUCUN professeur NON-INFO n'a participé comme jury!");
        } else {
            for (Map.Entry<Professeur, Integer> entry : participationJury.entrySet()) {
                System.out.println("   - " + entry.getKey().getNom() + ": " + entry.getValue() + " fois");
            }
        }
        
        System.out.println();
        System.out.println("=== FIN DES STATISTIQUES ===");
    }
    
    @Override
    public boolean isPlanningPossible(int nombreSoutenances, int nombreProfesseurs,
                                      List<Professeur> professeursInfo, ConfigurationSoutenance config) {
        return true;
    }
    
    private static class DefenseTask {
        Etudiant etudiant;
        Professeur encadrant;
        
        DefenseTask(Etudiant e, Professeur p) {
            this.etudiant = e;
            this.encadrant = p;
        }
    }
    
    private static class RoomSchedule {
        private Map<LocalDate, Set<LocalTime>> bookings;
        
        RoomSchedule(String name) {
            this.bookings = new HashMap<>();
        }
        
        boolean isAvailable(TimeSlot slot) {
            Set<LocalTime> booked = bookings.get(slot.getDate());
            return booked == null || !booked.contains(slot.getHeureDebut());
        }
        
        void bookSlot(TimeSlot slot) {
            bookings.computeIfAbsent(slot.getDate(), k -> new HashSet<>()).add(slot.getHeureDebut());
        }
    }
}