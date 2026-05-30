package com.ensa.util;

import com.ensa.model.PlanningSoutenance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class ExcelReaderPlanning {

    public static List<PlanningSoutenance> lirePlanning(String filePath) throws Exception {
        List<PlanningSoutenance> planning = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new Exception("Sheet not found");
            }
            
            // Detection des colonnes
            int colEncadrant = -1, colJury1 = -1, colJury2 = -1;
            int colDate = -1, colHeure = -1, colSalle = -1;
            int colEtudiant = -1, colFiliere = -1;
            int colNumero = -1;
            
            for (int rowIdx = 0; rowIdx <= 5; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;
                for (Cell cell : row) {
                    String val = getCellValue(cell);
                    if (val == null) continue;
                    if (val.contains("Encadrant")) colEncadrant = cell.getColumnIndex();
                    if (val.contains("Jury 1")) colJury1 = cell.getColumnIndex();
                    if (val.contains("Jury 2")) colJury2 = cell.getColumnIndex();
                    if (val.contains("Date")) colDate = cell.getColumnIndex();
                    if (val.contains("Heure")) colHeure = cell.getColumnIndex();
                    if (val.contains("Salle")) colSalle = cell.getColumnIndex();
                    if (val.contains("Etudiant") || val.contains("Nom Etudiant")) colEtudiant = cell.getColumnIndex();
                    if (val.contains("Filiere")) colFiliere = cell.getColumnIndex();
                    if (val.contains("N°")) colNumero = cell.getColumnIndex();
                }
                if (colEncadrant != -1) break;
            }
            
            // Valeurs par defaut
            if (colEncadrant == -1) colEncadrant = 4;
            if (colJury1 == -1) colJury1 = 5;
            if (colJury2 == -1) colJury2 = 6;
            if (colDate == -1) colDate = 1;
            if (colHeure == -1) colHeure = 2;
            if (colSalle == -1) colSalle = 3;
            if (colEtudiant == -1) colEtudiant = 7;
            if (colFiliere == -1) colFiliere = 8;
            
            // Lecture des donnees
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String numero = getCellValue(row.getCell(colNumero));
                if (numero == null || numero.trim().isEmpty()) continue;
                
                PlanningSoutenance ps = new PlanningSoutenance();
                ps.setEncadrant(getCellValue(row.getCell(colEncadrant)));
                ps.setJury1(getCellValue(row.getCell(colJury1)));
                ps.setJury2(getCellValue(row.getCell(colJury2)));
                ps.setDate(getCellValue(row.getCell(colDate)));
                ps.setHeure(getCellValue(row.getCell(colHeure)));
                ps.setSalle(getCellValue(row.getCell(colSalle)));
                
                String etudiant = getCellValue(row.getCell(colEtudiant));
                ps.setEtudiantComplet(etudiant);
                ps.setFiliere(getCellValue(row.getCell(colFiliere)));
                
                if (ps.getEncadrant() != null && !ps.getEncadrant().isEmpty()) {
                    planning.add(ps);
                }
            }
        }
        
        return planning;
    }
    
    public static Map<String, Object> getStatistiques(String filePath) throws Exception {
        List<PlanningSoutenance> planning = lirePlanning(filePath);
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("totalSoutenances", planning.size());
        stats.put("totalEtudiants", planning.stream().map(p -> p.getEtudiantComplet()).distinct().count());
        
        // Soutenances par filiere
        Map<String, Integer> parFiliere = new LinkedHashMap<>();
        for (PlanningSoutenance p : planning) {
            String filiere = (p.getFiliere() == null || p.getFiliere().isEmpty()) ? "Sans filiere" : p.getFiliere();
            parFiliere.merge(filiere, 1, Integer::sum);
        }
        stats.put("soutenancesParFiliere", parFiliere);
        
        // Etudiants par filiere
        Map<String, Set<String>> etudiantsParFiliere = new HashMap<>();
        for (PlanningSoutenance p : planning) {
            String filiere = (p.getFiliere() == null || p.getFiliere().isEmpty()) ? "Sans filiere" : p.getFiliere();
            etudiantsParFiliere.computeIfAbsent(filiere, k -> new HashSet<>()).add(p.getEtudiantComplet());
        }
        Map<String, Integer> etudiantsParFiliereCount = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : etudiantsParFiliere.entrySet()) {
            etudiantsParFiliereCount.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("etudiantsParFiliere", etudiantsParFiliereCount);
        
        // Soutenances par date
        Map<String, Integer> parDate = new LinkedHashMap<>();
        for (PlanningSoutenance p : planning) {
            if (p.getDate() != null) parDate.merge(p.getDate(), 1, Integer::sum);
        }
        stats.put("soutenancesParDate", parDate);
        stats.put("moyenneParJour", parDate.values().stream().mapToInt(Integer::intValue).average().orElse(0));
        
        // Soutenances par professeur
        Map<String, Integer> parProfesseur = new HashMap<>();
        for (PlanningSoutenance p : planning) {
            if (p.getEncadrant() != null) parProfesseur.merge(p.getEncadrant(), 1, Integer::sum);
            if (p.getJury1() != null) parProfesseur.merge(p.getJury1(), 1, Integer::sum);
            if (p.getJury2() != null) parProfesseur.merge(p.getJury2(), 1, Integer::sum);
        }
        List<Map<String, Object>> profs = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : parProfesseur.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())).toList()) {
            Map<String, Object> prof = new LinkedHashMap<>();
            prof.put("nom", entry.getKey());
            prof.put("total", entry.getValue());
            profs.add(prof);
        }
        stats.put("soutenancesParProfesseur", profs);
        
        // Repartition filieres par jour
        Map<String, Map<String, Integer>> repartitionJour = new LinkedHashMap<>();
        for (PlanningSoutenance p : planning) {
            String date = p.getDate();
            String filiere = (p.getFiliere() == null || p.getFiliere().isEmpty()) ? "Sans filiere" : p.getFiliere();
            repartitionJour.computeIfAbsent(date, k -> new HashMap<>())
                          .merge(filiere, 1, Integer::sum);
        }
        stats.put("repartitionFilieresParJour", repartitionJour);
        
        // Salle la plus utilisee
        Map<String, Integer> parSalle = new HashMap<>();
        for (PlanningSoutenance p : planning) {
            if (p.getSalle() != null) parSalle.merge(p.getSalle(), 1, Integer::sum);
        }
        stats.put("salleLaPlusUtilisee", parSalle.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Aucune"));
        
        // Encadrant plus sollicite
        Map<String, Integer> parEncadrant = new HashMap<>();
        for (PlanningSoutenance p : planning) {
            if (p.getEncadrant() != null) parEncadrant.merge(p.getEncadrant(), 1, Integer::sum);
        }
        stats.put("encadrantPlusSoutenu", parEncadrant.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Aucun"));
        
        // Jury plus sollicite
        Map<String, Integer> parJury = new HashMap<>();
        for (PlanningSoutenance p : planning) {
            if (p.getJury1() != null) parJury.merge(p.getJury1(), 1, Integer::sum);
            if (p.getJury2() != null) parJury.merge(p.getJury2(), 1, Integer::sum);
        }
        stats.put("juryPlusSollicite", parJury.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Aucun"));
        
        // Jour plus charge
        stats.put("jourPlusCharge", parDate.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Aucun"));
        
        // Total professeurs
        Set<String> profsSet = new HashSet<>();
        for (PlanningSoutenance p : planning) {
            if (p.getEncadrant() != null) profsSet.add(p.getEncadrant());
            if (p.getJury1() != null) profsSet.add(p.getJury1());
            if (p.getJury2() != null) profsSet.add(p.getJury2());
        }
        stats.put("totalProfesseurs", profsSet.size());
        
        return stats;
    }
    
    private static String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return null;
        }
    }
}