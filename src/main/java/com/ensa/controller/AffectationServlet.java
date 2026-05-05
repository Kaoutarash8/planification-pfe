package com.ensa.controller;

import com.ensa.dao.EtudiantDAO;
import com.ensa.dao.ProfesseurDAO;
import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.model.ConfigurationSoutenance;
import com.ensa.model.PlanningSoutenance;
import com.ensa.service.AffectationService;
import com.ensa.service.PlanningService;
import com.ensa.service.ProcesVerbalService;
import com.ensa.service.RapportAffectationService;
import com.ensa.util.ExcelWriter;
import com.ensa.util.FileUploadUtil;
import com.ensa.util.ConfigurationReader;
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
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(urlPatterns = {"/affectation", "/AffectationServlet"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class AffectationServlet extends HttpServlet {
    
    private EtudiantDAO etudiantDAO;
    private ProfesseurDAO professeurDAO;
    private AffectationService affectationService;
    
    @Override
    public void init() throws ServletException {
        etudiantDAO = new EtudiantDAO();
        professeurDAO = new ProfesseurDAO();
        affectationService = new AffectationService();
        
        String basePath = getServletContext().getRealPath("/");
        if (basePath == null || basePath.isEmpty()) {
            basePath = System.getProperty("user.dir");
        }
        FileUploadUtil.setBasePath(basePath);
        
        FileUploadUtil.creerRepertoireUpload();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String page = request.getParameter("page");
        String action = request.getParameter("action");
        
        if ("genererPVs".equals(action)) {
            try {
                genererPVs(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "Erreur lors de la génération des PVs: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            }
            return;
        }
        
        if ("formulaire".equals(page) || "upload".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/views/AffectationFormulaire.jsp").forward(request, response);
        }
        else if ("planning".equals(page)) {
            request.getRequestDispatcher("/WEB-INF/views/PlanningFormulaire.jsp").forward(request, response);
        }
        else if ("accueil".equals(page)) {
            request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
        }
        else {
            request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            if ("generer".equals(action)) {
                genererAffectation(request, response);
            } else if ("genererPlanning".equals(action)) {
                genererPlanning(request, response);
            } else if ("genererPVs".equals(action)) {
                genererPVs(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/affectation?page=accueil");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    private void genererAffectation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Part etudiantsPart = request.getPart("fichierEtudiants");
        Part professeursPart = request.getPart("fichierProfesseurs");
        
        if (etudiantsPart == null || etudiantsPart.getSize() == 0) {
            throw new Exception("Le fichier des étudiants est requis");
        }
        if (professeursPart == null || professeursPart.getSize() == 0) {
            throw new Exception("Le fichier des professeurs est requis");
        }
        
        String etudiantsPath = FileUploadUtil.sauvegarderFichierPermanent(etudiantsPart.getInputStream(), "etudiants_latest.xlsx");
        String professeursPath = FileUploadUtil.sauvegarderFichierPermanent(professeursPart.getInputStream(), "professeurs_latest.xlsx");
        
        List<Etudiant> etudiants = etudiantDAO.lireEtudiants(etudiantsPath);
        List<Professeur> professeurs = professeurDAO.lireProfesseurs(professeursPath);
        
        if (etudiants.isEmpty()) throw new Exception("Aucun étudiant trouvé dans le fichier");
        if (professeurs.isEmpty()) throw new Exception("Aucun professeur trouvé dans le fichier");
        
        Map<Professeur, List<Etudiant>> affectation = 
            affectationService.genererAffectationEquilibree(etudiants, professeurs);
        
        Map<String, Object> statistiques = 
            affectationService.calculerStatistiques(professeurs, affectation);
        
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        
        // 1. Générer le fichier EXCEL pour la planification (stocké dans uploads/rapports)
        String excelPath = ExcelWriter.sauvegarderAffectation(affectation, timestamp);
        System.out.println("Excel généré: " + excelPath);
        
        // 2. Générer les fichiers PDF et DOCX
        String webappPath = getServletContext().getRealPath("/");
        RapportAffectationService rapportService = new RapportAffectationService(webappPath);
        rapportService.genererTousLesRapports(affectation, timestamp);
        
        request.getSession().setAttribute("affectation", affectation);
        request.getSession().setAttribute("statistiques", statistiques);
        request.getSession().setAttribute("etudiants", etudiants);
        request.getSession().setAttribute("professeurs", professeurs);
        request.getSession().setAttribute("timestamp", timestamp);
        
        request.getRequestDispatcher("/WEB-INF/views/resultat.jsp").forward(request, response);
    }
    
    private void genererPlanning(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String affectationPath = FileUploadUtil.getAffectationPath();
    
    File affectationFile = new File(affectationPath + "/affectation_latest.xlsx");
    
    System.out.println("=== GÉNÉRATION PLANNING ===");
    System.out.println("Dossier Affectation: " + affectationPath);
    System.out.println("Recherche fichier affectation: " + affectationFile.getAbsolutePath());
    System.out.println("Fichier existe: " + affectationFile.exists());
    
    if (!affectationFile.exists()) {
        throw new Exception("Veuillez d'abord générer l'affectation des encadrants.");
    }
    
    List<Etudiant> etudiants = etudiantDAO.lireEtudiants(affectationFile.getAbsolutePath());
    
    Part configPart = request.getPart("fichierConfiguration");
    String configPath;
    
    if (configPart != null && configPart.getSize() > 0) {
        configPath = FileUploadUtil.sauvegarderFichierPermanent(configPart.getInputStream(), "configuration_latest.xlsx");
    } else {
        configPath = FileUploadUtil.getDernierFichier("configuration");
        if (configPath == null) {
            throw new Exception("Le fichier de configuration est requis");
        }
    }
    
    ConfigurationSoutenance config = ConfigurationReader.lireConfiguration(configPath);
    
    String professeursPath = FileUploadUtil.getDernierFichier("professeurs");
    if (professeursPath == null) {
        throw new Exception("Le fichier des professeurs est requis");
    }
    List<Professeur> professeurs = professeurDAO.lireProfesseurs(professeursPath);
    
    PlanningService planningService = new PlanningService(etudiants, professeurs, config);
    List<PlanningSoutenance> planning = planningService.genererPlanning();
    
    // Sauvegarder le planning
    String outputPath = affectationPath + "/planning_latest.xlsx";
    planningService.sauvegarderPlanning(outputPath);
    
    request.getSession().setAttribute("planning", planning);
    request.getRequestDispatcher("/WEB-INF/views/planningResultat.jsp").forward(request, response);
}
    
    private void genererPVs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String basePath = FileUploadUtil.getBasePath();
        
        File affectationFile = new File(basePath + "/uploads/affectation_latest.xlsx");
        File planningFile = new File(basePath + "/uploads/planning_latest.xlsx");
        
        if (!affectationFile.exists()) {
            throw new Exception("Veuillez d'abord générer l'affectation des encadrants.");
        }
        
        if (!planningFile.exists()) {
            throw new Exception("Veuillez d'abord générer le planning des soutenances.");
        }
        
        String etudiantsPath = FileUploadUtil.getDernierFichier("etudiants");
        List<Etudiant> etudiants = etudiantDAO.lireEtudiants(etudiantsPath);
        
        List<PlanningSoutenance> planning = lirePlanning(planningFile.getAbsolutePath());
        
        ProcesVerbalService pvService = new ProcesVerbalService();
        pvService.genererTousLesPVs(planning, etudiants);
        
        File pvDir = new File(basePath + "/uploads/pvs");
        List<String> fichiersPV = new ArrayList<>();
        File[] files = pvDir.listFiles();
        if (files != null) {
            for (File f : files) {
                fichiersPV.add(f.getName());
            }
        }
        
        request.getSession().setAttribute("pvGeneres", true);
        request.getSession().setAttribute("fichiersPV", fichiersPV);
        request.getRequestDispatcher("/WEB-INF/views/pvResultat.jsp").forward(request, response);
    }
    
    private List<PlanningSoutenance> lirePlanning(String filePath) throws Exception {
        List<PlanningSoutenance> planning = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 7;
            
            System.out.println("=== LECTURE DU PLANNING ===");
            
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String num = getCellValue(row.getCell(0));
                if (num == null || num.isEmpty()) continue;
                if (num.equals("N°") || num.equals("Date")) continue;
                
                String nom = getCellValue(row.getCell(8));
                String prenom = getCellValue(row.getCell(9));
                
                if (nom == null || nom.isEmpty()) continue;
                if (nom.equals("Nom") || nom.equals("Nom Etudiant")) continue;
                
                PlanningSoutenance ps = new PlanningSoutenance();
                ps.setEtudiantCNE(getCellValue(row.getCell(1)));
                ps.setEtudiantNom(nom);
                ps.setEtudiantPrenom(prenom);
                ps.setDate(getCellValue(row.getCell(2)));
                ps.setHeure(getCellValue(row.getCell(3)));
                ps.setSalle(getCellValue(row.getCell(4)));
                ps.setEncadrant(getCellValue(row.getCell(5)));
                ps.setJury1(getCellValue(row.getCell(6)));
                ps.setJury2(getCellValue(row.getCell(7)));
                ps.setFiliere(getCellValue(row.getCell(10)));
                
                planning.add(ps);
                System.out.println("Lecture: " + prenom + " " + nom + " - " + ps.getDate());
            }
        }
        
        System.out.println("=== TOTAL SOUTENANCES LUES: " + planning.size() + " ===");
        return planning;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }
}