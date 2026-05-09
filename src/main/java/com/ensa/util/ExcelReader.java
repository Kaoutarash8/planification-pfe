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
    
    /**
     * Lit le fichier planning Excel et retourne la liste des encadrants avec leurs etudiants
     * Structure du planning: 
     * - Ligne 0-3: Titres
     * - Ligne 4: En-tetes (N°, Date, Heure, Salle, Encadrant, Jury 1, Jury 2, Nom Etudiant, Prenom Etudiant, Filiere)
     * - Ligne 5+: Donnees
     */
    public static Map<String, List<Map<String, String>>> lirePlanning(String filePath) throws Exception {
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new Exception("Sheet introuvable");
            }
            
            // Les donnees commencent a la ligne 5 (index 4)
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Verifier si la ligne est vide
                String numero = getCellValueAsString(row.getCell(0));
                if (numero == null || numero.trim().isEmpty()) continue;
                
                // Colonnes selon votre fichier planning
                String encadrant = getCellValueAsString(row.getCell(4));  // Colonne E
                String nomEtudiant = getCellValueAsString(row.getCell(7));   // Colonne H
                String prenomEtudiant = getCellValueAsString(row.getCell(8)); // Colonne I
                String filiere = getCellValueAsString(row.getCell(9));        // Colonne J
                
                if (encadrant == null || encadrant.trim().isEmpty()) continue;
                
                encadrant = encadrant.trim();
                nomEtudiant = nomEtudiant != null ? nomEtudiant.trim() : "";
                prenomEtudiant = prenomEtudiant != null ? prenomEtudiant.trim() : "";
                filiere = filiere != null ? filiere.trim() : "";
                
                Map<String, String> etu = new HashMap<>();
                etu.put("nom", nomEtudiant);
                etu.put("prenom", prenomEtudiant);
                etu.put("filiere", filiere);
                
                result.computeIfAbsent(encadrant, k -> new ArrayList<>()).add(etu);
            }
        }
        
        return result;
    }
    
    /**
     * Version simplifiee pour obtenir les encadrants avec leurs etudiants
     */
    public static Map<String, List<Map<String, String>>> getEncadrantsAvecEtudiants(String planningPath) throws Exception {
        return lirePlanning(planningPath);
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
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ex) {
                        return "";
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}