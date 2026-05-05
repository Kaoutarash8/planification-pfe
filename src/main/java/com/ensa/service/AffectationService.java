package com.ensa.service;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import java.util.*;

public class AffectationService {
    
    public Map<Professeur, List<Etudiant>> genererAffectationEquilibree(List<Etudiant> etudiants, 
                                                                          List<Professeur> professeurs) {
        
        // Validation
        if (etudiants == null || etudiants.isEmpty()) {
            throw new IllegalArgumentException("La liste des étudiants ne peut pas être vide");
        }
        if (professeurs == null || professeurs.isEmpty()) {
            throw new IllegalArgumentException("La liste des professeurs ne peut pas être vide");
        }
        
        System.out.println("=== DÉBUT DE L'AFFECTATION ÉQUILIBRÉE ===");
        System.out.println("Total étudiants: " + etudiants.size());
        System.out.println("Total professeurs: " + professeurs.size());
        
        // 1. CRÉER UNE COPIE ET MÉLANGER LES PROFESSEURS
        List<Professeur> professeursMelanges = new ArrayList<>(professeurs);
        Collections.shuffle(professeursMelanges);
        
        // 2. CRÉER UNE COPIE ET MÉLANGER LES ÉTUDIANTS
        List<Etudiant> etudiantsMelanges = new ArrayList<>(etudiants);
        Collections.shuffle(etudiantsMelanges);
        
        // 3. CALCUL DE LA RÉPARTITION ÉQUILIBRÉE
        int totalEtudiants = etudiantsMelanges.size();
        int nbProfesseurs = professeursMelanges.size();
        int base = totalEtudiants / nbProfesseurs;
        int reste = totalEtudiants % nbProfesseurs;
        
        // 4. DISTRIBUER LES ÉTUDIANTS DE MANIÈRE ÉQUILIBRÉE
        Map<Professeur, List<Etudiant>> affectation = new LinkedHashMap<>();
        int indexEtudiant = 0;
        
        for (int i = 0; i < nbProfesseurs; i++) {
            Professeur prof = professeursMelanges.get(i);
            int nbEtudiants = base + (i < reste ? 1 : 0);
            
            List<Etudiant> etudiantsDuProf = new ArrayList<>();
            for (int j = 0; j < nbEtudiants && indexEtudiant < totalEtudiants; j++) {
                Etudiant e = etudiantsMelanges.get(indexEtudiant);
                etudiantsDuProf.add(e);
                e.setEncadrantNom(prof.getNom());
                e.setEncadrantPrenom(prof.getPrenom());
                indexEtudiant++;
            }
            
            // MÉLANGER LES ÉTUDIANTS DANS CHAQUE PROFESSEUR
            Collections.shuffle(etudiantsDuProf);
            affectation.put(prof, etudiantsDuProf);
            prof.setNbEtudiantsAffectes(etudiantsDuProf.size());
        }
        
        // 5. AFFICHER LE RÉSULTAT
        System.out.println("\n=== RAPPORT FINAL DE L'AFFECTATION ===");
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            Professeur prof = entry.getKey();
            int nb = entry.getValue().size();
            min = Math.min(min, nb);
            max = Math.max(max, nb);
            
            System.out.print(prof.getPrenom() + " " + prof.getNom() + ": " + nb + " étudiants → ");
            for (Etudiant e : entry.getValue()) {
                System.out.print(e.getPrenom() + " " + e.getNom() + " | ");
            }
            System.out.println();
        }
        
        System.out.println("\nMin: " + min + ", Max: " + max + ", Écart: " + (max - min));
        System.out.println("=== AFFECTATION GÉNÉRÉE AVEC SUCCÈS ===\n");
        
        return affectation;
    }
    
    public Map<String, Object> calculerStatistiques(List<Professeur> professeurs,
                                                     Map<Professeur, List<Etudiant>> affectation) {
        Map<String, Object> stats = new HashMap<>();
        
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int total = 0;
        
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            int nb = entry.getValue().size();
            total += nb;
            min = Math.min(min, nb);
            max = Math.max(max, nb);
        }
        
        stats.put("min", min);
        stats.put("max", max);
        stats.put("total", total);
        stats.put("nbProfesseurs", professeurs.size());
        stats.put("equilibre", (max - min) <= 1);
        
        return stats;
    }
}