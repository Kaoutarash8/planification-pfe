package com.ensa.util;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExcelWriter {
    
    // Chemin FIXE vers votre projet
    private static final String PROJECT_PATH = "C:/Users/e/workspace-pfe/pfe-affectation";
    
    public static String sauvegarderAffectation(Map<Professeur, List<Etudiant>> affectation, 
                                                  String timestamp) throws Exception {
        // Utiliser le chemin fixe du projet
        File uploadDir = new File(PROJECT_PATH, "uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        String fileName = "affectation_" + timestamp + ".xlsx";
        File outputFile = new File(uploadDir, fileName);
        String filePath = outputFile.getAbsolutePath();
        
        System.out.println("=== SAUVEGARDE FICHIER AFFECTATION ===");
        System.out.println("Chemin complet: " + filePath);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Affectation_PFE_2024_2025");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"CNE", "NOM", "PRENOM", "EMAIL PERSONNEL", 
                               "EMAIL ACADEMIQUE", "FILIERE", "ENCADRANT NOM", 
                               "ENCADRANT PRENOM", "ENCADRANT COMPLET"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
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
                    row.createCell(8).setCellValue(etudiant.getEncadrantComplet());
                    
                    for (int i = 0; i < headers.length; i++) {
                        if (row.getCell(i) != null) {
                            row.getCell(i).setCellStyle(dataStyle);
                        }
                    }
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
        
        // Créer la copie latest
        File latestFile = new File(uploadDir, "affectation_latest.xlsx");
        try (FileInputStream fis = new FileInputStream(outputFile);
             FileOutputStream fos = new FileOutputStream(latestFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
        
        System.out.println("Fichier affectation latest: " + latestFile.getAbsolutePath());
        return filePath;
    }
    
    public static String sauvegarderStatistiques(Map<Professeur, List<Etudiant>> affectation,
                                                   Map<String, Object> statistiques,
                                                   String timestamp) throws Exception {
        File uploadDir = new File(PROJECT_PATH, "uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        String fileName = "statistiques_affectation_" + timestamp + ".xlsx";
        File outputFile = new File(uploadDir, fileName);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet resumeSheet = workbook.createSheet("Resume");
            
            Row titleRow = resumeSheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Statistiques d'affectation - PFE 2024/2025");
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            Row row1 = resumeSheet.createRow(2);
            row1.createCell(0).setCellValue("Total étudiants:");
            row1.createCell(1).setCellValue((int) statistiques.get("total"));
            
            Row row2 = resumeSheet.createRow(3);
            row2.createCell(0).setCellValue("Total professeurs:");
            row2.createCell(1).setCellValue((int) statistiques.get("nbProfesseurs"));
            
            Row row3 = resumeSheet.createRow(4);
            row3.createCell(0).setCellValue("Minimum étudiants par professeur:");
            row3.createCell(1).setCellValue((int) statistiques.get("min"));
            
            Row row4 = resumeSheet.createRow(5);
            row4.createCell(0).setCellValue("Maximum étudiants par professeur:");
            row4.createCell(1).setCellValue((int) statistiques.get("max"));
            
            Row row5 = resumeSheet.createRow(6);
            row5.createCell(0).setCellValue("Affectation équilibrée:");
            row5.createCell(1).setCellValue((boolean) statistiques.get("equilibre") ? "OUI" : "NON");
            
            resumeSheet.autoSizeColumn(0);
            resumeSheet.autoSizeColumn(1);
            
            Sheet detailSheet = workbook.createSheet("Detail_par_professeur");
            
            Row headerRow = detailSheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            String[] headers = {"Professeur", "Spécialité", "Nombre d'étudiants", "Liste des étudiants"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowIdx = 1;
            for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();
                
                Row row = detailSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(prof.getNomComplet());
                row.createCell(1).setCellValue(prof.getSpecialite());
                row.createCell(2).setCellValue(etudiants.size());
                
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < etudiants.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(etudiants.get(i).getPrenom()).append(" ").append(etudiants.get(i).getNom());
                }
                row.createCell(3).setCellValue(sb.toString());
            }
            
            for (int i = 0; i < headers.length; i++) {
                detailSheet.autoSizeColumn(i);
            }
            
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
        
        return outputFile.getAbsolutePath();
    }
}