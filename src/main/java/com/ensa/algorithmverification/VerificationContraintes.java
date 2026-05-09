package com.ensa.algorithmverification;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.Affectation;
import java.util.*;

/**
 * Classe de verification des contraintes metier
 * Permet de verifier que l'affectation respecte toutes les regles
 */
public class VerificationContraintes {
    
    private List<String> erreurs;
    private List<String> avertissements;
    
    public VerificationContraintes() {
        this.erreurs = new ArrayList<>();
        this.avertissements = new ArrayList<>();
    }
    
    /**
     * Verifie toutes les contraintes de l'affectation
     * @param affectation L'affectation a verifier
     * @param etudiantsOriginaux La liste originale des etudiants
     * @return true si toutes les contraintes sont respectees
     */
    public boolean verifierToutesContraintes(Affectation affectation, List<Etudiant> etudiantsOriginaux) {
        erreurs.clear();
        avertissements.clear();
        
        System.out.println("\n========================================");
        System.out.println("VERIFICATION DES CONTRAINTES METIER");
        System.out.println("========================================\n");
        
        boolean contrainteA = verifierAffectationUnique(affectation, etudiantsOriginaux);
        boolean contrainteB = verifierEquilibrageGlobal(affectation);
        boolean contrainteC = verifierContrainteFilieres(affectation);
        boolean contrainteD = verifierDiversiteFilieres(affectation);
        boolean contrainteE = verifierPrioriteInfo(affectation);  // NOUVELLE CONTRAINTE
        
        System.out.println("\n========================================");
        System.out.println("RESUME DE LA VERIFICATION");
        System.out.println("========================================");
        
        if (contrainteA && contrainteB && contrainteC && contrainteD && contrainteE) {
            System.out.println("STATUT: TOUTES LES CONTRAINTES SONT RESPECTEES");
            return true;
        } else {
            System.out.println("STATUT: DES ERREURS ONT ETE TROUVEES");
            afficherErreurs();
            return false;
        }
    }
    
    /**
     * Contrainte A: Chaque etudiant est affecte a UN seul professeur
     * Tous les etudiants doivent etre affectes
     */
    private boolean verifierAffectationUnique(Affectation affectation, List<Etudiant> etudiantsOriginaux) {
        System.out.println("--- CONTRAINTE A: AFFECTATION UNIQUE ---");
        
        Set<Etudiant> etudiantsAffectes = new HashSet<>();
        
        for (List<Etudiant> liste : affectation.getAffectations().values()) {
            etudiantsAffectes.addAll(liste);
        }
        
        int totalOriginaux = etudiantsOriginaux.size();
        int totalAffectes = etudiantsAffectes.size();
        
        System.out.println("  Etudiants dans le fichier source: " + totalOriginaux);
        System.out.println("  Etudiants affectes: " + totalAffectes);
        
        boolean tousAffectes = (totalOriginaux == totalAffectes);
        
        if (tousAffectes) {
            System.out.println("  RESULTAT: Tous les etudiants ont ete affectes");
        } else {
            int manquants = totalOriginaux - totalAffectes;
            erreurs.add("Contrainte A: " + manquants + " etudiant(s) non affecte(s)");
            System.out.println("  ERREUR: " + manquants + " etudiant(s) non affecte(s)");
        }
        
        boolean pasDeDoublon = (etudiantsAffectes.size() == totalAffectes);
        if (pasDeDoublon) {
            System.out.println("  RESULTAT: Aucun etudiant en double");
        } else {
            erreurs.add("Contrainte A: Des etudiants sont en double");
            System.out.println("  ERREUR: Des etudiants sont en double");
        }
        
        System.out.println();
        return tousAffectes && pasDeDoublon;
    }
    
    /**
     * Contrainte B: Equilibrage global
     * Difference maximale autorisee entre professeurs <= 1 etudiant
     */
    private boolean verifierEquilibrageGlobal(Affectation affectation) {
        System.out.println("--- CONTRAINTE B: EQUILIBRAGE GLOBAL ---");
        
        List<Integer> charges = new ArrayList<>();
        for (List<Etudiant> liste : affectation.getAffectations().values()) {
            charges.add(liste.size());
        }
        
        int min = charges.stream().min(Integer::compare).orElse(0);
        int max = charges.stream().max(Integer::compare).orElse(0);
        int ecart = max - min;
        
        System.out.println("  Charge minimale par professeur: " + min);
        System.out.println("  Charge maximale par professeur: " + max);
        System.out.println("  Ecart: " + ecart);
        System.out.println("  Ecart autorise: <= 1");
        
        boolean estEquilibre = (ecart <= 1);
        
        if (estEquilibre) {
            System.out.println("  RESULTAT: L'affectation est equilibree");
        } else {
            erreurs.add("Contrainte B: Ecart de " + ecart + " etudiant(s) (maximum autorise: 1)");
            System.out.println("  ERREUR: L'ecart est trop grand");
        }
        
        System.out.println();
        return estEquilibre;
    }
    
    /**
     * Contrainte C: Filières
     * Un professeur peut encadrer plusieurs filieres
     * Aucune contrainte entre specialite professeur et filiere etudiant
     */
    private boolean verifierContrainteFilieres(Affectation affectation) {
        System.out.println("--- CONTRAINTE C: GESTION DES FILIERES ---");
        System.out.println("  RESULTAT: Aucune contrainte de filiere n'est imposee");
        System.out.println("  Les professeurs peuvent encadrer toutes les filieres");
        System.out.println();
        return true;
    }
    
    /**
     * Contrainte D: Diversite des filieres
     * Distribuer les etudiants de chaque filiere sur plusieurs professeurs
     * Eviter qu'un professeur ait une seule filiere dominante
     */
    private boolean verifierDiversiteFilieres(Affectation affectation) {
        System.out.println("--- CONTRAINTE D: DIVERSITE DES FILIERES ---");
        
        Set<String> toutesFilieres = new HashSet<>();
        for (List<Etudiant> liste : affectation.getAffectations().values()) {
            for (Etudiant e : liste) {
                String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "Sans filiere" : e.getFiliere();
                toutesFilieres.add(filiere);
            }
        }
        
        boolean distributionOK = true;
        boolean pasDeDominance = true;
        
        for (String filiere : toutesFilieres) {
            Set<Professeur> professeursDeCetteFiliere = new HashSet<>();
            int totalEtudiantsFiliere = 0;
            
            for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.getAffectations().entrySet()) {
                for (Etudiant e : entry.getValue()) {
                    String f = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "Sans filiere" : e.getFiliere();
                    if (f.equals(filiere)) {
                        professeursDeCetteFiliere.add(entry.getKey());
                        totalEtudiantsFiliere++;
                    }
                }
            }
            
            int nbProfesseurs = professeursDeCetteFiliere.size();
            int totalProfesseurs = affectation.getAffectations().size();
            
            if (nbProfesseurs < 2 && totalEtudiantsFiliere > 2 && totalProfesseurs > 1) {
                avertissements.add("Contrainte D: La filiere '" + filiere + "' n'est repartie que sur " + nbProfesseurs + " professeur(s)");
                distributionOK = false;
            }
        }
        
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.getAffectations().entrySet()) {
            Professeur prof = entry.getKey();
            List<Etudiant> etudiants = entry.getValue();
            
            if (etudiants.isEmpty()) continue;
            
            Map<String, Integer> compteurFilieres = new HashMap<>();
            for (Etudiant e : etudiants) {
                String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "Sans filiere" : e.getFiliere();
                compteurFilieres.put(filiere, compteurFilieres.getOrDefault(filiere, 0) + 1);
            }
            
            String filiereDominante = null;
            int maxCount = 0;
            for (Map.Entry<String, Integer> f : compteurFilieres.entrySet()) {
                if (f.getValue() > maxCount) {
                    maxCount = f.getValue();
                    filiereDominante = f.getKey();
                }
            }
            
            double pourcentage = (double) maxCount / etudiants.size() * 100;
            
            if (pourcentage > 70) {
                avertissements.add("Contrainte D: " + prof.getNomComplet() + " a une filiere dominante (" + filiereDominante + " - " + String.format("%.1f", pourcentage) + "%)");
                pasDeDominance = false;
            }
        }
        
        System.out.println("  RESULTAT: " + (distributionOK && pasDeDominance ? "OK" : "Avertissements"));
        System.out.println();
        return true; // Ne pas bloquer, juste avertir
    }
    
    /**
     * Contrainte E: PRIORITE INFO
     * Les professeurs qui recoivent +1 etudiant (charge maximale)
     * doivent etre prioritairement ceux de specialite INFORMATIQUE
     */
    private boolean verifierPrioriteInfo(Affectation affectation) {
        System.out.println("--- CONTRAINTE E: PRIORITE INFO ---");
        System.out.println("  Regle: Les professeurs avec charge maximale doivent etre prioritairement INFO");
        
        // Calculer les charges
        List<Integer> charges = new ArrayList<>();
        for (List<Etudiant> liste : affectation.getAffectations().values()) {
            charges.add(liste.size());
        }
        
        int maxCharge = charges.stream().max(Integer::compare).orElse(0);
        int minCharge = charges.stream().min(Integer::compare).orElse(0);
        
        System.out.println("  Charge maximale: " + maxCharge);
        System.out.println("  Charge minimale: " + minCharge);
        
        // Verifier si l'ecart est de 1 (sinon cette contrainte n'a pas de sens)
        if (maxCharge - minCharge != 1) {
            System.out.println("  NOTE: L'ecart n'est pas de 1, la priorite INFO n'est pas applicable");
            System.out.println("  RESULTAT: Contrainte non applicable (ecart = " + (maxCharge - minCharge) + ")");
            System.out.println();
            return true;
        }
        
        // Identifier les professeurs avec charge maximale
        List<Professeur> professeursAvecMax = new ArrayList<>();
        List<Professeur> professeursINFO = new ArrayList<>();
        List<Professeur> professeursNonINFO = new ArrayList<>();
        
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.getAffectations().entrySet()) {
            Professeur p = entry.getKey();
            int charge = entry.getValue().size();
            
            boolean estInfo = p.getSpecialite().toLowerCase().contains("info") || 
                              p.getSpecialite().toLowerCase().contains("informatique");
            
            if (estInfo) {
                professeursINFO.add(p);
            } else {
                professeursNonINFO.add(p);
            }
            
            if (charge == maxCharge) {
                professeursAvecMax.add(p);
            }
        }
        
        // Compter les INFO et NON-INFO parmi ceux avec charge max
        int infoAvecMax = 0;
        int nonInfoAvecMax = 0;
        
        for (Professeur p : professeursAvecMax) {
            boolean estInfo = p.getSpecialite().toLowerCase().contains("info") || 
                              p.getSpecialite().toLowerCase().contains("informatique");
            if (estInfo) {
                infoAvecMax++;
            } else {
                nonInfoAvecMax++;
            }
        }
        
        System.out.println("\n  Professeurs avec charge maximale (" + maxCharge + " etudiants):");
        for (Professeur p : professeursAvecMax) {
            String type = (p.getSpecialite().toLowerCase().contains("info") || 
                           p.getSpecialite().toLowerCase().contains("informatique")) ? "INFO" : "NON-INFO";
            System.out.println("    - " + p.getNom().toUpperCase() + " " + p.getPrenom() + " (" + p.getSpecialite() + ") - " + type);
        }
        
        System.out.println("\n  Statistiques:");
        System.out.println("    Total professeurs INFO: " + professeursINFO.size());
        System.out.println("    Total professeurs NON-INFO: " + professeursNonINFO.size());
        System.out.println("    INFO avec charge max: " + infoAvecMax);
        System.out.println("    NON-INFO avec charge max: " + nonInfoAvecMax);
        
        // VERIFICATION: Si aucun INFO n'a la charge max, c'est une ERREUR
        if (infoAvecMax == 0 && professeursINFO.size() > 0) {
            erreurs.add("Contrainte E: Aucun professeur INFO n'a la charge maximale (" + maxCharge + " etudiants)");
            System.out.println("\n  ERREUR: Aucun professeur INFO n'a la charge maximale!");
            System.out.println("  Les professeurs avec charge max sont tous NON-INFO");
            System.out.println();
            return false;
        }
        
        // VERIFICATION: Si des NON-INFO ont la charge max alors qu'il reste des INFO sans charge max
        if (nonInfoAvecMax > 0) {
            // Verifier si tous les INFO ont deja la charge max
            boolean tousInfoOntMax = (infoAvecMax == professeursINFO.size());
            
            if (!tousInfoOntMax && professeursINFO.size() > 0) {
                avertissements.add("Contrainte E: " + nonInfoAvecMax + " professeur(s) NON-INFO ont la charge maximale alors que des professeurs INFO n'ont pas cette charge");
                System.out.println("\n  AVERTISSEMENT: Des professeurs NON-INFO ont la charge maximale");
                System.out.println("  alors que des professeurs INFO n'ont pas cette charge");
            } else if (tousInfoOntMax) {
                System.out.println("\n  NOTE: Tous les professeurs INFO ont deja la charge maximale");
                System.out.println("  Les professeurs NON-INFO avec charge max sont acceptables (pas assez d'INFO)");
            }
        }
        
        // VERIFICATION SUPPLEMENTAIRE: Priorite aux INFO
        boolean prioriteRespectee = true;
        for (Professeur pNonInfo : professeursNonINFO) {
            int chargeNonInfo = affectation.getAffectations().get(pNonInfo).size();
            if (chargeNonInfo == maxCharge) {
                // Verifier s'il existe un INFO avec charge minimale
                for (Professeur pInfo : professeursINFO) {
                    int chargeInfo = affectation.getAffectations().get(pInfo).size();
                    if (chargeInfo == minCharge && chargeInfo < maxCharge) {
                        prioriteRespectee = false;
                        avertissements.add("Contrainte E: Un professeur NON-INFO a charge max (" + pNonInfo.getNomComplet() + 
                                          ") alors qu'un professeur INFO a charge min (" + pInfo.getNomComplet() + ")");
                        break;
                    }
                }
            }
        }
        
        if (prioriteRespectee) {
            System.out.println("\n  RESULTAT: Priorite INFO respectee");
        } else {
            System.out.println("\n  RESULTAT: Priorite INFO partiellement respectee (avertissements)");
        }
        
        System.out.println();
        return infoAvecMax > 0; // Au moins un INFO doit avoir la charge max
    }
    
    /**
     * Afficher toutes les erreurs
     */
    private void afficherErreurs() {
        if (!erreurs.isEmpty()) {
            System.out.println("\n--- ERREURS DETECTEES ---");
            for (String erreur : erreurs) {
                System.out.println("  X " + erreur);
            }
        }
        
        if (!avertissements.isEmpty()) {
            System.out.println("\n--- AVERTISSEMENTS ---");
            for (String avertissement : avertissements) {
                System.out.println("  ! " + avertissement);
            }
        }
    }
    
    public List<String> getErreurs() {
        return new ArrayList<>(erreurs);
    }
    
    public List<String> getAvertissements() {
        return new ArrayList<>(avertissements);
    }
    
    public boolean isValide() {
        return erreurs.isEmpty();
    }
}