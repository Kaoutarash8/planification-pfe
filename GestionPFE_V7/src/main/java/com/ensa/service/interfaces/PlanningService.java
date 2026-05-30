package com.ensa.service.interfaces;

import com.ensa.model.*;
import java.util.*;

public interface PlanningService {
    List<PlanningSoutenance> genererPlanning(
        Map<Professeur, List<Etudiant>> affectation,
        List<Professeur> tousProfesseurs,
        Map<String, Etudiant> mapEtudiantsParCNE,
        ConfigurationSoutenance config
    ) throws Exception;
}