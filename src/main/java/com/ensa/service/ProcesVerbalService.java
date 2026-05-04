package com.ensa.service;

import com.ensa.model.Etudiant;
import com.ensa.model.PlanningSoutenance;
import com.ensa.model.ProcesVerbal;
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
import java.util.Date;
import java.util.List;

public class ProcesVerbalService {
    
    private String basePath;
    
    public ProcesVerbalService() {
        basePath = "C:/Users/e/workspace-pfe/pfe-affectation";
        
        File pvDir = new File(basePath + "/uploads/pvs");
        if (!pvDir.exists()) {
            pvDir.mkdirs();
        }
        System.out.println("Dossier PVs: " + pvDir.getAbsolutePath());
    }
    
    public void genererTousLesPVs(List<PlanningSoutenance> planning, List<Etudiant> etudiants) throws Exception {
        System.out.println("=== GÉNÉRATION DES PVs ===");
        System.out.println("Nombre de soutenances: " + planning.size());
        System.out.println("Nombre d'étudiants: " + etudiants.size());
        
        int count = 0;
        for (PlanningSoutenance ps : planning) {
            // Chercher l'étudiant par CNE d'abord, puis par nom
            Etudiant etudiant = trouverEtudiantParCNE(etudiants, ps.getEtudiantCNE());
            
            if (etudiant == null) {
                System.out.println("Étudiant non trouvé pour CNE: " + ps.getEtudiantCNE());
                continue;
            }
            
            ProcesVerbal pv = new ProcesVerbal();
            pv.setCne(etudiant.getCne());
            pv.setNom(etudiant.getNom());
            pv.setPrenom(etudiant.getPrenom());
            pv.setFiliere(etudiant.getFiliere());
            pv.setIntituleRapport("");
            pv.setEncadrant(ps.getEncadrant());
            pv.setPresident(ps.getJury1());
            pv.setRapporteur1(ps.getJury2());
            pv.setRapporteur2(ps.getEncadrant());
            pv.setDate(ps.getDate());
            
            try {
                genererPDF(pv);
                genererDOCX(pv);
                count++;
                System.out.println("✓ PV généré pour: " + etudiant.getPrenom() + " " + etudiant.getNom() + " (" + ps.getEtudiantCNE() + ")");
            } catch (Exception e) {
                System.err.println("✗ Erreur: " + e.getMessage());
            }
        }
        
        System.out.println("=== RÉSUMÉ: " + count + "/" + planning.size() + " PVs générés ===");
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
    
    private void genererPDF(ProcesVerbal pv) throws Exception {
        String fileName = "PV_" + pv.getCne() + "_" + pv.getNom() + "_" + pv.getPrenom() + ".pdf";
        String filePath = basePath + "/uploads/pvs/" + fileName;
        
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        PdfFont font = PdfFontFactory.createFont("Helvetica");
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        
        // Titre
        Paragraph title = new Paragraph("PROCÈS-VERBAL DE SOUTENANCE PFE")
            .setFont(boldFont).setFontSize(16).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20);
        document.add(title);
        
        Paragraph ecole = new Paragraph("ECOLE NATIONALE DES SCIENCES APPLIQUÉES - AL HOCEIMA")
            .setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(5);
        document.add(ecole);
        
        Paragraph dept = new Paragraph("Département Mathématiques et Informatique")
            .setFont(font).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20);
        document.add(dept);
        
        // Informations étudiant
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setMarginBottom(15);
        
        addInfoRow(infoTable, "Nom - Prénom :", pv.getNomComplet(), boldFont, font);
        addInfoRow(infoTable, "CNE :", pv.getCne(), boldFont, font);
        addInfoRow(infoTable, "Filière :", pv.getFiliere(), boldFont, font);
        addInfoRow(infoTable, "Intitulé du rapport :", "_________________________", boldFont, font);
        document.add(infoTable);
        
        // Encadrant
        Table encadrantTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        encadrantTable.setWidth(UnitValue.createPercentValue(100));
        encadrantTable.setMarginBottom(15);
        addInfoRow(encadrantTable, "Encadrant(e) interne :", pv.getEncadrant(), boldFont, font);
        document.add(encadrantTable);
        
        // Membres du jury
        Paragraph juryTitle = new Paragraph("Membres du jury :")
            .setFont(boldFont).setFontSize(12).setMarginBottom(10);
        document.add(juryTitle);
        
        Table juryTable = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
        juryTable.setWidth(UnitValue.createPercentValue(100));
        juryTable.setMarginBottom(20);
        addInfoRow(juryTable, "Président :", pv.getPresident(), boldFont, font);
        addInfoRow(juryTable, "Rapporteur 1 :", pv.getRapporteur1(), boldFont, font);
        addInfoRow(juryTable, "Rapporteur 2 :", pv.getRapporteur2(), boldFont, font);
        document.add(juryTable);
        
        // Notes
        Paragraph notesTitle = new Paragraph("Notes :")
            .setFont(boldFont).setFontSize(12).setMarginBottom(10);
        document.add(notesTitle);
        
        Table notesTable = new Table(UnitValue.createPercentArray(new float[]{50, 20, 30}));
        notesTable.setWidth(UnitValue.createPercentValue(100));
        notesTable.setMarginBottom(15);
        
        addNoteRow(notesTable, "Note du Contenu (C) :", "_____", "Coef: 0.5", boldFont, font);
        addNoteRow(notesTable, "Note du Mémoire (M) :", "_____", "Coef: 0.2", boldFont, font);
        addNoteRow(notesTable, "Note de la Soutenance (S) :", "_____", "Coef: 0.3", boldFont, font);
        document.add(notesTable);
        
        // Moyenne
        Table moyenneTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        moyenneTable.setWidth(UnitValue.createPercentValue(100));
        moyenneTable.setMarginBottom(20);
        
        Cell moyenneLabel = new Cell().add(new Paragraph("Moyenne = C×0.5 + M×0.2 + S×0.3 =").setFont(boldFont));
        moyenneLabel.setBorder(null);
        Cell moyenneValue = new Cell().add(new Paragraph("_________").setFont(font));
        moyenneValue.setBorder(null);
        moyenneTable.addCell(moyenneLabel);
        moyenneTable.addCell(moyenneValue);
        document.add(moyenneTable);
        
        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Table dateTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        dateTable.setWidth(UnitValue.createPercentValue(100));
        dateTable.addCell(new Cell().add(new Paragraph("Le : " + sdf.format(new Date())).setFont(font)));
        document.add(dateTable);
        
        // Signatures
        Paragraph signTitle = new Paragraph("Signatures :").setFont(boldFont).setMarginBottom(10);
        document.add(signTitle);
        
        Table signTable = new Table(UnitValue.createPercentArray(new float[]{33, 33, 34}));
        signTable.setWidth(UnitValue.createPercentValue(100));
        signTable.addCell(new Cell().add(new Paragraph("Pr. " + pv.getPresident()).setFont(font)));
        signTable.addCell(new Cell().add(new Paragraph("Pr. " + pv.getRapporteur1()).setFont(font)));
        signTable.addCell(new Cell().add(new Paragraph("Pr. " + pv.getRapporteur2()).setFont(font)));
        
        document.add(signTable);
        document.close();
    }
    
    private void genererDOCX(ProcesVerbal pv) throws Exception {
        String fileName = "PV_" + pv.getCne() + "_" + pv.getNom() + "_" + pv.getPrenom() + ".docx";
        String filePath = basePath + "/uploads/pvs/" + fileName;
        
        XWPFDocument document = new XWPFDocument();
        
        XWPFParagraph p1 = document.createParagraph();
        p1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r1 = p1.createRun();
        r1.setText("PROCÈS-VERBAL DE SOUTENANCE PFE");
        r1.setFontSize(16);
        r1.setBold(true);
        
        XWPFParagraph p2 = document.createParagraph();
        p2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r2 = p2.createRun();
        r2.setText("ECOLE NATIONALE DES SCIENCES APPLIQUÉES - AL HOCEIMA");
        r2.setFontSize(12);
        r2.setBold(true);
        
        XWPFParagraph p3 = document.createParagraph();
        p3.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r3 = p3.createRun();
        r3.setText("Département Mathématiques et Informatique");
        r3.setFontSize(11);
        
        document.createParagraph();
        
        addDocxLine(document, "Nom - Prénom :", pv.getNomComplet());
        addDocxLine(document, "CNE :", pv.getCne());
        addDocxLine(document, "Filière :", pv.getFiliere());
        addDocxLine(document, "Intitulé du rapport :", "_________________________");
        document.createParagraph();
        
        addDocxLine(document, "Encadrant(e) interne :", pv.getEncadrant());
        document.createParagraph();
        
        XWPFParagraph juryTitle = document.createParagraph();
        XWPFRun juryRun = juryTitle.createRun();
        juryRun.setText("Membres du jury :");
        juryRun.setBold(true);
        juryRun.setFontSize(12);
        
        addDocxLine(document, "  Président :", pv.getPresident());
        addDocxLine(document, "  Rapporteur 1 :", pv.getRapporteur1());
        addDocxLine(document, "  Rapporteur 2 :", pv.getRapporteur2());
        document.createParagraph();
        
        XWPFParagraph notesTitle = document.createParagraph();
        XWPFRun notesRun = notesTitle.createRun();
        notesRun.setText("Notes :");
        notesRun.setBold(true);
        notesRun.setFontSize(12);
        
        addDocxLine(document, "Note du Contenu (C) :", "_____ (Coef: 0.5)");
        addDocxLine(document, "Note du Mémoire (M) :", "_____ (Coef: 0.2)");
        addDocxLine(document, "Note de la Soutenance (S) :", "_____ (Coef: 0.3)");
        document.createParagraph();
        
        addDocxLine(document, "Moyenne = C × 0,5 + M × 0,2 + S × 0,3 =", "_________");
        document.createParagraph();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        addDocxLine(document, "Le :", sdf.format(new Date()));
        document.createParagraph();
        addDocxLine(document, "Signatures :", "");
        
        XWPFParagraph signPara = document.createParagraph();
        XWPFRun sign1 = signPara.createRun();
        sign1.setText("Pr. " + pv.getPresident());
        sign1.addTab();
        sign1.addTab();
        sign1.addTab();
        XWPFRun sign2 = signPara.createRun();
        sign2.setText("Pr. " + pv.getRapporteur1());
        sign2.addTab();
        sign2.addTab();
        sign2.addTab();
        XWPFRun sign3 = signPara.createRun();
        sign3.setText("Pr. " + pv.getRapporteur2());
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            document.write(fos);
        }
    }
    
    private void addInfoRow(Table table, String label, String value, PdfFont boldFont, PdfFont normalFont) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(boldFont));
        labelCell.setBorder(null);
        Cell valueCell = new Cell().add(new Paragraph(value).setFont(normalFont));
        valueCell.setBorder(null);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private void addNoteRow(Table table, String label, String value, String coef, PdfFont boldFont, PdfFont normalFont) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(boldFont));
        labelCell.setBorder(null);
        Cell valueCell = new Cell().add(new Paragraph(value).setFont(normalFont));
        valueCell.setBorder(null);
        Cell coefCell = new Cell().add(new Paragraph(coef).setFont(normalFont));
        coefCell.setBorder(null);
        table.addCell(labelCell);
        table.addCell(valueCell);
        table.addCell(coefCell);
    }
    
    private void addDocxLine(XWPFDocument doc, String label, String value) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(label);
        run.setBold(true);
        run.addTab();
        run.addTab();
        XWPFRun runValue = para.createRun();
        runValue.setText(value);
    }
}