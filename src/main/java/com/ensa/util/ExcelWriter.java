package com.ensa.util;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class ExcelWriter {
    
    public static String sauvegarderAffectation(Map<Professeur, List<Etudiant>> affectation, 
                                                  String timestamp) throws Exception {
        // Utiliser le dossier Affectation du projet
        String affectationPath = FileUploadUtil.getAffectationPath();
        File affectationDir = new File(affectationPath);
        if (!affectationDir.exists()) affectationDir.mkdirs();
        
        // Supprimer les anciens fichiers Excel
        File[] anciensFichiers = affectationDir.listFiles((dir, name) -> name.endsWith(".xlsx"));
        if (anciensFichiers != null) {
            for (File f : anciensFichiers) {
                f.delete();
                System.out.println("Ancien fichier Excel supprimé: " + f.getName());
            }
        }
        
        String fileName = "affectation_" + timestamp + ".xlsx";
        File outputFile = new File(affectationDir, fileName);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Affectation_PFE");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"CNE", "NOM", "PRENOM", "EMAIL PERSONNEL", 
                               "EMAIL ACADEMIQUE", "FILIERE", "ENCADRANT NOM", 
                               "ENCADRANT PRENOM"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            
            // GARDER L'ORDRE ORIGINAL DE L'AFFECTATION (PAS DE TRI)
            for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
                for (Etudiant etudiant : entry.getValue()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(etudiant.getCne());
                    row.createCell(1).setCellValue(etudiant.getNom());
                    row.createCell(2).setCellValue(etudiant.getPrenom());
                    row.createCell(3).setCellValue(etudiant.getEmailPersonnel());
                    row.createCell(4).setCellValue(etudiant.getEmailAcademique());
                    row.createCell(5).setCellValue(etudiant.getFiliere());
                    row.createCell(6).setCellValue(etudiant.getEncadrantNom());
                    row.createCell(7).setCellValue(etudiant.getEncadrantPrenom());
                    
                    for (int i = 0; i < headers.length; i++) {
                        if (row.getCell(i) != null) {
                            row.getCell(i).setCellStyle(dataStyle);
                        }
                    }
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }
            
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
        
        // Fichier latest pour la planification
        File latestFile = new File(affectationDir, "affectation_latest.xlsx");
        try (FileInputStream fis = new FileInputStream(outputFile);
             FileOutputStream fos = new FileOutputStream(latestFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
        
        System.out.println("Fichier Excel affectation: " + outputFile.getAbsolutePath());
        System.out.println("Fichier latest: " + latestFile.getAbsolutePath());
        
        return outputFile.getAbsolutePath();
    }
    
    public static String sauvegarderStatistiques(Map<Professeur, List<Etudiant>> affectation,
                                                   Map<String, Object> statistiques,
                                                   String timestamp) throws Exception {
        String affectationPath = FileUploadUtil.getAffectationPath();
        File affectationDir = new File(affectationPath);
        if (!affectationDir.exists()) affectationDir.mkdirs();
        
        String fileName = "statistiques_affectation_" + timestamp + ".xlsx";
        File outputFile = new File(affectationDir, fileName);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Statistiques");
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("STATISTIQUES DE L'AFFECTATION PFE");
            titleCell.setCellStyle(titleStyle);
            
            CellStyle labelStyle = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            labelStyle.setFont(labelFont);
            
            int rowNum = 2;
            addStatRow(sheet, rowNum++, "Total étudiants:", statistiques.get("total"), labelStyle);
            addStatRow(sheet, rowNum++, "Total professeurs:", statistiques.get("nbProfesseurs"), labelStyle);
            addStatRow(sheet, rowNum++, "Minimum par professeur:", statistiques.get("min"), labelStyle);
            addStatRow(sheet, rowNum++, "Maximum par professeur:", statistiques.get("max"), labelStyle);
            addStatRow(sheet, rowNum++, "Affectation équilibrée:", 
                      ((boolean)statistiques.get("equilibre") ? "OUI" : "NON"), labelStyle);
            
            for (int i = 0; i < 2; i++) {
                sheet.autoSizeColumn(i);
            }
            
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
        
        System.out.println("Fichier statistiques: " + outputFile.getAbsolutePath());
        return outputFile.getAbsolutePath();
    }
    
    private static void addStatRow(Sheet sheet, int rowNum, String label, Object value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        if (value instanceof Integer) {
            valueCell.setCellValue((Integer) value);
        } else {
            valueCell.setCellValue(String.valueOf(value));
        }
    }
}