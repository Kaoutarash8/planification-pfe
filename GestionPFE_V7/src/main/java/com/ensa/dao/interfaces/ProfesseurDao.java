package com.ensa.dao.interfaces;

import com.ensa.model.Professeur;
import java.util.List;

public interface ProfesseurDao {
    List<Professeur> lireProfesseurs(String filePath) throws Exception;
}