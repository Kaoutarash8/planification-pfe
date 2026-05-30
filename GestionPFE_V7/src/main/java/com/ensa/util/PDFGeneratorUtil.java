package com.ensa.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PDFGeneratorUtil {

    private static final float ML = 50f, MR = 50f, MT = 28f, MB = 28f;
    private static final float F_SMALL  =  8.5f;
    private static final float F_NORMAL =  9.5f;
    private static final float F_LABEL  = 10.0f;
    private static final float F_TITLE  = 11.5f;
    private static final float F_HEAD   = 12.5f;
    private static final float SP_XS  = 2f;
    private static final float SP_S   = 4f;
    private static final float SP_M   = 7f;
    private static final float SP_L   = 10f;
    private static final DeviceRgb C_BLACK      = new DeviceRgb(20,  20,  20);
    private static final DeviceRgb C_DARK_GREY  = new DeviceRgb(55,  55,  55);
    private static final DeviceRgb C_MID_GREY   = new DeviceRgb(110, 110, 110);
    private static final DeviceRgb C_LIGHT_GREY = new DeviceRgb(220, 220, 220);
    private static final DeviceRgb C_BOX_GREY   = new DeviceRgb(235, 235, 235);
    private static final DeviceRgb C_BORDER     = new DeviceRgb(180, 180, 180);
    private static final DeviceRgb C_WHITE      = new DeviceRgb(255, 255, 255);

    public static byte[] generateSimpleEvaluationPDF(
            String logoEnsaPath,
            String logoUaePath,
            String encadrantNom,
            String etudiantNom,
            String filiere,
            String jury1,
            String jury2,
            String dateSoutenance) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter   writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document    doc    = new Document(pdfDoc, PageSize.A4)) {

            doc.setMargins(MT, MR, MB, ML);

            PdfFont fNormal = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            PdfFont fBold   = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
            PdfFont fItalic = PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC);

            addHeader(doc, fNormal, fBold, logoEnsaPath, logoUaePath);
            addTitreFiche(doc, fNormal, fBold);
            addHRule(doc, 1.2f, C_BLACK);
            addSectionLabel(doc, fBold, "Nom - Prenom de l'eleve ingenieur :");
            addPointLine(doc, fNormal, safe(etudiantNom));
            addSectionLabel(doc, fBold, "Filiere :");
            addPointLine(doc, fNormal, getFiliereNom(filiere));
            addSectionLabel(doc, fBold, "Intitule du rapport :");
            addPointLine(doc, fNormal, null);
            addSectionLabel(doc, fBold, "L'encadrant(e) interne :");
            addPointLine(doc, fNormal, "Pr.  " + safe(encadrantNom));
            addSectionLabel(doc, fBold, "Membres du jury :");
            addJuryLines(doc, fNormal, encadrantNom, jury1, jury2);
            addHRule(doc, 0.5f, C_BORDER);
            addNotesSection(doc, fNormal, fBold, fItalic);
            addMoyenneBox(doc, fNormal, fBold);

            String dateStr = (dateSoutenance != null && !dateSoutenance.isEmpty())
                    ? dateSoutenance
                    : new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            doc.add(new Paragraph("Le :   " + dateStr)
                    .setFont(fNormal).setFontSize(F_NORMAL)
                    .setFontColor(C_BLACK).setMarginTop(SP_S).setMarginBottom(SP_S));

            addSignatures(doc, fNormal, fBold, encadrantNom, jury1, jury2);
        }

        return baos.toByteArray();
    }

    public static byte[] generateSimpleEvaluationPDF(
            String logoEnsaPath,
            String logoUaePath,
            String encadrantNom,
            String etudiantNom,
            String filiere,
            String president,
            String rapporteur1,
            String rapporteur2,
            String dateSoutenance) throws Exception {
        
        return generateSimpleEvaluationPDF(
            logoEnsaPath, logoUaePath,
            encadrantNom,
            etudiantNom,
            filiere,
            rapporteur1,
            rapporteur2,
            dateSoutenance);
    }

    public static byte[] generateCompleteEvaluationPDF(
            String logoEnsaPath, String logoUaePath,
            String encadrantNom, String etudiantNom, String filiere,
            String president, String rapporteur1, String rapporteur2,
            double noteContenu, double noteMemoire, double noteSoutenance,
            String dateSoutenance) throws Exception {

        return generateSimpleEvaluationPDF(logoEnsaPath, logoUaePath,
                encadrantNom, etudiantNom, filiere, rapporteur1, rapporteur2, dateSoutenance);
    }

    private static void addHeader(Document doc, PdfFont fNormal, PdfFont fBold,
                                   String logoEnsaPath, String logoUaePath) throws Exception {

        Table t = new Table(new float[]{70f, 360f, 70f})
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER).setMarginBottom(SP_S);

        Cell cLeft = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        if (logoEnsaPath != null && new File(logoEnsaPath).exists()) {
            cLeft.add(new Image(ImageDataFactory.create(logoEnsaPath)).setWidth(60f));
        }
        t.addCell(cLeft);

        Cell cCenter = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph("UNIVERSITE ABDELMALEK ESSAADI")
                        .setFont(fBold).setFontSize(F_HEAD)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(C_BLACK).setMarginBottom(SP_XS))
                .add(new Paragraph("Ecole Nationale des Sciences Appliquees d'Al-Hoceima - Maroc")
                        .setFont(fNormal).setFontSize(F_SMALL)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(C_DARK_GREY).setMarginBottom(0));
        t.addCell(cCenter);

        Cell cRight = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);
        if (logoUaePath != null && new File(logoUaePath).exists()) {
            cRight.add(new Image(ImageDataFactory.create(logoUaePath))
                    .setWidth(60f).setHorizontalAlignment(HorizontalAlignment.RIGHT));
        }
        t.addCell(cRight);

        doc.add(t);
        addHRule(doc, 1.5f, C_BLACK);
    }

    private static void addTitreFiche(Document doc, PdfFont fNormal, PdfFont fBold) {
        doc.add(new Paragraph("Departement de Mathematiques et Informatique")
                .setFont(fBold).setFontSize(F_NORMAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(C_DARK_GREY).setMarginTop(SP_S).setMarginBottom(SP_XS));

        doc.add(new Paragraph("Fiche d'evaluation du Projet de Fin d'Etude")
                .setFont(fBold).setFontSize(F_TITLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(C_BLACK).setMarginBottom(SP_XS));

        doc.add(new Paragraph("Annee Universitaire : " + getAnneeUniversitaire())
                .setFont(fNormal).setFontSize(F_NORMAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(C_DARK_GREY).setMarginBottom(SP_S));
    }

    private static void addSectionLabel(Document doc, PdfFont fBold, String txt) {
        doc.add(new Paragraph(txt)
                .setFont(fBold).setFontSize(F_LABEL)
                .setFontColor(C_BLACK)
                .setUnderline()
                .setMarginTop(SP_S).setMarginBottom(SP_XS));
    }

    private static void addPointLine(Document doc, PdfFont fNormal, String valeur) {
        String content = (valeur == null || valeur.isBlank())
                ? "  \u2013  \u00a0" + dots(62)
                : "  \u2013  " + valeur;
        doc.add(new Paragraph(content)
                .setFont(fNormal).setFontSize(F_NORMAL)
                .setFontColor(C_BLACK).setMarginBottom(SP_XS));
    }

    private static void addJuryLines(Document doc, PdfFont fNormal, String encadrant, String jury1, String jury2) {

        String[] noms = {
            "Pr.  " + safe(encadrant),
            "Pr.  " + safe(jury1),
            "Pr.  " + safe(jury2)
        };
        String[] roles = { "President", "Rapporteur", "Rapporteur" };

        for (int i = 0; i < 3; i++) {
            Table row = new Table(new float[]{310f, 130f})
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER).setMarginBottom(SP_XS);

            String nomDisplay = noms[i].isBlank() || noms[i].equals("Pr.  ")
                    ? "  \u2013  Pr.  " + dots(42)
                    : "  \u2013  " + noms[i];

            row.addCell(new Cell().setBorder(Border.NO_BORDER)
                    .add(new Paragraph(nomDisplay)
                            .setFont(fNormal).setFontSize(F_NORMAL).setFontColor(C_BLACK)));
            row.addCell(new Cell().setBorder(Border.NO_BORDER)
                    .add(new Paragraph(roles[i])
                            .setFont(fNormal).setFontSize(F_NORMAL).setFontColor(C_DARK_GREY)
                            .setTextAlignment(TextAlignment.RIGHT)));
            doc.add(row);
        }
    }

    private static void addNotesSection(Document doc, PdfFont fNormal, PdfFont fBold, PdfFont fItalic) {

        doc.add(new Paragraph(SP_S + "").setMarginBottom(0));

        Paragraph pC = new Paragraph()
                .setFontSize(F_LABEL).setMarginBottom(SP_XS);
        pC.add(new Text("Note du Contenu").setFont(fBold).setUnderline().setFontColor(C_BLACK));
        pC.add(new Text("  (En prenant en compte l'appreciation de l'entreprise)")
                .setFont(fItalic).setFontSize(F_SMALL).setFontColor(C_MID_GREY));
        doc.add(pC);
        doc.add(new Paragraph("      C  =  " + dots(10))
                .setFont(fNormal).setFontSize(F_NORMAL).setFontColor(C_BLACK).setMarginBottom(SP_S));

        doc.add(new Paragraph("Note du Memoire")
                .setFont(fBold).setFontSize(F_LABEL).setUnderline().setFontColor(C_BLACK).setMarginBottom(SP_XS));
        doc.add(new Paragraph("      M  =  " + dots(10))
                .setFont(fNormal).setFontSize(F_NORMAL).setFontColor(C_BLACK).setMarginBottom(SP_S));

        doc.add(new Paragraph("Note de la Soutenance")
                .setFont(fBold).setFontSize(F_LABEL).setUnderline().setFontColor(C_BLACK).setMarginBottom(SP_XS));
        doc.add(new Paragraph("      S  =  " + dots(10))
                .setFont(fNormal).setFontSize(F_NORMAL).setFontColor(C_BLACK).setMarginBottom(SP_S));
    }

    private static void addMoyenneBox(Document doc, PdfFont fNormal, PdfFont fBold) {
        Table t = new Table(new float[]{1})
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(SP_XS).setMarginBottom(SP_M);

        t.addCell(new Cell()
                .add(new Paragraph("MOYENNE")
                        .setFont(fBold).setFontSize(F_LABEL)
                        .setTextAlignment(TextAlignment.CENTER).setFontColor(C_WHITE))
                .setBackgroundColor(C_DARK_GREY)
                .setBorder(new SolidBorder(C_BORDER, 0.8f))
                .setPaddingTop(4).setPaddingBottom(4));

        t.addCell(new Cell()
                .add(new Paragraph("Moyenne   =  C × 0,5  +  M × 0,2  +  S × 0,3  =  " + dots(12))
                        .setFont(fBold).setFontSize(F_NORMAL)
                        .setFontColor(C_BLACK))
                .setBackgroundColor(C_BOX_GREY)
                .setBorder(new SolidBorder(C_BORDER, 0.8f))
                .setPaddingLeft(12).setPaddingTop(6).setPaddingBottom(6));

        doc.add(t);
    }

    private static void addSignatures(Document doc, PdfFont fNormal, PdfFont fBold,
                                       String encadrant, String jury1, String jury2) {
        doc.add(new Paragraph("Signature des membres du jury :")
                .setFont(fBold).setFontSize(F_LABEL).setFontColor(C_BLACK)
                .setMarginBottom(SP_S));

        Table t = new Table(new float[]{165f, 165f, 165f})
                .setWidth(UnitValue.createPercentValue(100)).setBorder(Border.NO_BORDER);

        String enc = "Pr.  " + safe(encadrant);
        String j1 = "Pr.  " + safe(jury1);
        String j2 = "Pr.  " + safe(jury2);
        String[] noms = { enc, j1, j2 };

        for (String nom : noms) {
            String display = (nom.equals("Pr.  ") ? "Pr.  " + dots(14) : nom + "  " + dots(6));
            t.addCell(new Cell().setBorder(Border.NO_BORDER)
                    .add(new Paragraph(display)
                            .setFont(fNormal).setFontSize(F_NORMAL).setFontColor(C_BLACK)));
        }
        doc.add(t);
    }

    private static void addHRule(Document doc, float width, DeviceRgb color) {
        Table sep = new Table(new float[]{1})
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(color, width))
                .setMarginBottom(SP_S);
        doc.add(sep);
    }

    private static String dots(int n) {
        return ".".repeat(Math.max(0, n));
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static String getFiliereNom(String filiere) {
        if (filiere == null || filiere.trim().isEmpty()) return "";
        String f = filiere.toUpperCase();
        if (f.contains("DATA")) return "Ingenierie des Donnees";
        if (f.contains("INFO")) return "Genie Informatique";
        if (f.contains("TDIA")) return "Transformation Digitale & Intelligence Artificielle";
        return filiere;
    }

    private static String getAnneeUniversitaire() {
        Calendar cal  = Calendar.getInstance();
        int mois  = cal.get(Calendar.MONTH);
        int annee = cal.get(Calendar.YEAR);
        return (mois >= 8) ? annee + "-" + (annee + 1) : (annee - 1) + "-" + annee;
    }
}