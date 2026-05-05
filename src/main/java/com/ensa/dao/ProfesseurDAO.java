package com.ensa.dao;

import com.ensa.model.Professeur;
import com.ensa.util.ExcelReader;
import java.util.List;

public class ProfesseurDAO {
    
    public List<Professeur> lireProfesseurs(String filePath) throws Exception {
        return ExcelReader.lireProfesseurs(filePath);
    }
}