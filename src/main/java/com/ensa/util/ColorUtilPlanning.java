package com.ensa.util;

import org.apache.poi.ss.usermodel.IndexedColors;
import java.util.*;

public class ColorUtilPlanning {
    
    // 35 COULEURS PASTEL - CALMES ET PROFESSIONNELLES
    private static final short[] COULEURS_EXCEL = {
        IndexedColors.LIGHT_YELLOW.getIndex(),          // Jaune clair pastel
        IndexedColors.LIGHT_GREEN.getIndex(),           // Vert clair pastel
        IndexedColors.LIGHT_BLUE.getIndex(),            // Bleu clair pastel
        IndexedColors.LIGHT_ORANGE.getIndex(),          // Orange clair pastel
        IndexedColors.LIGHT_TURQUOISE.getIndex(),       // Turquoise clair pastel
        IndexedColors.PALE_BLUE.getIndex(),             // Bleu très clair
        IndexedColors.ROSE.getIndex(),                  // Rose pastel
        IndexedColors.LAVENDER.getIndex(),              // Lavande pastel
        IndexedColors.TAN.getIndex(),                   // Beige pastel
        IndexedColors.SEA_GREEN.getIndex(),             // Vert mer pastel
        IndexedColors.SKY_BLUE.getIndex(),              // Bleu ciel pastel
        IndexedColors.CORAL.getIndex(),                 // Corail doux
        IndexedColors.PLUM.getIndex(),                  // Prune douce
        IndexedColors.ORCHID.getIndex(),                // Orchidée pastel
        IndexedColors.GOLD.getIndex(),                  // Or doux
        IndexedColors.LIME.getIndex(),                  // Vert lime pastel
        IndexedColors.TEAL.getIndex(),                  // Teal pastel
        IndexedColors.AQUA.getIndex(),                  // Aqua pastel
        IndexedColors.LEMON_CHIFFON.getIndex(),         // Jaune citron très clair
        IndexedColors.OLIVE_GREEN.getIndex(),           // Olive doux
        IndexedColors.CORNFLOWER_BLUE.getIndex(),       // Bleuet pastel
        IndexedColors.INDIGO.getIndex(),                // Indigo doux
        IndexedColors.VIOLET.getIndex(),                // Violet pastel
        IndexedColors.TURQUOISE.getIndex(),             // Turquoise pastel
        IndexedColors.GREEN.getIndex(),                 // Vert doux
        IndexedColors.BLUE.getIndex(),                  // Bleu doux
        IndexedColors.YELLOW.getIndex(),                // Jaune doux
        IndexedColors.ORANGE.getIndex(),                // Orange doux
        IndexedColors.PINK.getIndex(),                  // Rose doux
        IndexedColors.ROYAL_BLUE.getIndex(),            // Bleu royal doux
        IndexedColors.MAROON.getIndex(),                // Marron doux
        IndexedColors.DARK_TEAL.getIndex(),             // Teal foncé doux
        IndexedColors.DARK_GREEN.getIndex(),            // Vert foncé doux
        IndexedColors.DARK_BLUE.getIndex(),             // Bleu foncé doux
        IndexedColors.DARK_RED.getIndex()               // Rouge doux (pas agressif)
    };
    
    // 35 couleurs pastel pour PDF (RGB - tons calmes)
    private static final float[][] COULEURS_PDF = {
        {1.0f, 1.0f, 0.8f},    // Jaune clair pastel
        {0.8f, 1.0f, 0.8f},    // Vert clair pastel
        {0.8f, 0.9f, 1.0f},    // Bleu clair pastel
        {1.0f, 0.9f, 0.8f},    // Orange clair pastel
        {0.8f, 1.0f, 1.0f},    // Turquoise clair pastel
        {0.9f, 0.9f, 1.0f},    // Bleu très clair
        {1.0f, 0.8f, 0.9f},    // Rose pastel
        {0.9f, 0.8f, 1.0f},    // Lavande pastel
        {0.95f, 0.9f, 0.85f},  // Beige pastel
        {0.7f, 0.9f, 0.8f},    // Vert mer pastel
        {0.7f, 0.8f, 1.0f},    // Bleu ciel pastel
        {1.0f, 0.8f, 0.7f},    // Corail doux
        {0.8f, 0.7f, 0.9f},    // Prune douce
        {0.9f, 0.7f, 0.9f},    // Orchidée pastel
        {1.0f, 0.9f, 0.6f},    // Or doux
        {0.8f, 1.0f, 0.7f},    // Vert lime pastel
        {0.6f, 0.8f, 0.8f},    // Teal pastel
        {0.7f, 0.9f, 0.9f},    // Aqua pastel
        {1.0f, 1.0f, 0.7f},    // Jaune citron très clair
        {0.7f, 0.8f, 0.6f},    // Olive doux
        {0.7f, 0.7f, 1.0f},    // Bleuet pastel
        {0.6f, 0.5f, 0.8f},    // Indigo doux
        {0.8f, 0.6f, 0.9f},    // Violet pastel
        {0.6f, 0.9f, 0.9f},    // Turquoise pastel
        {0.6f, 0.9f, 0.6f},    // Vert doux
        {0.6f, 0.7f, 1.0f},    // Bleu doux
        {1.0f, 1.0f, 0.6f},    // Jaune doux
        {1.0f, 0.8f, 0.6f},    // Orange doux
        {1.0f, 0.7f, 0.8f},    // Rose doux
        {0.5f, 0.6f, 0.9f},    // Bleu royal doux
        {0.7f, 0.5f, 0.6f},    // Marron doux
        {0.5f, 0.7f, 0.7f},    // Teal foncé doux
        {0.5f, 0.7f, 0.5f},    // Vert foncé doux
        {0.4f, 0.5f, 0.8f},    // Bleu foncé doux
        {0.8f, 0.5f, 0.5f}     // Rouge doux (pas agressif)
    };
    
    private static final String[] NOMS_COULEURS = {
        "JAUNE CLAIR", "VERT CLAIR", "BLEU CLAIR", "ORANGE CLAIR", "TURQUOISE CLAIR",
        "BLEU TRES CLAIR", "ROSE PASTEL", "LAVANDE", "BEIGE", "VERT MER",
        "BLEU CIEL", "CORAL DOUX", "PRUNE DOUCE", "ORCHIDEE", "OR DOUX",
        "VERT LIME", "TEAL PASTEL", "AQUA", "JAUNE CITRON", "OLIVE DOUX",
        "BLEUET", "INDIGO DOUX", "VIOLET PASTEL", "TURQUOISE", "VERT DOUX",
        "BLEU DOUX", "JAUNE DOUX", "ORANGE DOUX", "ROSE DOUX", "BLEU ROYAL",
        "MARRON DOUX", "TEAL FONCE", "VERT FONCE", "BLEU FONCE", "ROUGE DOUX"
    };
    
    // Maps pour stocker les couleurs par element
    private static Map<String, Short> mapCouleursProfesseurs = new LinkedHashMap<>();
    private static Map<String, Short> mapCouleursHeures = new LinkedHashMap<>();
    private static Map<String, Short> mapCouleursSalles = new LinkedHashMap<>();
    private static Map<String, Short> mapCouleursDates = new LinkedHashMap<>();
    private static Map<String, Short> mapCouleursFilieres = new LinkedHashMap<>();
    
    private static int indexProf = 0;
    private static int indexHeure = 0;
    private static int indexSalle = 0;
    private static int indexDate = 0;
    private static int indexFiliere = 0;
    
    private static boolean initialise = false;
    
    public static void initialiserCouleurs(
            Set<String> professeurs,
            Set<String> heures,
            Set<String> salles,
            Set<String> dates,
            Set<String> filieres) {
        
        if (!initialise) {
            mapCouleursProfesseurs.clear();
            mapCouleursHeures.clear();
            mapCouleursSalles.clear();
            mapCouleursDates.clear();
            mapCouleursFilieres.clear();
            
            indexProf = 0;
            indexHeure = 0;
            indexSalle = 0;
            indexDate = 0;
            indexFiliere = 0;
            
            for (String prof : professeurs) {
                mapCouleursProfesseurs.put(prof, COULEURS_EXCEL[indexProf % COULEURS_EXCEL.length]);
                indexProf++;
            }
            
            for (String heure : heures) {
                mapCouleursHeures.put(heure, COULEURS_EXCEL[indexHeure % COULEURS_EXCEL.length]);
                indexHeure++;
            }
            
            for (String salle : salles) {
                mapCouleursSalles.put(salle, COULEURS_EXCEL[indexSalle % COULEURS_EXCEL.length]);
                indexSalle++;
            }
            
            for (String date : dates) {
                mapCouleursDates.put(date, COULEURS_EXCEL[indexDate % COULEURS_EXCEL.length]);
                indexDate++;
            }
            
            for (String filiere : filieres) {
                mapCouleursFilieres.put(filiere, COULEURS_EXCEL[indexFiliere % COULEURS_EXCEL.length]);
                indexFiliere++;
            }
            
            initialise = true;
            
            System.out.println("=== INITIALISATION COULEURS PLANNING ===");
            System.out.println("  Professeurs: " + mapCouleursProfesseurs.size() + " couleurs");
            System.out.println("  Heures: " + mapCouleursHeures.size() + " couleurs");
            System.out.println("  Salles: " + mapCouleursSalles.size() + " couleurs");
            System.out.println("  Dates: " + mapCouleursDates.size() + " couleurs");
            System.out.println("  Filieres: " + mapCouleursFilieres.size() + " couleurs");
        }
    }
    
    public static short getCouleurProfesseur(String professeur) {
        return mapCouleursProfesseurs.getOrDefault(professeur, COULEURS_EXCEL[0]);
    }
    
    public static short getCouleurHeure(String heure) {
        return mapCouleursHeures.getOrDefault(heure, COULEURS_EXCEL[1]);
    }
    
    public static short getCouleurSalle(String salle) {
        return mapCouleursSalles.getOrDefault(salle, COULEURS_EXCEL[2]);
    }
    
    public static short getCouleurDate(String date) {
        return mapCouleursDates.getOrDefault(date, COULEURS_EXCEL[3]);
    }
    
    public static short getCouleurFiliere(String filiere) {
        return mapCouleursFilieres.getOrDefault(filiere, COULEURS_EXCEL[4]);
    }
    
    public static float[] getCouleurProfesseurPdf(String professeur) {
        return COULEURS_PDF[getIndexFromColor(getCouleurProfesseur(professeur))];
    }
    
    public static float[] getCouleurHeurePdf(String heure) {
        return COULEURS_PDF[getIndexFromColor(getCouleurHeure(heure))];
    }
    
    public static float[] getCouleurSallePdf(String salle) {
        return COULEURS_PDF[getIndexFromColor(getCouleurSalle(salle))];
    }
    
    public static float[] getCouleurDatePdf(String date) {
        return COULEURS_PDF[getIndexFromColor(getCouleurDate(date))];
    }
    
    public static float[] getCouleurFilierePdf(String filiere) {
        return COULEURS_PDF[getIndexFromColor(getCouleurFiliere(filiere))];
    }
    
    private static int getIndexFromColor(short color) {
        for (int i = 0; i < COULEURS_EXCEL.length; i++) {
            if (COULEURS_EXCEL[i] == color) {
                return i % COULEURS_PDF.length;
            }
        }
        return 0;
    }
    
    public static Map<String, Short> getCouleursProfesseurs() {
        return new LinkedHashMap<>(mapCouleursProfesseurs);
    }
    
    public static void reinitialiser() {
        initialise = false;
        mapCouleursProfesseurs.clear();
        mapCouleursHeures.clear();
        mapCouleursSalles.clear();
        mapCouleursDates.clear();
        mapCouleursFilieres.clear();
    }
}