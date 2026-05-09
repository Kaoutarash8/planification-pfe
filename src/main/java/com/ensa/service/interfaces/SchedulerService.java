package com.ensa.service.interfaces;

import com.ensa.model.ConfigurationSoutenance;
import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.ScheduleResult;

import java.util.List;
import java.util.Map;

public interface SchedulerService {
    
    ScheduleResult genererPlanning(
        Map<Professeur, List<Etudiant>> affectation,
        List<Professeur> tousProfesseurs,
        Map<String, Etudiant> mapEtudiantsParCNE,
        ConfigurationSoutenance config
    );
    
    boolean isPlanningPossible(
        int nombreSoutenances,
        int nombreProfesseurs,
        List<Professeur> professeursInfo,
        ConfigurationSoutenance config
    );
}