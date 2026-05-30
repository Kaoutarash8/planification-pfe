package com.ensa.service.impl;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.Affectation;
import com.ensa.service.interfaces.AffectationService;
import java.util.*;

public class AffectationServiceImpl implements AffectationService {
    
    @Override
    public Affectation genererAffectation(List<Etudiant> etudiants, List<Professeur> professeurs) {
        
        // Validation
        if (etudiants == null || etudiants.isEmpty()) {
            throw new IllegalArgumentException("La liste des etudiants ne peut pas etre vide");
        }
        if (professeurs == null || professeurs.isEmpty()) {
            throw new IllegalArgumentException("La liste des professeurs ne peut pas etre vide");
        }
        
        System.out.println("=== DEBUT AFFECTATION ===");
        System.out.println("Etudiants: " + etudiants.size());
        System.out.println("Professeurs: " + professeurs.size());
        
        // Separer les professeurs par specialite
        List<Professeur> professeursInfo = new ArrayList<>();
        List<Professeur> professeursNonInfo = new ArrayList<>();
        
        for (Professeur p : professeurs) {
            String specialite = p.getSpecialite().toLowerCase();
            if (specialite.contains("info") || specialite.contains("informatique")) {
                professeursInfo.add(p);
            } else {
                professeursNonInfo.add(p);
            }
        }
        
        System.out.println("\n=== REPARTITION DES PROFESSEURS ===");
        System.out.println("Professeurs INFO: " + professeursInfo.size());
        System.out.println("Professeurs NON-INFO: " + professeursNonInfo.size());
        
        // Melanger chaque liste separement
        Collections.shuffle(professeursInfo);
        Collections.shuffle(professeursNonInfo);
        
        // Combiner les listes: les INFO en premier pour qu'ils aient la priorite
        List<Professeur> professeursMelanges = new ArrayList<>();
        professeursMelanges.addAll(professeursInfo);
        professeursMelanges.addAll(professeursNonInfo);
        
        // Regrouper les etudiants par filiere
        Map<String, List<Etudiant>> etudiantsParFiliere = new LinkedHashMap<>();
        for (Etudiant e : etudiants) {
            String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "Sans filiere" : e.getFiliere();
            etudiantsParFiliere.computeIfAbsent(filiere, k -> new ArrayList<>()).add(e);
        }
        
        System.out.println("\n=== FILIERES TROUVEES ===");
        for (Map.Entry<String, List<Etudiant>> entry : etudiantsParFiliere.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue().size() + " etudiants");
        }
        
        // Initialiser l'affectation
        Map<Professeur, List<Etudiant>> affectation = new LinkedHashMap<>();
        Map<Professeur, Map<String, Integer>> compteurFilieresParProf = new HashMap<>();
        
        for (Professeur p : professeursMelanges) {
            affectation.put(p, new ArrayList<>());
            compteurFilieresParProf.put(p, new HashMap<>());
        }
        
        // Distribution round-robin par filiere
        List<Professeur> professeursList = new ArrayList<>(professeursMelanges);
        int indexProf = 0;
        
        System.out.println("\n=== DISTRIBUTION ROUND-ROBIN ===");
        
        for (Map.Entry<String, List<Etudiant>> entry : etudiantsParFiliere.entrySet()) {
            String filiere = entry.getKey();
            List<Etudiant> etudiantsFiliere = entry.getValue();
            Collections.shuffle(etudiantsFiliere);
            
            System.out.println("\nDistribution filiere: " + filiere);
            
            for (Etudiant etudiant : etudiantsFiliere) {
                Professeur prof = professeursList.get(indexProf % professeursList.size());
                affectation.get(prof).add(etudiant);
                
                Map<String, Integer> compteur = compteurFilieresParProf.get(prof);
                compteur.put(filiere, compteur.getOrDefault(filiere, 0) + 1);
                
                System.out.println("  " + etudiant.getPrenom() + " " + etudiant.getNom() + " -> " + 
                                   prof.getNom().toUpperCase() + " " + prof.getPrenom() + 
                                   " (" + prof.getSpecialite() + ")");
                
                indexProf++;
            }
        }
        
        // Calculer la charge cible
        int totalEtudiants = etudiants.size();
        int nbProfesseurs = professeursMelanges.size();
        int cibleMin = totalEtudiants / nbProfesseurs;
        int cibleMax = (int) Math.ceil((double) totalEtudiants / nbProfesseurs);
        
        System.out.println("\n=== EQUILIBRAGE AVEC PRIORITE INFO ===");
        System.out.println("Cible: min=" + cibleMin + ", max=" + cibleMax);
        System.out.println("Les professeurs INFO auront la priorite pour recevoir +1 etudiant");
        
        // RULE: Les professeurs INFO recoivent la charge maximale (X+1) en priorite
        List<Professeur> professeursAvecMax = new ArrayList<>();
        List<Professeur> professeursAvecMin = new ArrayList<>();
        
        // Compter combien de professeurs doivent avoir X+1
        int nbProfesseursAvecMax = totalEtudiants - (nbProfesseurs * cibleMin);
        
        System.out.println("Nombre de professeurs qui auront " + cibleMax + " etudiants: " + nbProfesseursAvecMax);
        
        // Priorite aux professeurs INFO pour la charge maximale
        for (Professeur p : professeursMelanges) {
            boolean estInfo = p.getSpecialite().toLowerCase().contains("info") || 
                              p.getSpecialite().toLowerCase().contains("informatique");
            if (estInfo && professeursAvecMax.size() < nbProfesseursAvecMax) {
                professeursAvecMax.add(p);
            } else {
                professeursAvecMin.add(p);
            }
        }
        
        // Si besoin de plus de professeurs pour X+1, ajouter des NON-INFO
        if (professeursAvecMax.size() < nbProfesseursAvecMax) {
            for (Professeur p : professeursAvecMin) {
                if (professeursAvecMax.size() < nbProfesseursAvecMax) {
                    professeursAvecMax.add(p);
                    professeursAvecMin.remove(p);
                    break;
                }
            }
        }
        
        System.out.println("\n=== REPARTITION DES CHARGES ===");
        System.out.println("Professeurs avec " + cibleMax + " etudiants (priorite INFO):");
        for (Professeur p : professeursAvecMax) {
            System.out.println("  - " + p.getNom().toUpperCase() + " " + p.getPrenom() + " (" + p.getSpecialite() + ")");
        }
        System.out.println("\nProfesseurs avec " + cibleMin + " etudiants:");
        for (Professeur p : professeursAvecMin) {
            System.out.println("  - " + p.getNom().toUpperCase() + " " + p.getPrenom() + " (" + p.getSpecialite() + ")");
        }
        
        // Ajuster l'affectation pour respecter la repartition cible
        ajusterAffectationAvecPrioriteInfo(affectation, professeursAvecMax, professeursAvecMin, cibleMax, cibleMin);
        
        // Mettre a jour les compteurs
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            entry.getKey().setNbEtudiantsAffectes(entry.getValue().size());
        }
        
        // Trier les professeurs par nombre d'etudiants (du plus grand au plus petit)
        Map<Professeur, List<Etudiant>> affectationTriee = new LinkedHashMap<>();
        affectation.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .forEach(entry -> affectationTriee.put(entry.getKey(), entry.getValue()));
        
        // Creer l'objet resultat
        Affectation resultat = new Affectation();
        resultat.setAffectations(affectationTriee);
        resultat.setTotalEtudiants(totalEtudiants);
        resultat.setTotalProfesseurs(nbProfesseurs);
        
        // Afficher les statistiques
        afficherStatistiquesFinales(resultat, compteurFilieresParProf);
        
        return resultat;
    }
    
    /**
     * Ajuste l'affectation pour que les professeurs INFO aient la priorite pour X+1 etudiants
     */
    private void ajusterAffectationAvecPrioriteInfo(Map<Professeur, List<Etudiant>> affectation,
                                                     List<Professeur> professeursAvecMax,
                                                     List<Professeur> professeursAvecMin,
                                                     int cibleMax, int cibleMin) {
        
        boolean modifie = true;
        int maxIterations = 100;
        int iteration = 0;
        
        while (modifie && iteration < maxIterations) {
            modifie = false;
            iteration++;
            
            // Parcourir les professeurs qui doivent avoir X+1 mais qui ont moins
            for (Professeur profMax : professeursAvecMax) {
                int tailleActuelle = affectation.get(profMax).size();
                
                if (tailleActuelle < cibleMax) {
                    // Chercher un professeur qui a trop d'etudiants
                    for (Professeur profMin : professeursAvecMin) {
                        int tailleAutre = affectation.get(profMin).size();
                        
                        if (tailleAutre > cibleMin) {
                            // Deplacer un etudiant
                            List<Etudiant> etudiantsDonneur = affectation.get(profMin);
                            if (!etudiantsDonneur.isEmpty()) {
                                Etudiant e = etudiantsDonneur.remove(etudiantsDonneur.size() - 1);
                                affectation.get(profMax).add(e);
                                
                                System.out.println("  Deplacement prioritaire: " + e.getPrenom() + " " + e.getNom() +
                                                   " de " + profMin.getNom().toUpperCase() + " " + profMin.getPrenom() +
                                                   " (" + profMin.getSpecialite() + ") vers " +
                                                   profMax.getNom().toUpperCase() + " " + profMax.getPrenom() +
                                                   " (" + profMax.getSpecialite() + ") [INFO PRIORITAIRE]");
                                modifie = true;
                                break;
                            }
                        }
                    }
                }
                if (modifie) break;
            }
            
            // Deuxieme passage: equilibrer globalement
            if (!modifie) {
                for (Professeur donneur : affectation.keySet()) {
                    for (Professeur receveur : affectation.keySet()) {
                        if (donneur.equals(receveur)) continue;
                        
                        int tailleDonneur = affectation.get(donneur).size();
                        int tailleReceveur = affectation.get(receveur).size();
                        
                        if (tailleDonneur > cibleMax && tailleReceveur < cibleMin) {
                            List<Etudiant> etudiantsDonneur = affectation.get(donneur);
                            if (!etudiantsDonneur.isEmpty()) {
                                Etudiant e = etudiantsDonneur.remove(etudiantsDonneur.size() - 1);
                                affectation.get(receveur).add(e);
                                
                                System.out.println("  Deplacement equilibrage: " + e.getPrenom() + " " + e.getNom() +
                                                   " de " + donneur.getNom().toUpperCase() + " " + donneur.getPrenom() +
                                                   " vers " + receveur.getNom().toUpperCase() + " " + receveur.getPrenom());
                                modifie = true;
                                break;
                            }
                        }
                    }
                    if (modifie) break;
                }
            }
        }
        
        System.out.println("\n=== RESULTAT FINAL APRES AJUSTEMENT ===");
        for (Professeur p : affectation.keySet()) {
            int charge = affectation.get(p).size();
            String priorite = "";
            if (professeursAvecMax.contains(p)) {
                priorite = " [MAX - PRIORITAIRE INFO]";
            } else if (professeursAvecMin.contains(p)) {
                priorite = " [MIN]";
            }
            System.out.println("  " + p.getNom().toUpperCase() + " " + p.getPrenom() + 
                               " (" + p.getSpecialite() + "): " + charge + " etudiants" + priorite);
        }
    }
    
    /**
     * Afficher les statistiques finales
     */
    private void afficherStatistiquesFinales(Affectation affectation,
                                              Map<Professeur, Map<String, Integer>> compteurFilieres) {
        
        System.out.println("\n=== STATISTIQUES FINALES ===");
        System.out.println("Total etudiants: " + affectation.getTotalEtudiants());
        System.out.println("Total professeurs: " + affectation.getTotalProfesseurs());
        System.out.println("Min par professeur: " + affectation.getMinParProfesseur());
        System.out.println("Max par professeur: " + affectation.getMaxParProfesseur());
        System.out.println("Affectation equilibree: " + (affectation.estEquilibree() ? "OUI" : "NON"));
        
        System.out.println("\n=== VERIFICATION PRIORITE INFO ===");
        int maxCharge = affectation.getMaxParProfesseur();
        int countInfoAvecMax = 0;
        int countNonInfoAvecMax = 0;
        
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.getAffectations().entrySet()) {
            Professeur p = entry.getKey();
            int charge = entry.getValue().size();
            boolean estInfo = p.getSpecialite().toLowerCase().contains("info") || 
                              p.getSpecialite().toLowerCase().contains("informatique");
            
            if (charge == maxCharge) {
                if (estInfo) {
                    countInfoAvecMax++;
                } else {
                    countNonInfoAvecMax++;
                }
            }
        }
        
        System.out.println("  Professeurs INFO avec charge maximale (" + maxCharge + "): " + countInfoAvecMax);
        System.out.println("  Professeurs NON-INFO avec charge maximale: " + countNonInfoAvecMax);
        
        if (countInfoAvecMax > 0 && countNonInfoAvecMax == 0) {
            System.out.println("  ✅ Priorite INFO respectee: Tous les professeurs avec charge max sont INFO");
        } else if (countInfoAvecMax > countNonInfoAvecMax) {
            System.out.println("  ⚠️ Priorite INFO partiellement respectee");
        } else {
            System.out.println("  ❌ Priorite INFO non respectee");
        }
        
        System.out.println("\n=== DETAIL PAR PROFESSEUR ===");
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.getAffectations().entrySet()) {
            Professeur prof = entry.getKey();
            List<Etudiant> etudiants = entry.getValue();
            
            String infoTag = (prof.getSpecialite().toLowerCase().contains("info") || 
                              prof.getSpecialite().toLowerCase().contains("informatique")) ? " [INFO]" : " [AUTRE]";
            
            System.out.println("\n" + prof.getNom().toUpperCase() + " " + prof.getPrenom() + 
                               " (" + prof.getSpecialite() + ")" + infoTag);
            System.out.println("   Total: " + etudiants.size() + " etudiants");
            
            Map<String, Integer> repartition = compteurFilieres.get(prof);
            if (repartition != null && !repartition.isEmpty()) {
                System.out.println("   Repartition par filiere:");
                for (Map.Entry<String, Integer> f : repartition.entrySet()) {
                    System.out.println("      " + f.getKey() + ": " + f.getValue() + " etudiant(s)");
                }
            }
        }
    }
}