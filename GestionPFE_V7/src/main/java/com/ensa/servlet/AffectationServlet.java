package com.ensa.servlet;

import com.ensa.dao.impl.EtudiantExcelDao;
import com.ensa.dao.impl.ProfesseurExcelDao;
import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.Affectation;
import com.ensa.service.impl.AffectationServiceImpl;
import com.ensa.util.FileUtil;
import com.ensa.util.ExcelWriter;
import com.ensa.algorithmverification.VerificationContraintes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

@WebServlet("/affectation")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize       = 1024 * 1024 * 10,
    maxRequestSize    = 1024 * 1024 * 50
)
public class AffectationServlet extends HttpServlet {

    private EtudiantExcelDao   etudiantDao;
    private ProfesseurExcelDao professeurDao;
    private AffectationServiceImpl affectationService;

    @Override
    public void init() throws ServletException {
        FileUtil.init(getServletContext());
        etudiantDao        = new EtudiantExcelDao();
        professeurDao      = new ProfesseurExcelDao();
        affectationService = new AffectationServiceImpl();
        System.out.println("=== SERVLET AFFECTATION INITIALISEE ===");
        System.out.println("Upload path: " + FileUtil.getUploadPath());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/affectation.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // ── Récupérer le fichier combiné ──────────────────────────────
            Part fichierPart = request.getPart("fichierCombine");

            if (fichierPart == null || fichierPart.getSize() == 0) {
                throw new Exception("Le fichier Excel est requis.");
            }

            // Vérifier l'extension
            String fileName = fichierPart.getSubmittedFileName();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                throw new Exception("Format invalide : seuls les fichiers .xlsx sont acceptés.");
            }

            // Sauvegarder temporairement le fichier combiné
            String combinePath = FileUtil.sauvegarderFichier(fichierPart.getInputStream(), "combine_temp.xlsx");
            System.out.println("Fichier combiné sauvegardé : " + combinePath);

            // ── Extraire les deux feuilles dans des fichiers séparés ──────
            String etudiantsPath   = extraireFeuille(combinePath, 0, "etudiants.xlsx");
            String professeursPath = extraireFeuille(combinePath, 1, "professeurs.xlsx");

            System.out.println("Feuille étudiants  → " + etudiantsPath);
            System.out.println("Feuille professeurs → " + professeursPath);

            // ── Lire les données via les DAOs existants (inchangés) ───────
            List<Etudiant>   etudiants   = etudiantDao.lireEtudiants(etudiantsPath);
            List<Professeur> professeurs = professeurDao.lireProfesseurs(professeursPath);

            if (etudiants.isEmpty()) {
                throw new Exception("Aucun étudiant trouvé dans la feuille 1 (\"etudiants\").");
            }
            if (professeurs.isEmpty()) {
                throw new Exception("Aucun professeur trouvé dans la feuille 2 (\"Prof\").");
            }

            System.out.println("Étudiants lus  : " + etudiants.size());
            System.out.println("Professeurs lus : " + professeurs.size());

            // ── Reste du traitement : identique à l'ancien code ───────────
            FileUtil.supprimerAncienneAffectation();

            Affectation affectation = affectationService.genererAffectation(etudiants, professeurs);

            VerificationContraintes verificateur = new VerificationContraintes();
            verificateur.verifierToutesContraintes(affectation, etudiants);

            String excelPath = ExcelWriter.sauvegarderExcel(affectation);
            File   source    = new File(excelPath);

            request.setAttribute("affectation",   affectation);
            request.setAttribute("excelPath",      "download?file=" + source.getName());
            request.setAttribute("nomFichier",     source.getName());

            request.getRequestDispatcher("/WEB-INF/views/resultat.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/affectation.jsp").forward(request, response);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Utilitaire : extrait une feuille du classeur combiné
    //  et la sauvegarde comme fichier Excel indépendant.
    //  sheetIndex : 0 = première feuille, 1 = deuxième feuille
    // ─────────────────────────────────────────────────────────────────────
    private String extraireFeuille(String sourcePath, int sheetIndex, String outputFileName)
            throws Exception {

        String outputPath = FileUtil.getUploadPath() + File.separator + outputFileName;

        try (FileInputStream fis = new FileInputStream(sourcePath);
             Workbook sourceWb   = new XSSFWorkbook(fis);
             Workbook targetWb   = new XSSFWorkbook()) {

            if (sheetIndex >= sourceWb.getNumberOfSheets()) {
                throw new Exception("Le fichier Excel ne contient pas de feuille à l'index "
                        + sheetIndex + ". Vérifiez que votre fichier a bien 2 feuilles : "
                        + "'etudiants' (feuille 1) et 'Prof' (feuille 2).");
            }

            Sheet sourceSheet = sourceWb.getSheetAt(sheetIndex);
            Sheet targetSheet = targetWb.createSheet(sourceSheet.getSheetName());

            // Copier toutes les lignes et cellules
            for (int r = 0; r <= sourceSheet.getLastRowNum(); r++) {
                Row sourceRow = sourceSheet.getRow(r);
                if (sourceRow == null) continue;

                Row targetRow = targetSheet.createRow(r);
                for (int c = 0; c < sourceRow.getLastCellNum(); c++) {
                    Cell sourceCell = sourceRow.getCell(c);
                    if (sourceCell == null) continue;

                    Cell targetCell = targetRow.createCell(c);
                    copierCellule(sourceCell, targetCell, targetWb);
                }
            }

            // Sauvegarder le fichier extrait
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                targetWb.write(fos);
            }
        }

        return outputPath;
    }

    /** Copie la valeur d'une cellule source vers une cellule cible */
    private void copierCellule(Cell source, Cell target, Workbook targetWb) {
        switch (source.getCellType()) {
            case STRING:
                target.setCellValue(source.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(source)) {
                    target.setCellValue(source.getDateCellValue());
                } else {
                    target.setCellValue(source.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                target.setCellValue(source.getBooleanCellValue());
                break;
            case FORMULA:
                target.setCellValue(source.getCellFormula());
                break;
            default:
                target.setCellValue("");
                break;
        }
    }
}