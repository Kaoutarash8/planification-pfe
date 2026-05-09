package com.ensa.dao.impl;

import com.ensa.dao.interfaces.ProfesseurDao;
import com.ensa.model.Professeur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.*;

public class ProfesseurExcelDao implements ProfesseurDao {
    
    @Override
    public List<Professeur> lireProfesseurs(String filePath) throws Exception {
        List<Professeur> professeurs = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }
                
                String nom = getCellValue(row.getCell(0));
                String prenom = getCellValue(row.getCell(1));
                String specialite = getCellValue(row.getCell(2));
                
                if (!nom.isEmpty() && !prenom.isEmpty()) {
                    Professeur p = new Professeur(nom, prenom, specialite);
                    professeurs.add(p);
                }
            }
        }
        
        return professeurs;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}