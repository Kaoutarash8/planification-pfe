package com.ensa.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.*;

/**
 * Lit le fichier planning Excel et retourne :
 *   Map< NomEncadrant, List<InfosEtudiant> >
 *
 * Compatible avec les deux formats de planning :
 *
 *   Format A (9 colonnes) — V7 :
 *     N° | Date | Heure | Salle | Encadrant | Jury 1 | Jury 2 | Etudiant | Filiere
 *     → colonne "Etudiant" contient le nom complet "Prénom NOM"
 *
 *   Format B (10 colonnes) — V8 :
 *     N° | Date | Heure | Salle | Encadrant | Jury 1 | Jury 2 | Nom Etudiant | Prenom Etudiant | Filiere
 *     → colonnes "Nom Etudiant" et "Prenom Etudiant" séparées
 */
public class ExcelReader {

    public static Map<String, List<Map<String, String>>> getEncadrantsAvecEtudiants(String planningPath)
            throws Exception {

        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new Exception("Feuille introuvable dans le planning");

            // ── Détection automatique des colonnes ────────────────────────
            int colEncadrant    = -1;
            int colJury1        = -1;
            int colJury2        = -1;
            int colEtudiant     = -1;  // format A : nom complet
            int colNom          = -1;  // format B : nom seul
            int colPrenom       = -1;  // format B : prénom seul
            int colFiliere      = -1;
            int headerRow       = -1;

            for (int rowIdx = 0; rowIdx <= 5; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;
                for (Cell cell : row) {
                    String val = getCellStr(cell);
                    if (val == null || val.isEmpty()) continue;
                    String v = val.trim();
                    if (v.equalsIgnoreCase("Encadrant"))            colEncadrant = cell.getColumnIndex();
                    if (v.equalsIgnoreCase("Jury 1"))               colJury1     = cell.getColumnIndex();
                    if (v.equalsIgnoreCase("Jury 2"))               colJury2     = cell.getColumnIndex();
                    // Format A : colonne unique "Etudiant"
                    if (v.equalsIgnoreCase("Etudiant"))             colEtudiant  = cell.getColumnIndex();
                    // Format B : colonnes séparées
                    if (v.equalsIgnoreCase("Nom Etudiant"))         colNom       = cell.getColumnIndex();
                    if (v.equalsIgnoreCase("Prenom Etudiant"))      colPrenom    = cell.getColumnIndex();
                    if (v.equalsIgnoreCase("Filiere")
                     || v.equalsIgnoreCase("Filière"))              colFiliere   = cell.getColumnIndex();

                    if (colEncadrant != -1) headerRow = rowIdx; // ligne d'en-tête trouvée
                }
                if (colEncadrant != -1) break;
            }

            // Valeurs par défaut si en-tête non trouvé
            if (colEncadrant == -1) colEncadrant = 4;
            if (colJury1     == -1) colJury1     = 5;
            if (colJury2     == -1) colJury2     = 6;
            if (colFiliere   == -1) colFiliere   = 8; // par défaut format A (col I)

            // Détecter le format utilisé
            boolean formatA = (colEtudiant != -1 && colNom == -1);
            boolean formatB = (colNom != -1 && colPrenom != -1);

            // Si ni A ni B, essayer de deviner selon nombre de colonnes
            if (!formatA && !formatB) {
                // Vérifier la 8ème colonne : si elle existe et la 9ème aussi → format B
                Row firstData = null;
                for (int i = (headerRow + 1); i <= sheet.getLastRowNum(); i++) {
                    firstData = sheet.getRow(i);
                    if (firstData != null) break;
                }
                if (firstData != null && firstData.getLastCellNum() >= 10) {
                    // 10 colonnes → format B
                    colNom    = 7;
                    colPrenom = 8;
                    colFiliere = 9;
                    formatB = true;
                } else {
                    // 9 colonnes → format A
                    colEtudiant = 7;
                    colFiliere  = 8;
                    formatA = true;
                }
            }

            System.out.println("=== ExcelReader ===");
            System.out.println("Format détecté : " + (formatA ? "A (Etudiant complet)" : "B (Nom + Prénom séparés)"));
            System.out.println("Colonnes : Encadrant=" + colEncadrant
                    + ", Jury1=" + colJury1 + ", Jury2=" + colJury2
                    + (formatA ? ", Etudiant=" + colEtudiant : ", Nom=" + colNom + ", Prenom=" + colPrenom)
                    + ", Filiere=" + colFiliere);

            // ── Lecture des données (à partir de la ligne après l'en-tête) ─
            int startRow = (headerRow != -1) ? headerRow + 1 : 4;

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Ignorer les lignes sans numéro (lignes vides ou de titre)
                String numero = getCellStr(row.getCell(0));
                if (numero == null || numero.trim().isEmpty()) continue;

                String encadrant = getCellStr(row.getCell(colEncadrant));
                if (encadrant == null || encadrant.trim().isEmpty()) continue;

                String jury1   = nvl(getCellStr(row.getCell(colJury1)));
                String jury2   = nvl(getCellStr(row.getCell(colJury2)));
                String filiere = nvl(getCellStr(row.getCell(colFiliere)));

                String nomComplet;
                String nomAffiche;
                String prenomAffiche;

                if (formatA) {
                    // Format A : "Etudiant" = "Prénom NOM" ou "NOM Prénom"
                    String etudiantBrut = nvl(getCellStr(row.getCell(colEtudiant)));
                    nomComplet   = etudiantBrut;
                    // Séparer : le premier mot = prénom, le reste = nom
                    String[] parts = etudiantBrut.split(" ", 2);
                    prenomAffiche = parts.length >= 1 ? parts[0] : "";
                    nomAffiche    = parts.length >= 2 ? parts[1] : "";
                } else {
                    // Format B : colonnes séparées
                    nomAffiche    = nvl(getCellStr(row.getCell(colNom)));
                    prenomAffiche = nvl(getCellStr(row.getCell(colPrenom)));
                    nomComplet    = prenomAffiche + " " + nomAffiche;
                }

                if (nomComplet.trim().isEmpty()) continue;

                Map<String, String> etudiantInfo = new LinkedHashMap<>();
                etudiantInfo.put("nom",      nomAffiche.trim());
                etudiantInfo.put("prenom",   prenomAffiche.trim());
                etudiantInfo.put("nomComplet", nomComplet.trim());
                etudiantInfo.put("filiere",  filiere);
                etudiantInfo.put("jury1",    jury1);
                etudiantInfo.put("jury2",    jury2);

                result.computeIfAbsent(encadrant.trim(), k -> new ArrayList<>()).add(etudiantInfo);

                System.out.println("  ✔ " + nomComplet.trim()
                        + " | Encadrant: " + encadrant.trim()
                        + " | Jury1: " + jury1 + " | Jury2: " + jury2
                        + " | Filière: " + filiere);
            }

            System.out.println("=== Total : " + result.values().stream().mapToInt(List::size).sum()
                    + " étudiants, " + result.size() + " encadrants ===");
        }

        return result;
    }

    // ── Utilitaires ───────────────────────────────────────────────────────

    private static String getCellStr(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new java.text.SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                }
                double d = cell.getNumericCellValue();
                return (d == (long) d) ? String.valueOf((long) d) : String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue().trim(); } catch (Exception ignored) {}
                try {
                    double d2 = cell.getNumericCellValue();
                    return (d2 == (long) d2) ? String.valueOf((long) d2) : String.valueOf(d2);
                } catch (Exception ignored) {}
                return null;
            default:      return null;
        }
    }

    /** Retourne "" si null */
    private static String nvl(String s) {
        return (s == null) ? "" : s.trim();
    }
}