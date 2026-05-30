package com.ensa.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/telecharger-exemple")
public class TelechargerExempleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String type = request.getParameter("type");

        if ("affectation".equals(type)) {
            genererExempleAffectation(response);
        } else if ("planning".equals(type)) {
            genererExempleConfiguration(response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void genererExempleAffectation(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"exemple_etudiants_professeurs.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {

            // Feuille 1: Étudiants
            Sheet sheetEtudiants = workbook.createSheet("Etudiants");
            Row headerEtudiants = sheetEtudiants.createRow(0);
            String[] colsEtudiants = {"CNE", "NOM", "PRENOM", "EMAIL PERSONNEL", "EMAIL ACADEMIQUE", "FILIERE"};
            for (int i = 0; i < colsEtudiants.length; i++) {
                Cell cell = headerEtudiants.createCell(i);
                cell.setCellValue(colsEtudiants[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            // Ligne d'exemple
            Row exempleEtudiant = sheetEtudiants.createRow(1);
            exempleEtudiant.createCell(0).setCellValue("H123456789");
            exempleEtudiant.createCell(1).setCellValue("BENALI");
            exempleEtudiant.createCell(2).setCellValue("Mohamed");
            exempleEtudiant.createCell(3).setCellValue("mohamed@example.com");
            exempleEtudiant.createCell(4).setCellValue("mohamed.benali@etu.uae.ac.ma");
            exempleEtudiant.createCell(5).setCellValue("INFO");

            // Ajuster les colonnes
            for (int i = 0; i < colsEtudiants.length; i++) {
                sheetEtudiants.autoSizeColumn(i);
            }

            // Feuille 2: Professeurs
            Sheet sheetProfesseurs = workbook.createSheet("Professeurs");
            Row headerProfesseurs = sheetProfesseurs.createRow(0);
            String[] colsProfesseurs = {"NOM", "PRENOM", "SPECIALITE"};
            for (int i = 0; i < colsProfesseurs.length; i++) {
                Cell cell = headerProfesseurs.createCell(i);
                cell.setCellValue(colsProfesseurs[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            // Ligne d'exemple
            Row exempleProfesseur = sheetProfesseurs.createRow(1);
            exempleProfesseur.createCell(0).setCellValue("EL HASSANI");
            exempleProfesseur.createCell(1).setCellValue("Ali");
            exempleProfesseur.createCell(2).setCellValue("Informatique");

            for (int i = 0; i < colsProfesseurs.length; i++) {
                sheetProfesseurs.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    private void genererExempleConfiguration(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"exemple_configuration.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Configuration");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Parametre");
            header.createCell(1).setCellValue("Valeur");
            header.getCell(0).setCellStyle(getHeaderStyle(workbook));
            header.getCell(1).setCellStyle(getHeaderStyle(workbook));

            String[][] configs = {
                {"duree_soutenance_minutes", "60"},
                {"heure_debut_matin", "09:00"},
                {"heure_fin_matin", "12:00"},
                {"heure_debut_apres_midi", "14:00"},
                {"heure_fin_apres_midi", "18:00"},
                {"pause_max_sans_soutenance_heures", "1"},
                {"salles", "salle 16, salle 17, salle 18"},
                {"date_debut_soutenance", "22/06/2026"},
                {"jours", "3"},
                {"InfoProf_min", "2"}
            };

            for (int i = 0; i < configs.length; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(configs[i][0]);
                row.createCell(1).setCellValue(configs[i][1]);
            }

            for (int i = 0; i < 2; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}