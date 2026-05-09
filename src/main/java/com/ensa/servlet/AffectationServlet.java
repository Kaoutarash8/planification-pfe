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
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@WebServlet("/affectation")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class AffectationServlet extends HttpServlet {
    
    private EtudiantExcelDao etudiantDao;
    private ProfesseurExcelDao professeurDao;
    private AffectationServiceImpl affectationService;
    
    @Override
    public void init() throws ServletException {
        etudiantDao = new EtudiantExcelDao();
        professeurDao = new ProfesseurExcelDao();
        affectationService = new AffectationServiceImpl();
        
        // Trouver le chemin du projet Eclipse
        String uploadPath = findEclipseProjectUploadPath();
        
        FileUtil.setUploadPath(uploadPath);
        
        // Stocker le chemin dans le contexte pour DownloadServlet
        getServletContext().setAttribute("uploadPath", uploadPath);
        
        System.out.println("=== SERVLET INITIALISEE ===");
        System.out.println("Upload path: " + uploadPath);
    }
    
    private String findEclipseProjectUploadPath() {
        try {
            // Méthode 1: Chercher à partir du répertoire de déploiement Tomcat
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                File catalinaDir = new File(catalinaBase);
                File current = catalinaDir;
                for (int i = 0; i < 10; i++) {
                    if (current == null) break;
                    
                    File metadataDir = new File(current, ".metadata");
                    if (metadataDir.exists()) {
                        File workspace = current;
                        File projectDir = new File(workspace, "GestionPFE_V7");
                        
                        if (projectDir.exists()) {
                            File webappUploads = new File(projectDir, "src/main/webapp/uploads");
                            if (!webappUploads.exists()) {
                                webappUploads.mkdirs();
                            }
                            return webappUploads.getAbsolutePath();
                        }
                    }
                    current = current.getParentFile();
                }
            }
            
            // Méthode 2: Utiliser le chemin du classpath
            String classPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            classPath = URLDecoder.decode(classPath, "UTF-8");
            
            if (classPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                classPath = classPath.substring(1);
            }
            
            File classFile = new File(classPath);
            File current = classFile;
            
            for (int i = 0; i < 15; i++) {
                if (current == null) break;
                
                File testWebapp = new File(current, "src/main/webapp");
                if (testWebapp.exists() && testWebapp.isDirectory()) {
                    File uploads = new File(testWebapp, "uploads");
                    if (!uploads.exists()) {
                        uploads.mkdirs();
                    }
                    return uploads.getAbsolutePath();
                }
                current = current.getParentFile();
            }
            
            // Méthode 3: Fallback - utiliser le répertoire du projet
            String userDir = System.getProperty("user.dir");
            File projectUploads = new File(userDir, "src/main/webapp/uploads");
            if (!projectUploads.exists()) {
                projectUploads.mkdirs();
            }
            return projectUploads.getAbsolutePath();
            
        } catch (Exception e) {
            e.printStackTrace();
            String fallback = System.getProperty("java.io.tmpdir") + "/pfe_uploads";
            new File(fallback).mkdirs();
            return fallback;
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        System.out.println("doGet appelle - affichage formulaire");
        request.getRequestDispatcher("/WEB-INF/views/affectation.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("doPost appelle - traitement formulaire");
        
        try {
            // RECUPERER LES FICHIERS
            Part etudiantsPart = request.getPart("fichierEtudiants");
            Part professeursPart = request.getPart("fichierProfesseurs");
            
            System.out.println("Etudiants part: " + (etudiantsPart != null ? etudiantsPart.getSize() : "null"));
            System.out.println("Professeurs part: " + (professeursPart != null ? professeursPart.getSize() : "null"));
            
            if (etudiantsPart == null || etudiantsPart.getSize() == 0) {
                throw new Exception("Le fichier des etudiants est requis");
            }
            if (professeursPart == null || professeursPart.getSize() == 0) {
                throw new Exception("Le fichier des professeurs est requis");
            }
            
            // SAUVEGARDER LES FICHIERS
            String etudiantsPath = FileUtil.sauvegarderFichier(etudiantsPart.getInputStream(), "etudiants.xlsx");
            String professeursPath = FileUtil.sauvegarderFichier(professeursPart.getInputStream(), "professeurs.xlsx");
            
            System.out.println("Fichier etudiants: " + etudiantsPath);
            System.out.println("Fichier professeurs: " + professeursPath);
            
            // LIRE LES DONNEES
            List<Etudiant> etudiants = etudiantDao.lireEtudiants(etudiantsPath);
            List<Professeur> professeurs = professeurDao.lireProfesseurs(professeursPath);
            
            if (etudiants.isEmpty()) {
                throw new Exception("Aucun etudiant trouve dans le fichier");
            }
            if (professeurs.isEmpty()) {
                throw new Exception("Aucun professeur trouve dans le fichier");
            }
            
            System.out.println("Total etudiants: " + etudiants.size());
            System.out.println("Total professeurs: " + professeurs.size());
            
            // GENERER L'AFFECTATION
            Affectation affectation = affectationService.genererAffectation(etudiants, professeurs);
            
            // VERIFIER LES CONTRAINTES
            VerificationContraintes verificateur = new VerificationContraintes();
            verificateur.verifierToutesContraintes(affectation, etudiants);
            
            // GENERER L'EXCEL DANS LE PROJET SOURCE
            String excelPath = ExcelWriter.sauvegarderExcel(affectation);
            File source = new File(excelPath);
            
            // LIEN VERS DownloadServlet
            String excelRelativePath = "download?file=" + source.getName();
            
            System.out.println("Excel genere: " + source.getAbsolutePath());
            System.out.println("Lien web: " + excelRelativePath);
            
            // PREPARER LES DONNEES POUR LA JSP
            request.setAttribute("affectation", affectation);
            request.setAttribute("excelPath", excelRelativePath);
            request.setAttribute("nomFichier", source.getName());
            
            // REDIRIGER VERS LA PAGE DE RESULTAT
            request.getRequestDispatcher("/WEB-INF/views/resultat.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/affectation.jsp").forward(request, response);
        }
    }
}