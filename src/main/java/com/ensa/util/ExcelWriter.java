package com.ensa.util;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.Affectation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.util.*;

public class ExcelWriter {
    
    public static String sauvegarderExcel(Affectation affectation) throws Exception {
        String uploadPath = FileUtil.getUploadPath();
        if (uploadPath == null) {
            throw new Exception("Le chemin d'upload n'est pas initialise");
        }
        
        String annee = FileUtil.getAnneeUniversitaire();
        String fileName = "affectation_" + annee + ".xlsx";
        String absolutePath = uploadPath + "/" + fileName;
        
        String anneeAffichage = annee.replace("_", "/");
        
        Set<String> filieresUniques = new LinkedHashSet<>();
        for (List<Etudiant> etudiants : affectation.getAffectations().values()) {
            for (Etudiant e : etudiants) {
                String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "Sans filiere" : e.getFiliere();
                filieresUniques.add(filiere);
            }
        }
        
        ColorUtil.initialiserCouleurs(filieresUniques);
        
        System.out.println("\n=== COLORATION DES FILIERES ===");
        for (String filiere : filieresUniques) {
            System.out.println("  " + filiere + " -> " + ColorUtil.getNomCouleur(filiere));
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Affectation PFE");
            
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int maxEtudiants = affectation.getAffectations().values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
            
            int totalColonnes = maxEtudiants + 1;
            
            Row ecoleRow = sheet.createRow(0);
            Cell ecoleCell = ecoleRow.createCell(0);
            ecoleCell.setCellValue("ECOLE NATIONALE DES SCIENCES APPLIQUEES - AL HOCEIMA");
            ecoleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalColonnes - 1));
            
            Row deptRow = sheet.createRow(1);
            Cell deptCell = deptRow.createCell(0);
            deptCell.setCellValue("Departement Mathematiques et Informatique");
            deptCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, totalColonnes - 1));
            
            Row titreRow = sheet.createRow(2);
            Cell titreCell = titreRow.createCell(0);
            titreCell.setCellValue("Affectation des encadrants de Projet de Fin d'Etude");
            titreCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, totalColonnes - 1));
            
            Row anneeRow = sheet.createRow(3);
            Cell anneeCell = anneeRow.createCell(0);
            anneeCell.setCellValue("Annee Universitaire : " + anneeAffichage);
            anneeCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, totalColonnes - 1));
            
            sheet.createRow(4);
            
            Map<String, Short> legendes = ColorUtil.getLegendeCouleurs();
            int legendRowNum = 5;
            for (Map.Entry<String, Short> entry : legendes.entrySet()) {
                Row legendRow = sheet.createRow(legendRowNum++);
                Cell legendCell = legendRow.createCell(0);
                legendCell.setCellValue(entry.getKey());

                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(entry.getValue());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderTop(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);
                style.setAlignment(HorizontalAlignment.LEFT);
                style.setVerticalAlignment(VerticalAlignment.CENTER);

                Font font = workbook.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLACK.getIndex());
                style.setFont(font);

                legendCell.setCellStyle(style);
            }
            
            sheet.createRow(legendRowNum);
            
            int tableauStartRow = legendRowNum + 1;
            
            Row headerRow = sheet.createRow(tableauStartRow);
            Cell encadrantHeader = headerRow.createCell(0);
            encadrantHeader.setCellValue("ENCADRANT (NOM Prenom)");
            encadrantHeader.setCellStyle(headerStyle);
            
            for (int i = 1; i <= maxEtudiants; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue("ETUDIANT " + i);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = tableauStartRow + 1;
            for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.getAffectations().entrySet()) {
                Professeur prof = entry.getKey();
                List<Etudiant> etudiants = entry.getValue();
                
                Row row = sheet.createRow(rowNum++);
                
                String encadrant = prof.getNom().toUpperCase() + " " + prof.getPrenom();
                Cell encCell = row.createCell(0);
                encCell.setCellValue(encadrant);
                encCell.setCellStyle(dataStyle);
                
                for (int i = 0; i < maxEtudiants; i++) {
                    Cell cell = row.createCell(i + 1);
                    
                    if (i < etudiants.size()) {
                        Etudiant e = etudiants.get(i);
                        String etudiantNom = e.getPrenom() + " " + e.getNom().toUpperCase();
                        cell.setCellValue(etudiantNom);
                        
                        String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "Sans filiere" : e.getFiliere();
                        short couleur = ColorUtil.getCouleurExcel(filiere);
                        
                        CellStyle coloredStyle = workbook.createCellStyle();
                        coloredStyle.cloneStyleFrom(dataStyle);
                        coloredStyle.setFillForegroundColor(couleur);
                        coloredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        
                        Font font = workbook.createFont();
                        font.setBold(true);
                        coloredStyle.setFont(font);
                        
                        cell.setCellStyle(coloredStyle);
                    } else {
                        cell.setCellValue("");
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
            
            for (int i = 0; i <= maxEtudiants; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 500, 8000));
            }
            
            try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
                workbook.write(fos);
            }
        }
        
        System.out.println("\nExcel genere: " + absolutePath);
        return absolutePath;
    }
    
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}