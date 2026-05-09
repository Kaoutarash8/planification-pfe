package com.ensa.util;

import com.ensa.model.PlanningSoutenance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.util.*;

public class PlanningExcelWriter {
    
    public static String sauvegarderPlanning(List<PlanningSoutenance> planning, String uploadPath, String annee) throws Exception {
        String fileName = "planning_" + annee + ".xlsx";
        String fullPath = uploadPath + "/" + fileName;
        
        // Extraire les valeurs uniques pour les couleurs
        Set<String> professeursUniques = new LinkedHashSet<>();
        Set<String> heuresUniques = new LinkedHashSet<>();
        Set<String> sallesUniques = new LinkedHashSet<>();
        Set<String> datesUniques = new LinkedHashSet<>();
        Set<String> filieresUniques = new LinkedHashSet<>();
        
        for (PlanningSoutenance ps : planning) {
            professeursUniques.add(ps.getEncadrant());
            professeursUniques.add(ps.getJury1());
            professeursUniques.add(ps.getJury2());
            heuresUniques.add(ps.getHeure());
            sallesUniques.add(ps.getSalle());
            datesUniques.add(ps.getDate());
            filieresUniques.add(ps.getFiliere());
        }
        
        // Initialiser les couleurs
        ColorUtilPlanning.initialiserCouleurs(professeursUniques, heuresUniques, sallesUniques, datesUniques, filieresUniques);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Planning Soutenances PFE");
            
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // En-tête
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PLANNING DES SOUTENANCES PFE");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            
            Row anneeRow = sheet.createRow(1);
            Cell anneeCell = anneeRow.createCell(0);
            anneeCell.setCellValue("Annee Universitaire : " + annee.replace("_", "/"));
            anneeCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));
            
            // En-têtes colonnes
            Row headerRow = sheet.createRow(3);
            String[] headers = {"N°", "Date", "Heure", "Salle", "Encadrant", "Jury 1", "Jury 2", "Nom Etudiant", "Prenom Etudiant", "Filiere"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Données avec couleurs
            int rowNum = 4;
            int numero = 1;
            for (PlanningSoutenance ps : planning) {
                Row row = sheet.createRow(rowNum++);
                
                // N°
                Cell cellNum = row.createCell(0);
                cellNum.setCellValue(numero++);
                cellNum.setCellStyle(dataStyle);
                
                // Date (avec couleur)
                Cell cellDate = row.createCell(1);
                cellDate.setCellValue(ps.getDate());
                short couleurDate = ColorUtilPlanning.getCouleurDate(ps.getDate());
                applyCellStyle(cellDate, couleurDate, workbook, dataStyle);
                
                // Heure (avec couleur)
                Cell cellHeure = row.createCell(2);
                cellHeure.setCellValue(ps.getHeure());
                short couleurHeure = ColorUtilPlanning.getCouleurHeure(ps.getHeure());
                applyCellStyle(cellHeure, couleurHeure, workbook, dataStyle);
                
                // Salle (avec couleur)
                Cell cellSalle = row.createCell(3);
                cellSalle.setCellValue(ps.getSalle());
                short couleurSalle = ColorUtilPlanning.getCouleurSalle(ps.getSalle());
                applyCellStyle(cellSalle, couleurSalle, workbook, dataStyle);
                
                // Encadrant (avec couleur)
                Cell cellEncadrant = row.createCell(4);
                cellEncadrant.setCellValue(ps.getEncadrant());
                short couleurEncadrant = ColorUtilPlanning.getCouleurProfesseur(ps.getEncadrant());
                applyCellStyle(cellEncadrant, couleurEncadrant, workbook, dataStyle);
                
                // Jury 1 (avec couleur)
                Cell cellJury1 = row.createCell(5);
                cellJury1.setCellValue(ps.getJury1());
                short couleurJury1 = ColorUtilPlanning.getCouleurProfesseur(ps.getJury1());
                applyCellStyle(cellJury1, couleurJury1, workbook, dataStyle);
                
                // Jury 2 (avec couleur)
                Cell cellJury2 = row.createCell(6);
                cellJury2.setCellValue(ps.getJury2());
                short couleurJury2 = ColorUtilPlanning.getCouleurProfesseur(ps.getJury2());
                applyCellStyle(cellJury2, couleurJury2, workbook, dataStyle);
                
                // Nom Etudiant
                Cell cellNom = row.createCell(7);
                cellNom.setCellValue(ps.getEtudiantNom());
                cellNom.setCellStyle(dataStyle);
                
                // Prenom Etudiant
                Cell cellPrenom = row.createCell(8);
                cellPrenom.setCellValue(ps.getEtudiantPrenom());
                cellPrenom.setCellStyle(dataStyle);
                
                // Filiere (avec couleur)
                Cell cellFiliere = row.createCell(9);
                cellFiliere.setCellValue(ps.getFiliere());
                short couleurFiliere = ColorUtilPlanning.getCouleurFiliere(ps.getFiliere());
                applyCellStyle(cellFiliere, couleurFiliere, workbook, dataStyle);
            }
            
            // Ajuster largeur colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 500, 8000));
            }
            
            try (FileOutputStream fos = new FileOutputStream(fullPath)) {
                workbook.write(fos);
            }
        }
        
        System.out.println("✅ Planning généré: " + fullPath);
        return fileName;
    }
    
    private static void applyCellStyle(Cell cell, short couleur, Workbook workbook, CellStyle baseStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        style.setFillForegroundColor(couleur);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }
    
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
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
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}