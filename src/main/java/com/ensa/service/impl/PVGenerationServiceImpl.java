package com.ensa.service.impl;

import com.ensa.model.Professeur;
import com.ensa.model.Etudiant;
import com.ensa.service.interfaces.PVGenerationService;
import com.ensa.util.PDFGeneratorUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class PVGenerationServiceImpl implements PVGenerationService {
    
    @Override
    public Map<Professeur, List<Etudiant>> getEncadrantsWithEtudiants(String planningPath) throws Exception {
        Map<Professeur, List<Etudiant>> result = new LinkedHashMap<>();
        
        try (FileInputStream fis = new FileInputStream(planningPath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String encadrantName = getCellValue(row.getCell(3));
                String etudiantName = getCellValue(row.getCell(4));
                String filiere = getCellValue(row.getCell(5));
                
                if (encadrantName == null || encadrantName.trim().isEmpty()) continue;
                if (etudiantName == null || etudiantName.trim().isEmpty()) continue;
                
                Professeur professeur = findOrCreateProfesseur(result, encadrantName);
                
                Etudiant etudiant = new Etudiant();
                String[] parts = etudiantName.split(" ");
                if (parts.length >= 2) {
                    etudiant.setPrenom(parts[0]);
                    etudiant.setNom(parts[1]);
                } else {
                    etudiant.setPrenom(etudiantName);
                    etudiant.setNom("");
                }
                etudiant.setFiliere(filiere);
                
                result.computeIfAbsent(professeur, k -> new ArrayList<>()).add(etudiant);
            }
        }
        
        return result;
    }
    
    private Professeur findOrCreateProfesseur(Map<Professeur, List<Etudiant>> map, String nomComplet) {
        for (Professeur p : map.keySet()) {
            if (p.getNomComplet().equalsIgnoreCase(nomComplet)) {
                return p;
            }
        }
        
        Professeur nouveau = new Professeur();
        String[] parts = nomComplet.split(" ");
        if (parts.length >= 2) {
            nouveau.setPrenom(parts[0]);
            nouveau.setNom(parts[1]);
        } else {
            nouveau.setNom(nomComplet);
            nouveau.setPrenom("");
        }
        return nouveau;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return "";
    }
    
    @Override
    public byte[] genererPV(Professeur encadrant, List<Etudiant> etudiants, List<Note> notes,
                            String president, String rapporteur1, String rapporteur2,
                            String dateSoutenance) throws Exception {
        
        // Déléguer à l'utilitaire
        return PDFGeneratorUtil.generateEvaluationPDF(encadrant, etudiants, notes, 
            president, rapporteur1, rapporteur2, dateSoutenance);
    }
}