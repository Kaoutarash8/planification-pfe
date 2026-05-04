package com.ensa.service;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import java.util.*;

public class AffectationService {
    
    /**
     * Génère une affectation équilibrée des étudiants aux professeurs
     * @param etudiants Liste des étudiants
     * @param professeurs Liste des professeurs
     * @return Map contenant l'affectation
     */
    public Map<Professeur, List<Etudiant>> genererAffectationEquilibree(List<Etudiant> etudiants, 
                                                                          List<Professeur> professeurs) {
        
        if (etudiants == null || etudiants.isEmpty() || professeurs == null || professeurs.isEmpty()) {
            return new HashMap<>();
        }
        
        // Créer une copie de la liste des professeurs
        List<Professeur> profsCopy = new ArrayList<>(professeurs);
        
        // Initialiser l'affectation
        Map<Professeur, List<Etudiant>> affectation = new LinkedHashMap<>();
        for (Professeur prof : profsCopy) {
            affectation.put(prof, new ArrayList<>());
            prof.setNbEtudiantsAffectes(0);
        }
        
        // Calculer la répartition équitable
        int nbEtudiants = etudiants.size();
        int nbProfesseurs = profsCopy.size();
        int base = nbEtudiants / nbProfesseurs;
        int reste = nbEtudiants % nbProfesseurs;
        
        // Mélanger les étudiants pour une répartition aléatoire mais équitable
        List<Etudiant> etudiantsMelanges = new ArrayList<>(etudiants);
        Collections.shuffle(etudiantsMelanges);
        
        // Répartir les étudiants
        int indexEtudiant = 0;
        for (int i = 0; i < nbProfesseurs; i++) {
            int nbAAffecter = base + (i < reste ? 1 : 0);
            Professeur prof = profsCopy.get(i);
            
            for (int j = 0; j < nbAAffecter && indexEtudiant < nbEtudiants; j++) {
                Etudiant etudiant = etudiantsMelanges.get(indexEtudiant);
                etudiant.setEncadrantNom(prof.getNom());
                etudiant.setEncadrantPrenom(prof.getPrenom());
                affectation.get(prof).add(etudiant);
                prof.incrementerNbEtudiants();
                indexEtudiant++;
            }
        }
        
        return affectation;
    }
    
    /**
     * Calcule les statistiques de l'affectation
     */
    public Map<String, Object> calculerStatistiques(List<Professeur> professeurs, 
                                                     Map<Professeur, List<Etudiant>> affectation) {
        Map<String, Object> stats = new HashMap<>();
        
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int total = 0;
        
        for (Professeur prof : professeurs) {
            int nb = affectation.get(prof).size();
            total += nb;
            if (nb < min) min = nb;
            if (nb > max) max = nb;
        }
        
        stats.put("min", min);
        stats.put("max", max);
        stats.put("total", total);
        stats.put("nbProfesseurs", professeurs.size());
        stats.put("equilibre", (max - min) <= 1);
        
        return stats;
    }
}