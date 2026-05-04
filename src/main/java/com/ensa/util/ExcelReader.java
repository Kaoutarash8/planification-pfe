package com.ensa.util;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {
    
    public static List<Etudiant> lireEtudiants(String filePath) throws Exception {
        List<Etudiant> etudiants = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            
            // Identifier les colonnes
            Map<String, Integer> columnIndex = new HashMap<>();
            
            for (Row row : sheet) {
                if (firstRow) {
                    // Lire les en-têtes pour identifier les colonnes
                    for (Cell cell : row) {
                        String header = getCellValue(cell);
                        columnIndex.put(header, cell.getColumnIndex());
                    }
                    firstRow = false;
                    continue;
                }
                
                Etudiant etudiant = new Etudiant();
                
                // Colonnes de base
                etudiant.setCne(getCellValue(row.getCell(columnIndex.getOrDefault("CNE", 0))));
                etudiant.setNom(getCellValue(row.getCell(columnIndex.getOrDefault("NOM", 1))));
                etudiant.setPrenom(getCellValue(row.getCell(columnIndex.getOrDefault("PRENOM", 2))));
                etudiant.setEmailPersonnel(getCellValue(row.getCell(columnIndex.getOrDefault("EMAIL PERSONNEL", 3))));
                etudiant.setEmailAcademique(getCellValue(row.getCell(columnIndex.getOrDefault("EMAIL ACADEMIQUE", 4))));
                etudiant.setFiliere(getCellValue(row.getCell(columnIndex.getOrDefault("FILIERE", 5))));
                
                // Colonnes d'encadrant (importantes pour le planning)
                etudiant.setEncadrantNom(getCellValue(row.getCell(columnIndex.getOrDefault("ENCADRANT NOM", 6))));
                etudiant.setEncadrantPrenom(getCellValue(row.getCell(columnIndex.getOrDefault("ENCADRANT PRENOM", 7))));
                
                etudiants.add(etudiant);
            }
        }
        
        return etudiants;
    }
    
    public static List<Professeur> lireProfesseurs(String filePath) throws Exception {
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
                
                Professeur professeur = new Professeur();
                professeur.setNom(getCellValue(row.getCell(0)));
                professeur.setPrenom(getCellValue(row.getCell(1)));
                professeur.setSpecialite(getCellValue(row.getCell(2)));
                
                professeurs.add(professeur);
            }
        }
        
        return professeurs;
    }
    
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}