package com.ensa.service.impl;

import com.ensa.service.interfaces.StatistiqueService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class StatistiqueServiceImpl implements StatistiqueService {
    
    @Override
    public int getTotalEtudiants(String planningPath) throws Exception {
        Set<String> etudiants = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String etudiant = getCellValue(row.getCell(4));
                if (etudiant != null && !etudiant.isEmpty()) {
                    etudiants.add(etudiant);
                }
            }
        }
        return etudiants.size();
    }
    
    @Override
    public int getTotalSoutenances(String planningPath) throws Exception {
        int count = 0;
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String num = getCellValue(row.getCell(0));
                if (num != null && !num.isEmpty() && !num.equals("N°")) {
                    count++;
                }
            }
        }
        return count;
    }
    
    @Override
    public int getTotalEncadrants(String planningPath) throws Exception {
        Set<String> encadrants = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String encadrant = getCellValue(row.getCell(3));
                if (encadrant != null && !encadrant.isEmpty()) {
                    encadrants.add(encadrant);
                }
            }
        }
        return encadrants.size();
    }
    
    @Override
    public int getTotalSalles(String planningPath) throws Exception {
        Set<String> salles = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String salle = getCellValue(row.getCell(2));
                if (salle != null && !salle.isEmpty()) {
                    salles.add(salle);
                }
            }
        }
        return salles.size();
    }
    
    @Override
    public int getTotalMembresJury(String planningPath) throws Exception {
        Set<String> membres = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String encadrant = getCellValue(row.getCell(3));
                String jury1 = getCellValue(row.getCell(5));
                String jury2 = getCellValue(row.getCell(6));
                if (encadrant != null && !encadrant.isEmpty()) membres.add(encadrant);
                if (jury1 != null && !jury1.isEmpty()) membres.add(jury1);
                if (jury2 != null && !jury2.isEmpty()) membres.add(jury2);
            }
        }
        return membres.size();
    }
    
    @Override
    public Map<String, Integer> getRepartitionParFiliere(String planningPath) throws Exception {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String filiere = getCellValue(row.getCell(7));
                if (filiere != null && !filiere.isEmpty()) {
                    result.put(filiere, result.getOrDefault(filiere, 0) + 1);
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<String, Integer> getSoutenancesParJour(String planningPath) throws Exception {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String date = getCellValue(row.getCell(1));
                if (date != null && !date.isEmpty()) {
                    result.put(date, result.getOrDefault(date, 0) + 1);
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<String, Integer> getSoutenancesParSalle(String planningPath) throws Exception {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String salle = getCellValue(row.getCell(2));
                if (salle != null && !salle.isEmpty()) {
                    result.put(salle, result.getOrDefault(salle, 0) + 1);
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<String, Integer> getEtudiantsParEncadrant(String planningPath, int limit) throws Exception {
        Map<String, Set<String>> encadrantEtudiants = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String encadrant = getCellValue(row.getCell(3));
                String etudiant = getCellValue(row.getCell(4));
                if (encadrant != null && !encadrant.isEmpty() && etudiant != null && !etudiant.isEmpty()) {
                    encadrantEtudiants.computeIfAbsent(encadrant, k -> new HashSet<>()).add(etudiant);
                }
            }
        }
        
        Map<String, Integer> result = new LinkedHashMap<>();
        encadrantEtudiants.entrySet().stream()
            .sorted((a, b) -> b.getValue().size() - a.getValue().size())
            .limit(limit)
            .forEach(e -> result.put(e.getKey(), e.getValue().size()));
        
        return result;
    }
    
    @Override
    public Map<String, Integer> getParticipationsParSpecialite(String planningPath) throws Exception {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("INFO", 0);
        result.put("NON-INFO", 0);
        
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> participations = new HashMap<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String encadrant = getCellValue(row.getCell(3));
                String jury1 = getCellValue(row.getCell(5));
                String jury2 = getCellValue(row.getCell(6));
                
                if (encadrant != null && !encadrant.isEmpty()) {
                    participations.put(encadrant, participations.getOrDefault(encadrant, 0) + 1);
                }
                if (jury1 != null && !jury1.isEmpty()) {
                    participations.put(jury1, participations.getOrDefault(jury1, 0) + 1);
                }
                if (jury2 != null && !jury2.isEmpty()) {
                    participations.put(jury2, participations.getOrDefault(jury2, 0) + 1);
                }
            }
            
            for (Map.Entry<String, Integer> entry : participations.entrySet()) {
                String nom = entry.getKey().toLowerCase();
                if (nom.contains("info") || nom.contains("informatique")) {
                    result.put("INFO", result.get("INFO") + entry.getValue());
                } else {
                    result.put("NON-INFO", result.get("NON-INFO") + entry.getValue());
                }
            }
        }
        return result;
    }
    
    @Override
    public double getChargeMoyenneParProfesseur(String planningPath) throws Exception {
        Map<String, Integer> participations = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String encadrant = getCellValue(row.getCell(3));
                String jury1 = getCellValue(row.getCell(5));
                String jury2 = getCellValue(row.getCell(6));
                
                if (encadrant != null && !encadrant.isEmpty()) {
                    participations.put(encadrant, participations.getOrDefault(encadrant, 0) + 1);
                }
                if (jury1 != null && !jury1.isEmpty()) {
                    participations.put(jury1, participations.getOrDefault(jury1, 0) + 1);
                }
                if (jury2 != null && !jury2.isEmpty()) {
                    participations.put(jury2, participations.getOrDefault(jury2, 0) + 1);
                }
            }
        }
        
        int total = participations.values().stream().mapToInt(Integer::intValue).sum();
        int nbProfesseurs = participations.size();
        
        return nbProfesseurs > 0 ? (double) total / nbProfesseurs : 0;
    }
    
    @Override
    public int getTotalJours(String planningPath) throws Exception {
        Set<String> jours = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String date = getCellValue(row.getCell(1));
                if (date != null && !date.isEmpty()) {
                    jours.add(date);
                }
            }
        }
        return jours.size();
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return new java.text.SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
            }
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return "";
    }
}