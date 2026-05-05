package com.ensa.service;

import com.ensa.model.Etudiant;
import com.ensa.model.PlanningSoutenance;
import com.ensa.model.ProcesVerbal;
import com.ensa.util.FileUploadUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProcesVerbalService {
    
    private String basePath;
    private String pvsPath;
    
    public ProcesVerbalService() {
        basePath = FileUploadUtil.getBasePath();
        pvsPath = basePath + "/uploads/pvs";
        
        File pvDir = new File(pvsPath);
        if (!pvDir.exists()) {
            pvDir.mkdirs();
        }
        System.out.println("Dossier PVs: " + pvDir.getAbsolutePath());
    }
    
    public void genererTousLesPVs(List<PlanningSoutenance> planning, List<Etudiant> etudiants) throws Exception {
        System.out.println("=== GÉNÉRATION DES PVs PAR ENCADRANT ===");
        
        // Nettoyer l'ancien dossier PVs
        File pvDir = new File(pvsPath);
        if (pvDir.exists()) {
            supprimerDossier(pvDir);
        }
        pvDir.mkdirs();
        
        // Grouper les soutenances par encadrant (qui sera président)
        Map<String, List<PlanningSoutenance>> pvsParEncadrant = new HashMap<>();
        
        for (PlanningSoutenance ps : planning) {
            String president = ps.getEncadrant();
            String nomDossier = president.replace("/", "_").replace("\\", "_").replace(":", "_").replace("?", "");
            pvsParEncadrant.computeIfAbsent(nomDossier, k -> new ArrayList<>()).add(ps);
        }
        
        int totalPVs = 0;
        
        for (Map.Entry<String, List<PlanningSoutenance>> entry : pvsParEncadrant.entrySet()) {
            String encadrantDir = entry.getKey();
            List<PlanningSoutenance> soutenances = entry.getValue();
            
            File dossierEncadrant = new File(pvsPath, encadrantDir);
            dossierEncadrant.mkdirs();
            
            System.out.println("📁 Dossier créé: " + encadrantDir + " (" + soutenances.size() + " PVs)");
            
            for (PlanningSoutenance ps : soutenances) {
                Etudiant etudiant = trouverEtudiantParCNE(etudiants, ps.getEtudiantCNE());
                if (etudiant == null) continue;
                
                ProcesVerbal pv = new ProcesVerbal();
                pv.setCne(etudiant.getCne());
                pv.setNom(etudiant.getNom());
                pv.setPrenom(etudiant.getPrenom());
                pv.setFiliere(etudiant.getFiliere());
                pv.setIntituleRapport("");
                pv.setEncadrant(ps.getEncadrant());
                pv.setPresident(ps.getEncadrant());
                pv.setRapporteur1(ps.getJury1());
                pv.setRapporteur2(ps.getJury2());
                pv.setDate(ps.getDate());
                
                genererDOCX(pv, dossierEncadrant.getAbsolutePath());
                genererPDF(pv, dossierEncadrant.getAbsolutePath());
                totalPVs++;
            }
        }
        
        System.out.println("=== RÉSUMÉ: " + totalPVs + " PVs générés dans " + pvsParEncadrant.size() + " dossiers ===");
    }
    
    private void supprimerDossier(File dossier) {
        if (dossier.exists()) {
            File[] fichiers = dossier.listFiles();
            if (fichiers != null) {
                for (File f : fichiers) {
                    if (f.isDirectory()) {
                        supprimerDossier(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dossier.delete();
        }
    }
    
    private Etudiant trouverEtudiantParCNE(List<Etudiant> etudiants, String cne) {
        if (cne == null || cne.isEmpty()) return null;
        for (Etudiant e : etudiants) {
            if (e.getCne() != null && e.getCne().equalsIgnoreCase(cne)) {
                return e;
            }
        }
        return null;
    }
    
    private void genererDOCX(ProcesVerbal pv, String dossierPath) throws Exception {
        String fileName = "Fiche_Evaluation_PFE_" + pv.getPrenom() + "_" + pv.getNom() + ".docx";
        String filePath = dossierPath + "/" + fileName;
        
        XWPFDocument document = new XWPFDocument();
        
        // ==================== TITRE ====================
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("FICHE D'ÉVALUATION DU PROJET DE FIN D'ÉTUDES");
        titleRun.setBold(true);
        titleRun.setFontSize(14);
        
        document.createParagraph();
        
        // ==================== NOM ÉTUDIANT ====================
        XWPFParagraph nomPara = document.createParagraph();
        XWPFRun nomLabel = nomPara.createRun();
        nomLabel.setText("[Nom - Prénom de l'élève ingénieur] :");
        nomLabel.setBold(true);
        nomLabel.addCarriageReturn();
        
        XWPFRun nomValue = nomPara.createRun();
        nomValue.setText(pv.getPrenom().toUpperCase() + " " + pv.getNom().toUpperCase());
        nomValue.setBold(true);
        nomValue.setColor("0033a0");
        
        document.createParagraph();
        
        // ==================== FILIÈRE ====================
        XWPFParagraph filierePara = document.createParagraph();
        XWPFRun filiereLabel = filierePara.createRun();
        filiereLabel.setText("[Filière] : ");
        filiereLabel.setBold(true);
        
        XWPFRun filiereValue = filierePara.createRun();
        String filiere = pv.getFiliere();
        if (filiere != null) {
            if (filiere.toUpperCase().contains("DATA") || filiere.toUpperCase().contains("DONNÉES")) {
                filiereValue.setText("◻ Ingénierie des Données");
            } else if (filiere.toUpperCase().contains("INFO") || filiere.toUpperCase().contains("GÉNIE")) {
                filiereValue.setText("◻ Génie Informatique");
            } else {
                filiereValue.setText("◻ " + filiere);
            }
        } else {
            filiereValue.setText("◻");
        }
        
        document.createParagraph();
        
        // ==================== INTITULÉ DU RAPPORT ====================
        XWPFParagraph intitulePara = document.createParagraph();
        XWPFRun intituleLabel = intitulePara.createRun();
        intituleLabel.setText("[Intitulé du rapport] :");
        intituleLabel.setBold(true);
        intituleLabel.addCarriageReturn();
        
        XWPFRun intituleValue = intitulePara.createRun();
        intituleValue.setText("_________________________________________");
        intituleValue.setFontSize(10);
        
        document.createParagraph();
        
        // ==================== ENCADRANT ====================
        XWPFParagraph encadrantPara = document.createParagraph();
        XWPFRun encadrantLabel = encadrantPara.createRun();
        encadrantLabel.setText("[L'encadrant(e) interne] :");
        encadrantLabel.setBold(true);
        encadrantLabel.addCarriageReturn();
        
        XWPFRun encadrantValue = encadrantPara.createRun();
        encadrantValue.setText("Pr. " + pv.getEncadrant());
        encadrantValue.setBold(true);
        encadrantValue.setColor("0033a0");
        
        document.createParagraph();
        
        // ==================== MEMBRES DU JURY ====================
        XWPFParagraph juryTitle = document.createParagraph();
        XWPFRun juryTitleRun = juryTitle.createRun();
        juryTitleRun.setText("[Membres du jury] :");
        juryTitleRun.setBold(true);
        
        document.createParagraph();
        
        // Tableau pour les membres du jury (style simple)
        String[][] juryData = {
            {"Président :", "Pr. " + pv.getPresident()},
            {"Rapporteur 1 :", "Pr. " + pv.getRapporteur1()},
            {"Rapporteur 2 :", "Pr. " + pv.getRapporteur2()}
        };
        
        for (String[] row : juryData) {
            XWPFParagraph juryRow = document.createParagraph();
            XWPFRun labelRun = juryRow.createRun();
            labelRun.setText("  - " + row[0]);
            labelRun.setBold(true);
            
            XWPFRun valueRun = juryRow.createRun();
            valueRun.setText(" " + row[1]);
            valueRun.setColor("0033a0");
        }
        
        document.createParagraph();
        
        // ==================== NOTES ====================
        XWPFParagraph notesTitle = document.createParagraph();
        XWPFRun notesTitleRun = notesTitle.createRun();
        notesTitleRun.setText("[Notes] :");
        notesTitleRun.setBold(true);
        
        document.createParagraph();
        
        // Note du Contenu
        XWPFParagraph noteContenu = document.createParagraph();
        XWPFRun contenuLabel = noteContenu.createRun();
        contenuLabel.setText("Note du Contenu (C) : ");
        contenuLabel.setBold(true);
        XWPFRun contenuValue = noteContenu.createRun();
        contenuValue.setText("_________");
        contenuValue.setFontSize(11);
        
        // Note du Mémoire
        XWPFParagraph noteMemoire = document.createParagraph();
        XWPFRun memoireLabel = noteMemoire.createRun();
        memoireLabel.setText("Note du Mémoire (M) : ");
        memoireLabel.setBold(true);
        XWPFRun memoireValue = noteMemoire.createRun();
        memoireValue.setText("_________");
        memoireValue.setFontSize(11);
        
        // Note de la Soutenance
        XWPFParagraph noteSoutenance = document.createParagraph();
        XWPFRun soutenanceLabel = noteSoutenance.createRun();
        soutenanceLabel.setText("Note de la Soutenance (S) : ");
        soutenanceLabel.setBold(true);
        XWPFRun soutenanceValue = noteSoutenance.createRun();
        soutenanceValue.setText("_________");
        soutenanceValue.setFontSize(11);
        
        document.createParagraph();
        
        // ==================== MOYENNE ====================
        XWPFParagraph moyennePara = document.createParagraph();
        XWPFRun moyenneLabel = moyennePara.createRun();
        moyenneLabel.setText("MOYENNE = C × 0,5 + M × 0,2 + S × 0,3 = ");
        moyenneLabel.setBold(true);
        moyenneLabel.setFontSize(12);
        
        XWPFRun moyenneValue = moyennePara.createRun();
        moyenneValue.setText("_________");
        moyenneValue.setFontSize(12);
        moyenneValue.setBold(true);
        moyenneValue.setColor("0033a0");
        
        document.createParagraph();
        document.createParagraph();
        
        // ==================== DATE ====================
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        XWPFParagraph datePara = document.createParagraph();
        XWPFRun dateLabel = datePara.createRun();
        dateLabel.setText("Le : ");
        dateLabel.setBold(true);
        XWPFRun dateValue = datePara.createRun();
        dateValue.setText(sdf.format(new Date()));
        
        document.createParagraph();
        document.createParagraph();
        
        // ==================== SIGNATURES ====================
        XWPFParagraph signTitle = document.createParagraph();
        XWPFRun signTitleRun = signTitle.createRun();
        signTitleRun.setText("Signature des membres du jury :");
        signTitleRun.setBold(true);
        
        document.createParagraph();
        
        // Ligne des signatures
        XWPFParagraph signPara = document.createParagraph();
        signPara.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun sign1 = signPara.createRun();
        sign1.setText("Pr. " + pv.getPresident());
        sign1.addTab();
        sign1.addTab();
        sign1.addTab();
        sign1.addTab();
        
        XWPFRun sign2 = signPara.createRun();
        sign2.setText("Pr. " + pv.getRapporteur1());
        sign2.addTab();
        sign2.addTab();
        sign2.addTab();
        sign2.addTab();
        
        XWPFRun sign3 = signPara.createRun();
        sign3.setText("Pr. " + pv.getRapporteur2());
        
        // Ligne pour les signatures manuscrites
        document.createParagraph();
        XWPFParagraph signatureLine = document.createParagraph();
        signatureLine.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun lineRun = signatureLine.createRun();
        lineRun.setText("_________________________________    _________________________________    _________________________________");
        lineRun.setFontSize(10);
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            document.write(fos);
        }
        
        System.out.println("  ✓ DOCX généré: " + fileName);
    }
    
    private void genererPDF(ProcesVerbal pv, String dossierPath) throws Exception {
        String fileName = "Fiche_Evaluation_PFE_" + pv.getPrenom() + "_" + pv.getNom() + ".pdf";
        String filePath = dossierPath + "/" + fileName;
        
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.setMargins(40, 40, 40, 40);
        
        PdfFont fontNormal = PdfFontFactory.createFont("Helvetica");
        PdfFont fontBold = PdfFontFactory.createFont("Helvetica-Bold");
        
        // TITRE
        Paragraph title = new Paragraph("FICHE D'ÉVALUATION DU PROJET DE FIN D'ÉTUDES")
            .setFont(fontBold).setFontSize(14).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20);
        document.add(title);
        
        // Nom étudiant
        Paragraph nomLabel = new Paragraph("[Nom - Prénom de l'élève ingénieur] :").setFont(fontBold);
        document.add(nomLabel);
        Paragraph nomValue = new Paragraph(pv.getPrenom().toUpperCase() + " " + pv.getNom().toUpperCase())
            .setFont(fontBold).setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE);
        document.add(nomValue);
        document.add(new Paragraph(" "));
        
        // Filière
        Paragraph filiereLabel = new Paragraph("[Filière] :").setFont(fontBold);
        document.add(filiereLabel);
        String filiere = pv.getFiliere();
        String filiereText = "◻ ";
        if (filiere != null) {
            if (filiere.toUpperCase().contains("DATA") || filiere.toUpperCase().contains("DONNÉES")) {
                filiereText += "Ingénierie des Données";
            } else if (filiere.toUpperCase().contains("INFO") || filiere.toUpperCase().contains("GÉNIE")) {
                filiereText += "Génie Informatique";
            } else {
                filiereText += filiere;
            }
        }
        document.add(new Paragraph(filiereText));
        document.add(new Paragraph(" "));
        
        // Intitulé du rapport
        Paragraph intituleLabel = new Paragraph("[Intitulé du rapport] :").setFont(fontBold);
        document.add(intituleLabel);
        document.add(new Paragraph("_________________________________________"));
        document.add(new Paragraph(" "));
        
        // Encadrant
        Paragraph encadrantLabel = new Paragraph("[L'encadrant(e) interne] :").setFont(fontBold);
        document.add(encadrantLabel);
        Paragraph encadrantValue = new Paragraph("Pr. " + pv.getEncadrant())
            .setFont(fontBold).setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE);
        document.add(encadrantValue);
        document.add(new Paragraph(" "));
        
        // Membres du jury
        Paragraph juryTitle = new Paragraph("[Membres du jury] :").setFont(fontBold);
        document.add(juryTitle);
        document.add(new Paragraph("  - Président :    Pr. " + pv.getPresident()).setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE));
        document.add(new Paragraph("  - Rapporteur 1 : Pr. " + pv.getRapporteur1()).setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE));
        document.add(new Paragraph("  - Rapporteur 2 : Pr. " + pv.getRapporteur2()).setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE));
        document.add(new Paragraph(" "));
        
        // Notes
        Paragraph notesTitle = new Paragraph("[Notes] :").setFont(fontBold);
        document.add(notesTitle);
        document.add(new Paragraph("Note du Contenu (C) :    _________"));
        document.add(new Paragraph("Note du Mémoire (M) :    _________"));
        document.add(new Paragraph("Note de la Soutenance (S) : _________"));
        document.add(new Paragraph(" "));
        
        // Moyenne
        Paragraph moyenne = new Paragraph("MOYENNE = C × 0,5 + M × 0,2 + S × 0,3 = _________")
            .setFont(fontBold).setFontSize(12);
        document.add(moyenne);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        
        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        document.add(new Paragraph("Le : " + sdf.format(new Date())));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        
        // Signatures
        Paragraph signTitle = new Paragraph("Signature des membres du jury :").setFont(fontBold);
        document.add(signTitle);
        document.add(new Paragraph(" "));
        
        Paragraph signLine = new Paragraph("Pr. " + pv.getPresident() + "                    Pr. " + pv.getRapporteur1() + "                    Pr. " + pv.getRapporteur2());
        document.add(signLine);
        
        Paragraph line = new Paragraph("_________________________________    _________________________________    _________________________________");
        document.add(line);
        
        document.close();
        
        System.out.println("  ✓ PDF généré: " + fileName);
    }
    
    public File genererZipTousLesPVs() throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String zipPath = basePath + "/uploads/pvs_" + timestamp + ".zip";
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            File dossierPVs = new File(pvsPath);
            ajouterDossierAuZip(dossierPVs, dossierPVs.getName(), zos);
        }
        
        return new File(zipPath);
    }
    
    public File genererZipParEncadrant(String nomEncadrant) throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String zipPath = basePath + "/uploads/pvs_" + nomEncadrant.replace(" ", "_") + "_" + timestamp + ".zip";
        
        File dossierEncadrant = new File(pvsPath, nomEncadrant);
        if (!dossierEncadrant.exists()) {
            return null;
        }
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            ajouterDossierAuZip(dossierEncadrant, nomEncadrant, zos);
        }
        
        return new File(zipPath);
    }
    
    private void ajouterDossierAuZip(File source, String parentPath, ZipOutputStream zos) throws Exception {
        if (!source.exists()) return;
        
        for (File fichier : source.listFiles()) {
            if (fichier.isDirectory()) {
                ajouterDossierAuZip(fichier, parentPath + "/" + fichier.getName(), zos);
            } else {
                byte[] buffer = new byte[1024];
                try (java.io.FileInputStream fis = new java.io.FileInputStream(fichier)) {
                    zos.putNextEntry(new ZipEntry(parentPath + "/" + fichier.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }
    
    public List<Map<String, Object>> getListeEncadrantsAvecNbPVs() {
        List<Map<String, Object>> encadrants = new ArrayList<>();
        File dossierPVs = new File(pvsPath);
        if (dossierPVs.exists()) {
            for (File f : dossierPVs.listFiles()) {
                if (f.isDirectory()) {
                    Map<String, Object> encadrant = new HashMap<>();
                    encadrant.put("nom", f.getName());
                    int nbPVs = f.listFiles() != null ? f.listFiles().length : 0;
                    encadrant.put("nbPVs", nbPVs);
                    encadrants.add(encadrant);
                }
            }
        }
        return encadrants;
    }
}