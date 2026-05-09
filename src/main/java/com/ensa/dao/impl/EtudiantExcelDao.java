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
                    e.setNom(nom);  // Garde le nom complet (peut contenir plusieurs mots)
                    e.setPrenom(prenom);  // Garde le prénom complet (peut contenir plusieurs mots)
                    e.setEmailPersonnel(emailPersonnel);
                    e.setEmailAcademique(emailAcademique);
                    e.setFiliere(filiere);
                    e.setEncadrantNom(encadrantNom);
                    e.setEncadrantPrenom(encadrantPrenom);
                    
                    etudiants.add(e);
                    System.out.println("Etudiant lu: " + prenom + " " + nom + " - Filiere: " + filiere);
                }
            }
        }
        
        System.out.println("Total etudiants lus: " + etudiants.size());
        return etudiants;
    }
    
    /**
     * Lire l'affectation depuis le fichier Excel genere
     * Cette methode cherche dynamiquement la ligne contenant "ENCADRANT"
     */
    public Map<String, List<String>> lireAffectation(String filePath) throws Exception {
        Map<String, List<String>> affectations = new LinkedHashMap<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            int headerRowIndex = -1;
            
            // Chercher la ligne contenant "ENCADRANT"
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
                throw new Exception("Header ENCADRANT introuvable dans le fichier");
            }
            
            System.out.println("Header ENCADRANT trouve a la ligne: " + headerRowIndex);
            
            // Lire les donnees apres l'en-tete
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell profCell = row.getCell(0);
                if (profCell == null) continue;
                
                String encadrant = profCell.toString().trim();
                if (encadrant.isEmpty()) continue;
                
                List<String> etudiants = new ArrayList<>();
                
                // Parcourir toutes les colonnes d'etudiants
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
                    System.out.println(encadrant + " -> " + etudiants.size() + " etudiants");
                }
            }
        }
        
        System.out.println("Total encadrants lus: " + affectations.size());
        return affectations;
    }
    
    /**
     * Lire les etudiants complets (avec filieres) depuis le fichier etudiants.xlsx
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
                    e.setNom(nom);      // Nom complet
                    e.setPrenom(prenom); // Prénom complet
                    e.setFiliere(filiere);
                    etudiants.add(e);
                    System.out.println("Etudiant complet lu: " + prenom + " " + nom + " - Filiere: " + filiere);
                }
            }
        }
        
        System.out.println("Total etudiants complets lus: " + etudiants.size());
        return etudiants;
    }
    
    /**
     * Extrait le prénom et le nom depuis un nom complet "Prénom Nom"
     * Gère correctement les noms composés comme "EL ALAOUI", "Mohamed Reda"
     */
    public static String[] extrairePrenomNom(String nomComplet) {
        if (nomComplet == null || nomComplet.isEmpty()) {
            return new String[]{"", ""};
        }
        
        String[] parts = nomComplet.trim().split(" ");
        
        if (parts.length == 1) {
            // Un seul mot -> considéré comme prénom
            return new String[]{parts[0], ""};
        }
        
        // Premier mot = prénom, le reste = nom
        String prenom = parts[0];
        StringBuilder nom = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) nom.append(" ");
            nom.append(parts[i]);
        }
        
        return new String[]{prenom, nom.toString()};
    }
    
    /**
     * Extrait le nom et prénom d'un encadrant "NOM Prenom"
     * Gère correctement les noms composés
     */
    public static String[] extraireNomPrenomEncadrant(String nomComplet) {
        if (nomComplet == null || nomComplet.isEmpty()) {
            return new String[]{"", ""};
        }
        
        String[] parts = nomComplet.trim().split(" ");
        
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        
        // Premier mot = nom, le reste = prénom
        String nom = parts[0];
        StringBuilder prenom = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) prenom.append(" ");
            prenom.append(parts[i]);
        }
        
        return new String[]{nom, prenom.toString()};
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