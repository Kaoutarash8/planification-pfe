package com.ensa.dao.interfaces;

import com.ensa.model.Etudiant;
import java.util.List;

public interface EtudiantDao {
    List<Etudiant> lireEtudiants(String filePath) throws Exception;
}