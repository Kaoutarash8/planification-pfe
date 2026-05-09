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
            Map<String, Integer> columnIndex = new HashMap<>();
            
            for (Row row : sheet) {
                if (firstRow) {
                    for (Cell cell : row) {
                        String header = getCellValue(cell).toUpperCase();
                        columnIndex.put(header, cell.getColumnIndex());
                    }
                    firstRow = false;
                    continue;
                }
                
                String nom = getCellValue(row.getCell(columnIndex.getOrDefault("NOM", 0)));
                String prenom = getCellValue(row.getCell(columnIndex.getOrDefault("PRENOM", 1)));
                String specialite = getCellValue(row.getCell(columnIndex.getOrDefault("SPECIALITE", 2)));
                
                if (nom.isEmpty() && prenom.isEmpty()) {
                    nom = getCellValue(row.getCell(0));
                    prenom = getCellValue(row.getCell(1));
                    specialite = getCellValue(row.getCell(2));
                }
                
                if (!nom.isEmpty() && !prenom.isEmpty()) {
                    Professeur p = new Professeur();
                    p.setNom(nom.toUpperCase());  // NOM en majuscules
                    p.setPrenom(prenom);           // Prénom normal
                    p.setSpecialite(specialite);
                    professeurs.add(p);
                }
            }
        }
        
        System.out.println("Total professeurs lus: " + professeurs.size());
        return professeurs;
    }
    
    /**
     * Retourne le nom complet formaté: "Prénom NOM"
     */
    public static String getNomCompletProfesseur(Professeur p) {
        if (p == null) return "";
        return p.getPrenom() + " " + p.getNom().toUpperCase();
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