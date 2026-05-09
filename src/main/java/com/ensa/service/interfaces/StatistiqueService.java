package com.ensa.service.interfaces;

import java.util.Map;

public interface StatistiqueService {
    int getTotalEtudiants(String planningPath) throws Exception;
    int getTotalSoutenances(String planningPath) throws Exception;
    int getTotalEncadrants(String planningPath) throws Exception;
    int getTotalSalles(String planningPath) throws Exception;
    int getTotalMembresJury(String planningPath) throws Exception;
    int getTotalJours(String planningPath) throws Exception;
    Map<String, Integer> getRepartitionParFiliere(String planningPath) throws Exception;
    Map<String, Integer> getSoutenancesParJour(String planningPath) throws Exception;
    Map<String, Integer> getSoutenancesParSalle(String planningPath) throws Exception;
    Map<String, Integer> getEtudiantsParEncadrant(String planningPath, int limit) throws Exception;
    Map<String, Integer> getParticipationsParSpecialite(String planningPath) throws Exception;
    double getChargeMoyenneParProfesseur(String planningPath) throws Exception;
}