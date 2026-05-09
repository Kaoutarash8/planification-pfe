package com.ensa.service.interfaces;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.Affectation;
import java.util.List;

public interface AffectationService {
    Affectation genererAffectation(List<Etudiant> etudiants, List<Professeur> professeurs);
}