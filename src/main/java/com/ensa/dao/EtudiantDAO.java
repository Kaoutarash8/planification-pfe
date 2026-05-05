package com.ensa.dao;

import com.ensa.model.Etudiant;
import com.ensa.util.ExcelReader;
import java.util.List;

public class EtudiantDAO {
    
    public List<Etudiant> lireEtudiants(String filePath) throws Exception {
        return ExcelReader.lireEtudiants(filePath);
    }
}