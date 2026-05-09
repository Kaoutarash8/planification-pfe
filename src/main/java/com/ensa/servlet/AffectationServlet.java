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
        // Initialisation portable avec ServletContext
        FileUtil.init(getServletContext());
        
        etudiantDao = new EtudiantExcelDao();
        professeurDao = new ProfesseurExcelDao();
        affectationService = new AffectationServiceImpl();
        
        System.out.println("=== SERVLET AFFECTATION INITIALISEE ===");
        System.out.println("Upload path: " + FileUtil.getUploadPath());
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
            Part etudiantsPart = request.getPart("fichierEtudiants");
            Part professeursPart = request.getPart("fichierProfesseurs");
            
            if (etudiantsPart == null || etudiantsPart.getSize() == 0) {
                throw new Exception("Le fichier des etudiants est requis");
            }
            if (professeursPart == null || professeursPart.getSize() == 0) {
                throw new Exception("Le fichier des professeurs est requis");
            }
            
            String etudiantsPath = FileUtil.sauvegarderFichier(etudiantsPart.getInputStream(), "etudiants.xlsx");
            String professeursPath = FileUtil.sauvegarderFichier(professeursPart.getInputStream(), "professeurs.xlsx");
            
            System.out.println("Fichier etudiants: " + etudiantsPath);
            System.out.println("Fichier professeurs: " + professeursPath);
            
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
            
            FileUtil.supprimerAncienneAffectation();
            
            Affectation affectation = affectationService.genererAffectation(etudiants, professeurs);
            
            VerificationContraintes verificateur = new VerificationContraintes();
            verificateur.verifierToutesContraintes(affectation, etudiants);
            
            String excelPath = ExcelWriter.sauvegarderExcel(affectation);
            File source = new File(excelPath);
            
            String excelRelativePath = "download?file=" + source.getName();
            
            System.out.println("Excel genere: " + source.getAbsolutePath());
            System.out.println("Lien web: " + excelRelativePath);
            
            request.setAttribute("affectation", affectation);
            request.setAttribute("excelPath", excelRelativePath);
            request.setAttribute("nomFichier", source.getName());
            
            request.getRequestDispatcher("/WEB-INF/views/resultat.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/affectation.jsp").forward(request, response);
        }
    }
}