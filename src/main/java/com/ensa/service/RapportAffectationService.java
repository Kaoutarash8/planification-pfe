package com.ensa.service;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.util.FileUploadUtil;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class RapportAffectationService {

    private String webappPath;
    private Map<String, Color> couleurPDFParFiliere = new LinkedHashMap<>();
    private Map<String, String> couleurDOCXParFiliere = new LinkedHashMap<>();

    private final Color[] PALETTE_PDF = {
        new DeviceRgb(173, 216, 230), new DeviceRgb(144, 238, 144),
        new DeviceRgb(255, 218, 185), new DeviceRgb(255, 255, 180),
        new DeviceRgb(221, 160, 221), new DeviceRgb(255, 182, 193),
        new DeviceRgb(176, 224, 230)
    };

    private final String[] PALETTE_DOCX = {
        "D4E6F1", "D5F5E3", "FAD7A1", "FCF3CF", "E8DAEF", "FADBD8", "D0ECE7"
    };

    private static final DeviceRgb COLOR_HEADER_BG = new DeviceRgb(0, 51, 160);
    private static final DeviceRgb COLOR_HEADER_FG = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_BORDER = new DeviceRgb(180, 180, 180);
    private static final DeviceRgb COLOR_SUPERVISOR_BG = new DeviceRgb(245, 245, 245);

    public RapportAffectationService(String webappPath) {
        this.webappPath = webappPath;
    }

    public void genererTousLesRapports(Map<Professeur, List<Etudiant>> affectation, String timestamp) throws Exception {
        // Utiliser le dossier Affectation du projet
        String affectationPath = FileUploadUtil.getAffectationPath();
        File affectationDir = new File(affectationPath);
        if (!affectationDir.exists()) affectationDir.mkdirs();
        
        // Supprimer les anciens PDF et DOCX
        File[] anciensPDF = affectationDir.listFiles((dir, name) -> name.endsWith(".pdf") || name.endsWith(".docx"));
        if (anciensPDF != null) {
            for (File f : anciensPDF) {
                f.delete();
                System.out.println("Ancien rapport supprimé: " + f.getName());
            }
        }

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String yearTimestamp = String.valueOf(year);

        Set<String> filieresUniques = new LinkedHashSet<>();
        for (List<Etudiant> list : affectation.values()) {
            for (Etudiant e : list) {
                String f = e.getFiliere();
                filieresUniques.add((f == null || f.isEmpty()) ? "INFO" : f);
            }
        }

        int ci = 0;
        for (String f : filieresUniques) {
            couleurPDFParFiliere.put(f, PALETTE_PDF[ci % PALETTE_PDF.length]);
            couleurDOCXParFiliere.put(f, PALETTE_DOCX[ci % PALETTE_DOCX.length]);
            ci++;
        }

        // GARDER L'ORDRE ORIGINAL DE L'AFFECTATION (PAS DE TRI)
        genererPDF(affectation, yearTimestamp, affectationDir.getAbsolutePath(), filieresUniques);
        genererDOCX(affectation, yearTimestamp, affectationDir.getAbsolutePath(), filieresUniques);

        System.out.println("=== RAPPORTS GÉNÉRÉS (ordre aléatoire original) ===");
        System.out.println("PDF: " + affectationDir.getAbsolutePath() + "/affectation_" + yearTimestamp + ".pdf");
        System.out.println("DOCX: " + affectationDir.getAbsolutePath() + "/affectation_" + yearTimestamp + ".docx");
    }

    private void genererPDF(Map<Professeur, List<Etudiant>> affectation, String yearTimestamp,
                            String rapportsPath, Set<String> filieresUniques) throws Exception {
        String filePath = rapportsPath + "/affectation_" + yearTimestamp + ".pdf";

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PageSize pageSize = new PageSize(842, 595);
        pdfDoc.setDefaultPageSize(pageSize);

        Document document = new Document(pdfDoc);
        document.setMargins(25, 25, 25, 25);

        PdfFont fontNormal = PdfFontFactory.createFont("Helvetica");
        PdfFont fontBold = PdfFontFactory.createFont("Helvetica-Bold");

        Paragraph ecole = new Paragraph("ECOLE NATIONALE DES SCIENCES APPLIQUÉES - AL HOCEIMA")
                .setFont(fontBold).setFontSize(13).setTextAlignment(TextAlignment.CENTER).setMarginBottom(4);
        document.add(ecole);

        Paragraph dept = new Paragraph("Département Mathématiques et Informatique")
                .setFont(fontNormal).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(4);
        document.add(dept);

        Paragraph titre = new Paragraph("Affectation des encadrants de Projet de Fin d'Etude")
                .setFont(fontBold).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setMarginBottom(2);
        document.add(titre);

        Paragraph annee = new Paragraph("Année Universitaire " + yearTimestamp + "/" + (Integer.parseInt(yearTimestamp) + 1))
                .setFont(fontNormal).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(12);
        document.add(annee);

        Table filieresTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        filieresTable.setWidth(UnitValue.createPercentValue(50));
        filieresTable.setMarginBottom(12);
        filieresTable.setMarginLeft(25);
        filieresTable.setMarginRight(25);

        List<String> filieresList = new ArrayList<>(filieresUniques);
        
        for (int i = 0; i < 3; i++) {
            Cell filiereCell = new Cell();
            if (i < filieresList.size()) {
                String filiere = filieresList.get(i);
                Color couleur = couleurPDFParFiliere.get(filiere);
                Paragraph filierePara = new Paragraph();
                filierePara.add("• ").setFontColor(couleur).setFontSize(12);
                filierePara.add(filiere).setFont(fontBold).setFontSize(9);
                filiereCell.add(filierePara);
            } else {
                filiereCell.add(new Paragraph(""));
            }
            filiereCell.setBorder(new SolidBorder(COLOR_BORDER, 0.5f));
            filiereCell.setTextAlignment(TextAlignment.CENTER);
            filiereCell.setPadding(6);
            filieresTable.addCell(filiereCell);
        }
        document.add(filieresTable);

        int maxEtudiants = affectation.values().stream().mapToInt(List::size).max().orElse(1);

        float[] colWidths = new float[maxEtudiants + 1];
        colWidths[0] = 15f;
        float etudiantWidth = 85f / maxEtudiants;
        for (int i = 1; i <= maxEtudiants; i++) {
            colWidths[i] = etudiantWidth;
        }

        Table table = new Table(UnitValue.createPercentArray(colWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setBorder(new SolidBorder(COLOR_BORDER, 0.8f));

        Cell headerEnc = new Cell().add(new Paragraph("ENCADRANT").setFont(fontBold).setFontSize(9));
        headerEnc.setBackgroundColor(COLOR_HEADER_BG);
        headerEnc.setFontColor(COLOR_HEADER_FG);
        headerEnc.setTextAlignment(TextAlignment.CENTER);
        headerEnc.setVerticalAlignment(VerticalAlignment.MIDDLE);
        headerEnc.setPaddingTop(6);
        headerEnc.setPaddingBottom(6);
        headerEnc.setPaddingLeft(4);
        headerEnc.setPaddingRight(4);
        table.addCell(headerEnc);

        for (int i = 1; i <= maxEtudiants; i++) {
            Cell headerCell = new Cell().add(new Paragraph("ETUDIANT " + i).setFont(fontBold).setFontSize(9));
            headerCell.setBackgroundColor(COLOR_HEADER_BG);
            headerCell.setFontColor(COLOR_HEADER_FG);
            headerCell.setTextAlignment(TextAlignment.CENTER);
            headerCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            headerCell.setPaddingTop(6);
            headerCell.setPaddingBottom(6);
            headerCell.setPaddingLeft(4);
            headerCell.setPaddingRight(4);
            table.addCell(headerCell);
        }

        // GARDER L'ORDRE ORIGINAL DE L'AFFECTATION
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            Professeur prof = entry.getKey();
            List<Etudiant> etudiants = entry.getValue();
            
            String encText = prof.getNom().toUpperCase() + " " + prof.getPrenom() + " (" + etudiants.size() + ")";

            Cell encCell = new Cell().add(new Paragraph(encText).setFont(fontBold).setFontSize(8.5f));
            encCell.setBackgroundColor(COLOR_SUPERVISOR_BG);
            encCell.setTextAlignment(TextAlignment.CENTER);
            encCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            encCell.setPaddingTop(4);
            encCell.setPaddingBottom(4);
            encCell.setPaddingLeft(4);
            encCell.setPaddingRight(4);
            encCell.setBorderRight(new SolidBorder(COLOR_BORDER, 0.8f));
            table.addCell(encCell);

            for (int i = 0; i < maxEtudiants; i++) {
                Cell etudiantCell = new Cell();
                if (i < etudiants.size()) {
                    Etudiant e = etudiants.get(i);
                    String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "INFO" : e.getFiliere();
                    Color bg = couleurPDFParFiliere.getOrDefault(filiere, PALETTE_PDF[0]);
                    etudiantCell.add(new Paragraph(e.getPrenom() + " " + e.getNom().toUpperCase())
                            .setFont(fontNormal).setFontSize(7.5f));
                    etudiantCell.setBackgroundColor(bg);
                } else {
                    etudiantCell.add(new Paragraph(""));
                    etudiantCell.setBackgroundColor(new DeviceRgb(252, 252, 252));
                }
                etudiantCell.setTextAlignment(TextAlignment.CENTER);
                etudiantCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
                etudiantCell.setPaddingTop(4);
                etudiantCell.setPaddingBottom(4);
                etudiantCell.setPaddingLeft(4);
                etudiantCell.setPaddingRight(4);
                etudiantCell.setBorder(new SolidBorder(COLOR_BORDER, 0.4f));
                table.addCell(etudiantCell);
            }
        }

        document.add(table);
        document.close();
        System.out.println("PDF généré : " + filePath);
    }

    private void genererDOCX(Map<Professeur, List<Etudiant>> affectation, String yearTimestamp,
                             String rapportsPath, Set<String> filieresUniques) throws Exception {
        String filePath = rapportsPath + "/affectation_" + yearTimestamp + ".docx";
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph p1 = document.createParagraph();
        p1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r1 = p1.createRun();
        r1.setText("ECOLE NATIONALE DES SCIENCES APPLIQUÉES - AL HOCEIMA");
        r1.setFontSize(13);
        r1.setBold(true);
        r1.setFontFamily("Arial");

        XWPFParagraph p2 = document.createParagraph();
        p2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r2 = p2.createRun();
        r2.setText("Département Mathématiques et Informatique");
        r2.setFontSize(10);
        r2.setFontFamily("Arial");

        XWPFParagraph p3 = document.createParagraph();
        p3.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r3 = p3.createRun();
        r3.setText("Affectation des encadrants de Projet de Fin d'Etude");
        r3.setFontSize(11);
        r3.setBold(true);
        r3.setFontFamily("Arial");

        XWPFParagraph p4 = document.createParagraph();
        p4.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r4 = p4.createRun();
        r4.setText("Année Universitaire " + yearTimestamp + "/" + (Integer.parseInt(yearTimestamp) + 1));
        r4.setFontSize(10);
        r4.setFontFamily("Arial");

        document.createParagraph();

        XWPFTable filieresTable = document.createTable(1, 3);
        filieresTable.setWidth("50%");
        filieresTable.setTableAlignment(TableRowAlign.CENTER);
        
        XWPFTableRow filieresRow = filieresTable.getRow(0);
        List<String> filieresList = new ArrayList<>(filieresUniques);
        
        for (int i = 0; i < 3; i++) {
            XWPFTableCell cell = filieresRow.getCell(i);
            if (cell == null) cell = filieresRow.addNewTableCell();
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            
            if (i < filieresList.size()) {
                String filiere = filieresList.get(i);
                String couleur = couleurDOCXParFiliere.get(filiere);
                cell.setColor(couleur);
                cell.setText("• " + filiere);
                
                XWPFParagraph cellPara = cell.getParagraphs().get(0);
                cellPara.setAlignment(ParagraphAlignment.CENTER);
                if (!cellPara.getRuns().isEmpty()) {
                    cellPara.getRuns().get(0).setBold(true);
                    cellPara.getRuns().get(0).setFontSize(9);
                    cellPara.getRuns().get(0).setFontFamily("Arial");
                }
            } else {
                cell.setText("");
            }
        }

        document.createParagraph();

        int maxEtudiants = 0;
        for (List<Etudiant> list : affectation.values()) {
            if (list.size() > maxEtudiants) {
                maxEtudiants = list.size();
            }
        }

        XWPFTable table = document.createTable(affectation.size() + 1, maxEtudiants + 1);
        table.setWidth("100%");
        table.setTableAlignment(TableRowAlign.CENTER);

        XWPFTableRow headerRow = table.getRow(0);
        setDocxHeaderCell(headerRow.getCell(0), "ENCADRANT (nb)", 9);
        for (int i = 1; i <= maxEtudiants; i++) {
            setDocxHeaderCell(headerRow.getCell(i), "ETUDIANT " + i, 9);
        }

        int rowIndex = 1;
        for (Map.Entry<Professeur, List<Etudiant>> entry : affectation.entrySet()) {
            Professeur prof = entry.getKey();
            List<Etudiant> etudiants = entry.getValue();
            
            XWPFTableRow row = table.getRow(rowIndex);
            if (row == null) {
                row = table.createRow();
            }
            
            while (row.getTableCells().size() <= maxEtudiants) {
                row.addNewTableCell();
            }
            
            String encText = prof.getNom().toUpperCase() + " " + prof.getPrenom() + " (" + etudiants.size() + ")";
            row.getCell(0).setText(encText);
            row.getCell(0).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            styleNeutralCell(row.getCell(0));
            
            for (int i = 0; i < maxEtudiants; i++) {
                if (i < etudiants.size()) {
                    Etudiant e = etudiants.get(i);
                    String filiere = (e.getFiliere() == null || e.getFiliere().isEmpty()) ? "INFO" : e.getFiliere();
                    String couleurCellule = couleurDOCXParFiliere.getOrDefault(filiere, "D4E6F1");
                    row.getCell(i + 1).setText(e.getPrenom() + " " + e.getNom().toUpperCase());
                    row.getCell(i + 1).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    styleDataCell(row.getCell(i + 1), couleurCellule);
                } else {
                    row.getCell(i + 1).setText("");
                    row.getCell(i + 1).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    styleDataCell(row.getCell(i + 1), "FFFFFF");
                }
            }
            
            rowIndex++;
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            document.write(fos);
        }
        System.out.println("DOCX généré : " + filePath);
    }

    private void setDocxHeaderCell(XWPFTableCell cell, String text, int fontSize) {
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        p.getRuns().forEach(r -> r.setText("", 0));
        XWPFRun r = p.getRuns().isEmpty() ? p.createRun() : p.getRuns().get(0);
        r.setText(text);
        r.setFontSize(fontSize);
        r.setBold(true);
        r.setColor("FFFFFF");
        r.setFontFamily("Arial");
        cell.setColor("0033A0");
    }
    
    private void styleNeutralCell(XWPFTableCell cell) {
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        if (p.getRuns().isEmpty()) {
            XWPFRun r = p.createRun();
            r.setText(cell.getText());
            r.setFontSize(9);
            r.setBold(true);
            r.setFontFamily("Arial");
        } else {
            p.getRuns().get(0).setFontSize(9);
            p.getRuns().get(0).setBold(true);
            p.getRuns().get(0).setFontFamily("Arial");
        }
        cell.setColor("F0F0F0");
    }

    private void styleDataCell(XWPFTableCell cell, String bgColor) {
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        if (p.getRuns().isEmpty()) {
            XWPFRun r = p.createRun();
            r.setText(cell.getText());
            r.setFontSize(8);
            r.setFontFamily("Arial");
        } else {
            p.getRuns().get(0).setFontSize(8);
            p.getRuns().get(0).setFontFamily("Arial");
        }
        cell.setColor(bgColor);
    }
}