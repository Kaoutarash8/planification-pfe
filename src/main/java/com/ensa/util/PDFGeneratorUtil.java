package com.ensa.util;

import com.ensa.model.Etudiant;
import com.ensa.model.Professeur;
import com.ensa.service.interfaces.PVGenerationService.Note;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
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
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Génère une fiche d'évaluation PFE au format PDF
 * identique au modèle officiel ENSA Al-Hoceima.
 *
 * Dépendances Maven (iText 7 + Jakarta) :
 * <dependency>
 *   <groupId>com.itextpdf</groupId>
 *   <artifactId>itext7-core</artifactId>
 *   <version>7.2.5</version>
 *   <type>pom</type>
 * </dependency>
 */
public class PDFGeneratorUtil {

    // ─── Marges (en points, 1 pt ≈ 0.353 mm) ──────────────────────────────
    private static final float MARGIN        = 50f;
    private static final float FONT_NORMAL   = 10f;
    private static final float FONT_TITLE    = 11f;
    private static final float FONT_HEADER   = 12f;
    private static final float SPACE_SMALL   = 6f;
    private static final float SPACE_MEDIUM  = 10f;
    private static final float SPACE_LARGE   = 14f;

    // ─── Couleur gris clair pour l'en-tête du tableau MOYENNE ──────────────
    private static final DeviceRgb GREY_LIGHT = new DeviceRgb(220, 220, 220);

    // ═══════════════════════════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * @param encadrant       Professeur encadrant interne
     * @param etudiants       Liste des étudiants
     * @param notes           Liste des notes (même ordre que etudiants)
     * @param president       Nom du président du jury
     * @param rapporteur1     Nom du 1er rapporteur
     * @param rapporteur2     Nom du 2ème rapporteur
     * @param dateSoutenance  Date au format dd/MM/yyyy (ou null pour aujourd'hui)
     * @return tableau d'octets du PDF généré
     */
    public static byte[] generateEvaluationPDF(
            Professeur encadrant,
            List<Etudiant> etudiants,
            List<Note> notes,
            String president,
            String rapporteur1,
            String rapporteur2,
            String dateSoutenance) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc, PageSize.A4)) {

            doc.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

            // Polices
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
            PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC);
            PdfFont fontBoldIt = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);

            // ── 1. EN-TÊTE ────────────────────────────────────────────────
            addHeader(doc, fontNormal, fontBold, etudiants);

            // ── 2. NOM – PRÉNOM ───────────────────────────────────────────
            addSectionLabel(doc, fontBold, "Nom - Pr\u00e9nom de l\u2019\u00e9l\u00e8ve ing\u00e9nieur :");
            for (Etudiant e : etudiants) {
                String name = (e.getPrenom() + " " + e.getNom()).trim();
                addDotLine(doc, fontNormal, name.isEmpty() ? "" : name);
            }
            addSpace(doc, SPACE_MEDIUM);

            // ── 3. FILIÈRE ────────────────────────────────────────────────
            addFiliere(doc, fontNormal, fontBold, etudiants);
            addSpace(doc, SPACE_MEDIUM);

            // ── 4. INTITULÉ DU RAPPORT ────────────────────────────────────
         // ── 4. INTITULÉ DU RAPPORT ────────────────────────────────────────────
            addSectionLabel(doc, fontBold, "Intitul\u00e9 du rapport :");
            addDotLine(doc, fontNormal, "");   // ← vide = pointillés à remplir manuellement
            addSpace(doc, SPACE_MEDIUM);

            // ── 5. ENCADRANT ──────────────────────────────────────────────
            addSectionLabel(doc, fontBold, "L\u2019encadrant (e) interne:");
            String encNom = (encadrant == null) ? "" : encadrant.getNomComplet();
            addDotLine(doc, fontNormal, encNom.isEmpty() ? "" : "Pr. " + encNom);
            addSpace(doc, SPACE_MEDIUM);

            // ── 6. MEMBRES DU JURY ────────────────────────────────────────
            addJury(doc, fontNormal, fontBold, president, rapporteur1, rapporteur2);
            addSpace(doc, SPACE_MEDIUM);

            // ── 7. NOTES C / M / S ────────────────────────────────────────
            Note noteRef = (notes != null && !notes.isEmpty()) ? notes.get(0) : null;
            addNotes(doc, fontNormal, fontBold, fontItalic, noteRef);

            // ── 8. BOITE MOYENNE ──────────────────────────────────────────
            addMoyenneBox(doc, fontNormal, fontBold, noteRef);
            addSpace(doc, SPACE_LARGE);

            // ── 9. DATE ───────────────────────────────────────────────────
            String dateStr = (dateSoutenance != null && !dateSoutenance.isEmpty())
                    ? dateSoutenance
                    : new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            doc.add(new Paragraph("Le :   " + dateStr)
                    .setFont(fontNormal).setFontSize(FONT_NORMAL)
                    .setMarginBottom(SPACE_MEDIUM));

            // ── 10. SIGNATURES ────────────────────────────────────────────
            addSignatures(doc, fontNormal);
        }

        return baos.toByteArray();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SECTIONS PRIVÉES
    // ═══════════════════════════════════════════════════════════════════════

    /** En-tête : université + département + titre fiche */
    private static void addHeader(Document doc, PdfFont fontNormal, PdfFont fontBold,
                                  List<Etudiant> etudiants) throws Exception {

        // Ligne université
        doc.add(new Paragraph("UNIVERSITE ABDELMALEK ESSAADI")
                .setFont(fontBold).setFontSize(FONT_HEADER)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        doc.add(new Paragraph("Ecole Nationale des Sciences Appliqu\u00e9es d\u2019Al-Hoceima - Maroc")
                .setFont(fontNormal).setFontSize(FONT_NORMAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(SPACE_SMALL));

        doc.add(new Paragraph("D\u00e9partement de Math\u00e9matiques et Informatique")
                .setFont(fontBold).setFontSize(FONT_TITLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        doc.add(new Paragraph("Fiche d\u2019\u00e9valuation du Projet de Fin d\u2019\u00c9tude")
                .setFont(fontBold).setFontSize(FONT_TITLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

    }

    /** Label de section souligné gras */
    private static void addSectionLabel(Document doc, PdfFont fontBold, String text) {
        doc.add(new Paragraph(text)
                .setFont(fontBold).setFontSize(FONT_NORMAL)
                .setUnderline()
                .setMarginBottom(4));
    }

    /** Ligne  "  –  valeur ou ............" */
    private static void addDotLine(Document doc, PdfFont fontNormal, String value) {
        String display = (value == null || value.isBlank())
                ? "\u2013  " + dots(60)
                : "\u2013  " + value;
        doc.add(new Paragraph("    " + display)
                .setFont(fontNormal).setFontSize(FONT_NORMAL)
                .setMarginBottom(3));
    }

    /** Filière avec cases à cocher */
    private static void addFiliere(Document doc, PdfFont fontNormal, PdfFont fontBold,
                                   List<Etudiant> etudiants) {

        String filiere = (etudiants != null && !etudiants.isEmpty())
                ? etudiants.get(0).getFiliere() : "";

        boolean isID = "Ing\u00e9nierie des Donn\u00e9es".equalsIgnoreCase(filiere)
                    || "ID".equalsIgnoreCase(filiere);
        boolean isGI = "G\u00e9nie Informatique".equalsIgnoreCase(filiere)
                    || "GI".equalsIgnoreCase(filiere);

        String cbID = isID ? "\u2611" : "\u2610";   // ☑ ou ☐
        String cbGI = isGI ? "\u2611" : "\u2610";

        Paragraph p = new Paragraph()
                .setFont(fontNormal).setFontSize(FONT_NORMAL);
        p.add(new Text("Fili\u00e8re :  ").setFont(fontBold).setUnderline());
        p.add(new Text(cbID + "  Ing\u00e9nierie des Donn\u00e9es      "));
        p.add(new Text(cbGI + "  G\u00e9nie Informatique"));
        doc.add(p);
    }

    /** Section Membres du jury avec rôles alignés à droite */
    private static void addJury(Document doc, PdfFont fontNormal, PdfFont fontBold,
                                 String president, String rapporteur1, String rapporteur2) {

        addSectionLabel(doc, fontBold, "Membres du jury :");

        // Table 2 colonnes : nom | rôle
        float[] cols = {420f, 80f};
        Table table = new Table(cols).setWidth(UnitValue.createPercentValue(100));
        table.setBorder(Border.NO_BORDER);

        addJuryRow(table, fontNormal, president,   "Pr\u00e9sident");
        addJuryRow(table, fontNormal, rapporteur1, "Rapporteur");
        addJuryRow(table, fontNormal, rapporteur2, "Rapporteur");

        doc.add(table);
    }

    private static void addJuryRow(Table table, PdfFont fontNormal, String name, String role) {
        String display = (name == null || name.isBlank())
                ? "    \u2013  Pr. " + dots(45)
                : "    \u2013  Pr. " + name;

        Cell cellName = new Cell()
                .add(new Paragraph(display).setFont(fontNormal).setFontSize(FONT_NORMAL))
                .setBorder(Border.NO_BORDER)
                .setPadding(2);

        Cell cellRole = new Cell()
                .add(new Paragraph(role).setFont(fontNormal).setFontSize(FONT_NORMAL)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setPadding(2);

        table.addCell(cellName);
        table.addCell(cellRole);
    }

    /** Notes C, M, S */
    private static void addNotes(Document doc, PdfFont fontNormal, PdfFont fontBold,
                                  PdfFont fontItalic, Note note) {

        // Note du Contenu
        Paragraph pC = new Paragraph()
                .setFont(fontNormal).setFontSize(FONT_NORMAL).setMarginBottom(4);
        pC.add(new Text("Note du Contenu").setFont(fontBold).setUnderline());
        pC.add(new Text(" (En prenant en compte l\u2019appr\u00e9ciation de l\u2019entreprise)")
                .setFont(fontItalic));
        doc.add(pC);

        String cVal = (note != null) ? String.valueOf(note.getNoteContenu()) : "";
        doc.add(new Paragraph("      C = " + cVal)
                .setFont(fontNormal).setFontSize(FONT_NORMAL).setMarginBottom(SPACE_MEDIUM));

        // Note du Mémoire
        doc.add(new Paragraph("Note du M\u00e9moire")
                .setFont(fontBold).setFontSize(FONT_NORMAL).setUnderline().setMarginBottom(4));
        String mVal = (note != null) ? String.valueOf(note.getNoteMemoire()) : "";
        doc.add(new Paragraph("      M = " + mVal)
                .setFont(fontNormal).setFontSize(FONT_NORMAL).setMarginBottom(SPACE_MEDIUM));

        // Note de la Soutenance
        doc.add(new Paragraph("Note de la Soutenance")
                .setFont(fontBold).setFontSize(FONT_NORMAL).setUnderline().setMarginBottom(4));
        String sVal = (note != null) ? String.valueOf(note.getNoteSoutenance()) : "";
        doc.add(new Paragraph("      S = " + sVal)
                .setFont(fontNormal).setFontSize(FONT_NORMAL).setMarginBottom(SPACE_MEDIUM));
    }

    /** Boîte MOYENNE avec bordure et fond gris sur le titre */
    private static void addMoyenneBox(Document doc, PdfFont fontNormal, PdfFont fontBold,
                                       Note note) {

        String moyVal = (note != null)
                ? String.format("%.2f", note.getMoyenne())
                : "";

        // Ligne 1 : titre "MOYENNE" fond gris
        // Ligne 2 : formule
        float[] cols = {UnitValue.createPercentValue(100).getValue()};
        Table table = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(SPACE_MEDIUM);

        // Titre MOYENNE
        Cell titleCell = new Cell()
                .add(new Paragraph("MOYENNE")
                        .setFont(fontBold).setFontSize(FONT_NORMAL)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(GREY_LIGHT)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.8f))
                .setPadding(5);
        table.addCell(titleCell);

        // Formule
        String formula = "Moyenne  = C * 0,5 + M * 0,2 + S * 0,3 = " + moyVal;
        Cell formulaCell = new Cell()
                .add(new Paragraph(formula)
                        .setFont(fontBold).setFontSize(FONT_NORMAL))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.8f))
                .setPadding(6);
        table.addCell(formulaCell);

        doc.add(table);
    }

    /** Ligne de signatures */
    private static void addSignatures(Document doc, PdfFont fontNormal) {
        doc.add(new Paragraph("Signature des membres du jury :")
                .setFont(fontNormal).setFontSize(FONT_NORMAL).setMarginBottom(SPACE_MEDIUM));

        float[] cols = {160f, 160f, 160f};
        Table table = new Table(cols).setWidth(UnitValue.createPercentValue(100));
        table.setBorder(Border.NO_BORDER);

        for (int i = 0; i < 3; i++) {
            table.addCell(new Cell()
                    .add(new Paragraph("Pr.  " + dots(12))
                            .setFont(fontNormal).setFontSize(FONT_NORMAL))
                    .setBorder(Border.NO_BORDER));
        }
        doc.add(table);
    }

    // ─── utilitaires ────────────────────────────────────────────────────────

    private static void addSpace(Document doc, float size) {
        doc.add(new Paragraph("").setMarginBottom(size));
    }

    private static String dots(int n) {
        return "\u2026".repeat(Math.max(0, n / 3)) + ".".repeat(n % 3);
        // Alternative simple : return "." .repeat(n);
    }
}