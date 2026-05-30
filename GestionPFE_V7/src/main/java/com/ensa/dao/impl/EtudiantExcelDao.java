package com.ensa.dao.impl;

import com.ensa.dao.interfaces.EtudiantDao;
import com.ensa.model.Etudiant;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class EtudiantExcelDao implements EtudiantDao {
    
    @Override
    public List<Etudiant> lireEtudiants(String filePath) throws Exception {
        List<Etudiant> etudiants = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.err.println("Sheet not found");
                return etudiants;
            }
            
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
                
                String cne = getCellValue(row.getCell(columnIndex.getOrDefault("CNE", 0)));
                String nom = getCellValue(row.getCell(columnIndex.getOrDefault("NOM", 1)));
                String prenom = getCellValue(row.getCell(columnIndex.getOrDefault("PRENOM", 2)));
                String emailPersonnel = getCellValue(row.getCell(columnIndex.getOrDefault("EMAIL PERSONNEL", 3)));
                String emailAcademique = getCellValue(row.getCell(columnIndex.getOrDefault("EMAIL ACADEMIQUE", 4)));
                String filiere = getCellValue(row.getCell(columnIndex.getOrDefault("FILIERE", 5)));
                String encadrantNom = getCellValue(row.getCell(columnIndex.getOrDefault("ENCADRANT NOM", 6)));
                String encadrantPrenom = getCellValue(row.getCell(columnIndex.getOrDefault("ENCADRANT PRENOM", 7)));
                
                if (!nom.isEmpty() && !prenom.isEmpty()) {
                    Etudiant e = new Etudiant();
                    e.setCne(cne);
                    e.setNom(nom.toUpperCase());  // NOM en majuscules
                    e.setPrenom(prenom);           // Prénom normal
                    e.setEmailPersonnel(emailPersonnel);
                    e.setEmailAcademique(emailAcademique);
                    e.setFiliere(filiere);
                    e.setEncadrantNom(encadrantNom);
                    e.setEncadrantPrenom(encadrantPrenom);
                    
                    etudiants.add(e);
                }
            }
        }
        
        System.out.println("Total etudiants lus: " + etudiants.size());
        return etudiants;
    }
    
    /**
     * Retourne le nom complet formaté: "Prénom NOM"
     */
    public static String getNomCompletEtudiant(Etudiant e) {
        if (e == null) return "";
        return e.getPrenom() + " " + e.getNom().toUpperCase();
    }
    
    /**
     * Lit l'affectation et retourne une Map: Encadrant -> Liste des noms complets des étudiants
     */
    public Map<String, List<String>> lireAffectation(String filePath) throws Exception {
        Map<String, List<String>> affectations = new LinkedHashMap<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            int headerRowIndex = -1;
            
            for (Row row : sheet) {
                Cell firstCell = row.getCell(0);
                if (firstCell != null) {
                    String value = firstCell.toString().trim();
                    if (value.contains("ENCADRANT")) {
                        headerRowIndex = row.getRowNum();
                        break;
                    }
                }
            }
            
            if (headerRowIndex == -1) {
                throw new Exception("Header ENCADRANT introuvable");
            }
            
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell profCell = row.getCell(0);
                if (profCell == null) continue;
                
                String encadrant = profCell.toString().trim();
                if (encadrant.isEmpty()) continue;
                
                List<String> etudiants = new ArrayList<>();
                
                for (int j = 1; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String etudiant = cell.toString().trim();
                        if (!etudiant.isEmpty()) {
                            etudiants.add(etudiant);
                        }
                    }
                }
                
                if (!etudiants.isEmpty()) {
                    affectations.put(encadrant, etudiants);
                }
            }
        }
        
        System.out.println("Total encadrants lus: " + affectations.size());
        return affectations;
    }
    
    /**
     * Lit les etudiants complets pour les filières
     */
    public List<Etudiant> lireEtudiantsComplet(String filePath) throws Exception {
        List<Etudiant> etudiants = new ArrayList<>();
        
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
                
                String cne = getCellValue(row.getCell(columnIndex.getOrDefault("CNE", 0)));
                String nom = getCellValue(row.getCell(columnIndex.getOrDefault("NOM", 1)));
                String prenom = getCellValue(row.getCell(columnIndex.getOrDefault("PRENOM", 2)));
                String filiere = getCellValue(row.getCell(columnIndex.getOrDefault("FILIERE", 5)));
                
                if (!nom.isEmpty() && !prenom.isEmpty()) {
                    Etudiant e = new Etudiant();
                    e.setCne(cne);
                    e.setNom(nom.toUpperCase());
                    e.setPrenom(prenom);
                    e.setFiliere(filiere);
                    etudiants.add(e);
                }
            }
        }
        
        System.out.println("Total etudiants complets lus: " + etudiants.size());
        return etudiants;
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