package com.ensa.service.impl;

import com.ensa.model.*;
import com.ensa.service.interfaces.PlanningService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PlanningServiceImpl implements PlanningService {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Niveaux de relaxation pour la pause (en minutes)
    private static final int[] NIVEAUX_PAUSE = {120, 60}; // 2h puis 1h (jamais 0)
    
    // Contraintes HARD
    private int minInfoParJury = 2;
    
    // Contraintes SOFT
    private int maxSoutenancesParJourParProf = 4;
    
    @Override
    public List<PlanningSoutenance> genererPlanning(
            Map<Professeur, List<Etudiant>> affectation,
            List<Professeur> tousProfesseurs,
            Map<String, Etudiant> mapEtudiantsParCNE,
            ConfigurationSoutenance config) throws Exception {
        
        List<PlanningSoutenance> planning = new ArrayList<>();
        
        System.out.println("\n========================================");
        System.out.println("GENERATION DU PLANNING AVEC RELAXATION");
        System.out.println("========================================");
        
        // Lire la pause depuis la configuration
        int pauseConfigHeures = config.getPauseMaxSansSoutenanceHeures();
        int pauseStrict = pauseConfigHeures * 60; // en minutes
        int pauseRelaxed = Math.max(60, pauseStrict / 2); // minimum 60 min
        
        // Construire les niveaux de pause (strict d'abord, puis relaxe)
        List<Integer> niveauxPause = new ArrayList<>();
        niveauxPause.add(pauseStrict);
        if (pauseRelaxed != pauseStrict && pauseRelaxed >= 60) {
            niveauxPause.add(pauseRelaxed);
        }
        
        System.out.println("Configuration:");
        System.out.println("  Pause stricte: " + pauseStrict + " minutes");
        System.out.println("  Pause relaxee: " + (niveauxPause.size() > 1 ? niveauxPause.get(1) + " minutes" : "aucune"));
        System.out.println("  Max par jour par prof: " + config.getMaxSoutenancesParJourParProf());
        System.out.println("  Min informaticiens par jury: " + config.getMinInfoParJury());
        
        this.maxSoutenancesParJourParProf = config.getMaxSoutenancesParJourParProf();
        this.minInfoParJury = config.getMinInfoParJury();
        
        if (affectation == null || affectation.isEmpty()) {
            System.err.println("ERREUR: L'affectation est vide!");
            return planning;
        }
        
        // Separer les professeurs INFO et NON-INFO
        List<Professeur> professeursInfo = new ArrayList<>();
        List<Professeur> professeursNonInfo = new ArrayList<>();
        
        for (Professeur p : tousProfesseurs) {
            if (estProfesseurInfo(p)) {
                professeursInfo.add(p);
            } else {
                professeursNonInfo.add(p);
            }
        }
        
        System.out.println("Professeurs INFO: " + professeursInfo.size());
        System.out.println("Professeurs NON-INFO: " + professeursNonInfo.size());
        
        // GENERER TOUS LES CRENEAUX
        List<Creneau> tousLesCreneaux = genererTousLesCreneaux(config);
        System.out.println("Total creneaux disponibles: " + tousLesCreneaux.size());
        
        int totalEtudiants = affectation.values().stream().mapToInt(List::size).sum();
        System.out.println("Total etudiants a planifier: " + totalEtudiants);
        
        // Statistiques des placements par niveau
        Map<Integer, List<String>> placesParNiveau = new HashMap<>();
        for (int niveau : niveauxPause) {
            placesParNiveau.put(niveau, new ArrayList<>());
        }
        
        List<PlanningSoutenance> planningFinal = new ArrayList<>();
        Map<String, Boolean> sallesOccupees = new HashMap<>();
        
        // Traiter chaque encadrant et ses etudiants
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            Professeur encadrant = entry.getKey();
            List<Etudiant> etudiants = entry.getValue();
            
            for (Etudiant etudiant : etudiants) {
                boolean placee = false;
                int niveauUtilise = -1;
                PlanningSoutenance ps = null;
                
                // Essayer chaque niveau de relaxation
                for (int pauseMinutes : niveauxPause) {
                    if (placee) break;
                    
                    // Reinitialiser les structures pour cet essai
                    Map<Professeur, ChargeSuivi> chargesTemp = reinitialiserCharges(planningFinal, tousProfesseurs);
                    Map<String, Boolean> sallesTemp = new HashMap<>(sallesOccupees);
                    
                    // Essayer de placer l'etudiant avec ce niveau de pause
                    ps = essayerPlacerEtudiant(
                        encadrant, etudiant, tousLesCreneaux, tousProfesseurs,
                        professeursInfo, professeursNonInfo,
                        chargesTemp, sallesTemp, pauseMinutes
                    );
                    
                    if (ps != null) {
                        placee = true;
                        niveauUtilise = pauseMinutes;
                        placesParNiveau.get(pauseMinutes).add(etudiant.getPrenom() + " " + etudiant.getNom());
                        break;
                    }
                }
                
                if (placee && ps != null) {
                    planningFinal.add(ps);
                    // Mettre a jour les salles occupees
                    String salleKey = ps.getDate() + "_" + ps.getHeure() + "_" + ps.getSalle();
                    sallesOccupees.put(salleKey, true);
                } else {
                    System.err.println("Impossible de placer (meme apres relaxation): " + 
                                       etudiant.getPrenom() + " " + etudiant.getNom());
                }
            }
        }
        
        // Afficher les statistiques de relaxation
        System.out.println("\n=== STATISTIQUES DE RELAXATION ===");
        for (int niveau : niveauxPause) {
            int count = placesParNiveau.get(niveau).size();
            String niveauStr = (niveau == niveauxPause.get(0)) ? "STRICT (" + niveau + "min)" : "RELAXE (" + niveau + "min)";
            System.out.println("  " + niveauStr + ": " + count + " etudiants");
        }
        
        // Afficher les resultats finaux
        System.out.println("\n========================================");
        System.out.println("RESULTATS FINAUX");
        System.out.println("========================================");
        System.out.println("Soutenances placees: " + planningFinal.size() + "/" + totalEtudiants);
        
        // Afficher les statistiques d'equilibrage
        afficherStatistiques(planningFinal, config);
        
        // Trier par date, heure, salle
        planningFinal.sort(Comparator.comparing(PlanningSoutenance::getDate)
                .thenComparing(PlanningSoutenance::getHeure)
                .thenComparing(PlanningSoutenance::getSalle));
        
        System.out.println("\nPlanning genere: " + planningFinal.size() + " soutenances");
        
        return planningFinal;
    }
    
    private PlanningSoutenance essayerPlacerEtudiant(
            Professeur encadrant,
            Etudiant etudiant,
            List<Creneau> creneaux,
            List<Professeur> tousProfesseurs,
            List<Professeur> professeursInfo,
            List<Professeur> professeursNonInfo,
            Map<Professeur, ChargeSuivi> charges,
            Map<String, Boolean> sallesOccupees,
            int pauseMinutes) {
        
        // Melanger les creneaux pour une meilleure distribution
        List<Creneau> creneauxMelanges = new ArrayList<>(creneaux);
        Collections.shuffle(creneauxMelanges);
        
        ChargeSuivi chargeEncadrant = charges.get(encadrant);
        
        for (Creneau creneau : creneauxMelanges) {
            // H2: Verifier que la salle n'est pas deja occupee a ce creneau
            String salleKey = creneau.date.format(DATE_FORMATTER) + "_" + 
                              creneau.heure.format(TIME_FORMATTER) + "_" + 
                              creneau.salle;
            if (sallesOccupees.containsKey(salleKey)) {
                continue;
            }
            
            // H3: Disponibilite de l'encadrant
            if (!chargeEncadrant.estDisponible(creneau.date, creneau.heure)) {
                continue;
            }
            
            // S1: Pause minimale (relaxable)
            if (!chargeEncadrant.peutAjouterSoutenance(creneau.date, creneau.heure, pauseMinutes)) {
                continue;
            }
            
            // S2: Limite par jour
            if (chargeEncadrant.getChargeJour(creneau.date) >= maxSoutenancesParJourParProf) {
                continue;
            }
            
            // Selectionner les jurys
            List<Professeur> jurys = selectionnerJurys(
                encadrant, tousProfesseurs, professeursInfo, professeursNonInfo,
                charges, creneau.date, creneau.heure, pauseMinutes
            );
            
            if (jurys.size() < 2) continue;
            
            // H4: Minimum d'informaticiens
            if (!verifierNbInformaticiens(encadrant, jurys.get(0), jurys.get(1))) {
                continue;
            }
            
            // Verifier disponibilite des jurys
            boolean jurysDisponibles = true;
            for (Professeur jury : jurys) {
                ChargeSuivi chargeJury = charges.get(jury);
                if (!chargeJury.estDisponible(creneau.date, creneau.heure)) {
                    jurysDisponibles = false;
                    break;
                }
                if (!chargeJury.peutAjouterSoutenance(creneau.date, creneau.heure, pauseMinutes)) {
                    jurysDisponibles = false;
                    break;
                }
            }
            if (!jurysDisponibles) continue;
            
            // Creer la soutenance
            PlanningSoutenance ps = new PlanningSoutenance();
            ps.setEncadrant(formatNom(encadrant));
            ps.setJury1(formatNom(jurys.get(0)));
            ps.setJury2(formatNom(jurys.get(1)));
            ps.setDate(creneau.date.format(DATE_FORMATTER));
            ps.setHeure(creneau.heure.format(TIME_FORMATTER));
            ps.setSalle(creneau.salle);
            ps.setEtudiantComplet(etudiant.getPrenom() + " " + etudiant.getNom().toUpperCase());
            ps.setFiliere(etudiant.getFiliere());
            
            // Mettre a jour les charges
            chargeEncadrant.ajouterSoutenance(creneau.date, creneau.heure);
            charges.get(jurys.get(0)).ajouterSoutenance(creneau.date, creneau.heure);
            charges.get(jurys.get(1)).ajouterSoutenance(creneau.date, creneau.heure);
            
            return ps;
        }
        
        return null;
    }
    
    private Map<Professeur, ChargeSuivi> reinitialiserCharges(
            List<PlanningSoutenance> planning,
            List<Professeur> tousProfesseurs) {
        
        Map<Professeur, ChargeSuivi> charges = new HashMap<>();
        for (Professeur p : tousProfesseurs) {
            charges.put(p, new ChargeSuivi(p));
        }
        
        for (PlanningSoutenance ps : planning) {
            // Trouver l'encadrant
            for (Professeur p : tousProfesseurs) {
                String nomComplet = p.getPrenom() + " " + p.getNom().toUpperCase();
                if (nomComplet.equals(ps.getEncadrant())) {
                    try {
                        LocalDate date = LocalDate.parse(ps.getDate(), DATE_FORMATTER);
                        LocalTime heure = LocalTime.parse(ps.getHeure(), TIME_FORMATTER);
                        charges.get(p).ajouterSoutenance(date, heure);
                    } catch (Exception e) {}
                    break;
                }
            }
            
            // Trouver les jurys
            for (Professeur p : tousProfesseurs) {
                String nomComplet = p.getPrenom() + " " + p.getNom().toUpperCase();
                if (nomComplet.equals(ps.getJury1()) || nomComplet.equals(ps.getJury2())) {
                    try {
                        LocalDate date = LocalDate.parse(ps.getDate(), DATE_FORMATTER);
                        LocalTime heure = LocalTime.parse(ps.getHeure(), TIME_FORMATTER);
                        charges.get(p).ajouterSoutenance(date, heure);
                    } catch (Exception e) {}
                }
            }
        }
        
        return charges;
    }
    
    private List<Creneau> genererTousLesCreneaux(ConfigurationSoutenance config) {
        List<Creneau> creneaux = new ArrayList<>();
        
        List<LocalDate> dates = genererDates(config.getDateDebutSoutenance(), config.getNbJours());
        List<LocalTime> heures = genererHeures(config);
        List<String> salles = config.getSalles();
        
        System.out.println("\n=== GENERATION DES CRENEAUX ===");
        System.out.println("Dates: " + dates.size());
        System.out.println("Heures par jour: " + heures.size());
        System.out.println("Salles: " + salles.size());
        
        for (LocalDate date : dates) {
            for (LocalTime heure : heures) {
                for (String salle : salles) {
                    creneaux.add(new Creneau(date, heure, salle));
                }
            }
        }
        
        System.out.println("Total creneaux generes: " + creneaux.size());
        System.out.println("Creneaux par jour: " + (heures.size() * salles.size()));
        
        return creneaux;
    }
    
    private List<LocalDate> genererDates(String dateDebut, int nbJours) {
        List<LocalDate> dates = new ArrayList<>();
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate start = LocalDate.parse(dateDebut, inputFormatter);
            for (int i = 0; i < nbJours; i++) {
                dates.add(start.plusDays(i));
            }
        } catch (Exception e) {
            LocalDate start = LocalDate.of(2026, 6, 22);
            for (int i = 0; i < nbJours; i++) {
                dates.add(start.plusDays(i));
            }
        }
        return dates;
    }
    
    private List<LocalTime> genererHeures(ConfigurationSoutenance config) {
        List<LocalTime> heures = new ArrayList<>();
        int duree = config.getDureeSoutenanceMinutes();
        
        try {
            LocalTime debutMatin = LocalTime.parse(config.getHeureDebutMatin());
            LocalTime finMatin = LocalTime.parse(config.getHeureFinMatin());
            
            if (debutMatin.isBefore(finMatin)) {
                LocalTime current = debutMatin;
                while (current.plusMinutes(duree).compareTo(finMatin) <= 0) {
                    heures.add(current);
                    current = current.plusMinutes(duree);
                }
            }
            
            LocalTime debutApresMidi = LocalTime.parse(config.getHeureDebutApresMidi());
            LocalTime finApresMidi = LocalTime.parse(config.getHeureFinApresMidi());
            
            if (debutApresMidi.isBefore(finApresMidi)) {
                LocalTime current = debutApresMidi;
                while (current.plusMinutes(duree).compareTo(finApresMidi) <= 0) {
                    heures.add(current);
                    current = current.plusMinutes(duree);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur parsing heure: " + e.getMessage());
            heures.add(LocalTime.of(9, 0));
            heures.add(LocalTime.of(10, 0));
            heures.add(LocalTime.of(11, 0));
            heures.add(LocalTime.of(14, 0));
            heures.add(LocalTime.of(15, 0));
            heures.add(LocalTime.of(16, 0));
            heures.add(LocalTime.of(17, 0));
        }
        
        Collections.sort(heures);
        
        System.out.println("\n=== GENERATION DES HEURES ===");
        System.out.println("Duree soutenance: " + duree + " minutes");
        for (LocalTime h : heures) {
            System.out.println("  Heure valide: " + h.format(TIME_FORMATTER));
        }
        System.out.println("Total heures par jour: " + heures.size());
        
        return heures;
    }
    
    private void afficherStatistiques(List<PlanningSoutenance> planning, ConfigurationSoutenance config) {
        System.out.println("\n=== STATISTIQUES FINALES ===");
        
        // Par jour
        Map<String, Integer> parJour = new LinkedHashMap<>();
        for (PlanningSoutenance ps : planning) {
            parJour.put(ps.getDate(), parJour.getOrDefault(ps.getDate(), 0) + 1);
        }
        System.out.println("\nSoutenances par jour:");
        for (Map.Entry<String, Integer> entry : parJour.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        
        // Par heure
        Map<String, Integer> parHeure = new LinkedHashMap<>();
        for (PlanningSoutenance ps : planning) {
            parHeure.put(ps.getHeure(), parHeure.getOrDefault(ps.getHeure(), 0) + 1);
        }
        System.out.println("\nSoutenances par heure:");
        List<String> heuresTriees = new ArrayList<>(parHeure.keySet());
        Collections.sort(heuresTriees);
        for (String heure : heuresTriees) {
            System.out.println("  " + heure + ": " + parHeure.get(heure));
        }
        
        // Par salle
        Map<String, Integer> parSalle = new LinkedHashMap<>();
        for (PlanningSoutenance ps : planning) {
            parSalle.put(ps.getSalle(), parSalle.getOrDefault(ps.getSalle(), 0) + 1);
        }
        System.out.println("\nSoutenances par salle:");
        for (String salle : config.getSalles()) {
            System.out.println("  " + salle + ": " + parSalle.getOrDefault(salle, 0));
        }
        
        // Verification des conflits
        Set<String> sallesParCreneau = new HashSet<>();
        int conflitsSalle = 0;
        for (PlanningSoutenance ps : planning) {
            String key = ps.getDate() + "_" + ps.getHeure() + "_" + ps.getSalle();
            if (sallesParCreneau.contains(key)) {
                conflitsSalle++;
            }
            sallesParCreneau.add(key);
        }
        System.out.println("\nVerification conflits salles: " + (conflitsSalle == 0 ? "OK" : conflitsSalle + " conflits"));
        
        // Taux de remplissage
        int totalCreneaux = config.getNbJours() * 7 * config.getSalles().size();
        double taux = (double) planning.size() / totalCreneaux * 100;
        System.out.println("\nTaux de remplissage: " + String.format("%.1f", taux) + "%");
    }
    
    private String formatNom(Professeur p) {
        return p.getPrenom() + " " + p.getNom().toUpperCase();
    }
    
    private boolean estProfesseurInfo(Professeur p) {
        if (p == null || p.getSpecialite() == null) return false;
        String specialite = p.getSpecialite().toLowerCase();
        return specialite.contains("info") || specialite.contains("informatique");
    }
    
    private boolean verifierNbInformaticiens(Professeur encadrant, Professeur jury1, Professeur jury2) {
        int nbInfo = (estProfesseurInfo(encadrant) ? 1 : 0) +
                     (estProfesseurInfo(jury1) ? 1 : 0) +
                     (estProfesseurInfo(jury2) ? 1 : 0);
        return nbInfo >= minInfoParJury;
    }
    
    private List<Professeur> selectionnerJurys(Professeur encadrant,
                                                List<Professeur> tousProfesseurs,
                                                List<Professeur> professeursInfo,
                                                List<Professeur> professeursNonInfo,
                                                Map<Professeur, ChargeSuivi> charges,
                                                LocalDate date, LocalTime heure,
                                                int pauseMinutes) {
        
        List<Professeur> disponibles = new ArrayList<>();
        
        for (Professeur p : tousProfesseurs) {
            if (p.equals(encadrant)) continue;
            
            ChargeSuivi charge = charges.get(p);
            if (charge != null && charge.estDisponible(date, heure) && 
                charge.peutAjouterSoutenance(date, heure, pauseMinutes)) {
                disponibles.add(p);
            }
        }
        
        disponibles.sort(Comparator.comparingInt(p -> charges.get(p).getTotalSoutenances()));
        
        boolean encadrantEstInfo = estProfesseurInfo(encadrant);
        
        List<Professeur> infoDisponibles = new ArrayList<>();
        List<Professeur> nonInfoDisponibles = new ArrayList<>();
        
        for (Professeur p : disponibles) {
            if (estProfesseurInfo(p)) {
                infoDisponibles.add(p);
            } else {
                nonInfoDisponibles.add(p);
            }
        }
        
        List<Professeur> jurys = new ArrayList<>();
        
        if (encadrantEstInfo) {
            if (infoDisponibles.size() >= 1 && nonInfoDisponibles.size() >= 1) {
                jurys.add(infoDisponibles.get(0));
                jurys.add(nonInfoDisponibles.get(0));
            } else if (infoDisponibles.size() >= 2) {
                jurys.add(infoDisponibles.get(0));
                jurys.add(infoDisponibles.get(1));
            } else if (nonInfoDisponibles.size() >= 2) {
                jurys.add(nonInfoDisponibles.get(0));
                jurys.add(nonInfoDisponibles.get(1));
            }
        } else {
            if (infoDisponibles.size() >= 2) {
                jurys.add(infoDisponibles.get(0));
                jurys.add(infoDisponibles.get(1));
            } else if (infoDisponibles.size() >= 1 && nonInfoDisponibles.size() >= 1) {
                jurys.add(infoDisponibles.get(0));
                jurys.add(nonInfoDisponibles.get(0));
            }
        }
        
        return jurys;
    }
    
    private static class ChargeSuivi {
        private Professeur professeur;
        private int totalSoutenances;
        private Map<LocalDate, List<LocalTime>> soutenancesParJour;
        
        public ChargeSuivi(Professeur professeur) {
            this.professeur = professeur;
            this.totalSoutenances = 0;
            this.soutenancesParJour = new HashMap<>();
        }
        
        public void ajouterSoutenance(LocalDate date, LocalTime heure) {
            totalSoutenances++;
            soutenancesParJour.computeIfAbsent(date, k -> new ArrayList<>()).add(heure);
        }
        
        public int getChargeJour(LocalDate date) {
            List<LocalTime> soutenances = soutenancesParJour.get(date);
            return soutenances == null ? 0 : soutenances.size();
        }
        
        public boolean estDisponible(LocalDate date, LocalTime heure) {
            List<LocalTime> horaires = soutenancesParJour.get(date);
            if (horaires == null) return true;
            return !horaires.contains(heure);
        }
        
        public boolean peutAjouterSoutenance(LocalDate date, LocalTime heure, int pauseMinutes) {
            List<LocalTime> horaires = soutenancesParJour.get(date);
            if (horaires == null || pauseMinutes <= 0) return true;
            
            int heureSeconds = heure.toSecondOfDay();
            for (LocalTime h : horaires) {
                int ecartMinutes = Math.abs(heureSeconds - h.toSecondOfDay()) / 60;
                if (ecartMinutes < pauseMinutes) {
                    return false;
                }
            }
            return true;
        }
        
        public int getTotalSoutenances() { return totalSoutenances; }
    }
    
    private static class Creneau {
        LocalDate date;
        LocalTime heure;
        String salle;
        
        Creneau(LocalDate date, LocalTime heure, String salle) {
            this.date = date;
            this.heure = heure;
            this.salle = salle;
        }
    }
}