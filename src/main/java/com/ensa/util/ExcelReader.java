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
            
            Map<String, Integer> columnIndex = new HashMap<>();
            
            for (Row row : sheet) {
                if (firstRow) {
                    for (Cell cell : row) {
                        String header = getCellValueAsString(cell);
                        columnIndex.put(header.toUpperCase(), cell.getColumnIndex());
                    }
                    firstRow = false;
                    continue;
                }
                
                Etudiant etudiant = new Etudiant();
                
                etudiant.setCne(getCellValueAsString(row.getCell(columnIndex.getOrDefault("CNE", 0))));
                etudiant.setNom(getCellValueAsString(row.getCell(columnIndex.getOrDefault("NOM", 1))));
                etudiant.setPrenom(getCellValueAsString(row.getCell(columnIndex.getOrDefault("PRENOM", 2))));
                etudiant.setEmailPersonnel(getCellValueAsString(row.getCell(columnIndex.getOrDefault("EMAIL PERSONNEL", 3))));
                etudiant.setEmailAcademique(getCellValueAsString(row.getCell(columnIndex.getOrDefault("EMAIL ACADEMIQUE", 4))));
                etudiant.setFiliere(getCellValueAsString(row.getCell(columnIndex.getOrDefault("FILIERE", 5))));
                etudiant.setEncadrantNom(getCellValueAsString(row.getCell(columnIndex.getOrDefault("ENCADRANT NOM", 6))));
                etudiant.setEncadrantPrenom(getCellValueAsString(row.getCell(columnIndex.getOrDefault("ENCADRANT PRENOM", 7))));
                
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
                professeur.setNom(getCellValueAsString(row.getCell(0)));
                professeur.setPrenom(getCellValueAsString(row.getCell(1)));
                professeur.setSpecialite(getCellValueAsString(row.getCell(2)));
                
                professeurs.add(professeur);
            }
        }
        
        return professeurs;
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue();
                return value != null ? value.trim() : "";
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
}