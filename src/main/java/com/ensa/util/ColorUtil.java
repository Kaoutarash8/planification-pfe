package com.ensa.util;

import org.apache.poi.ss.usermodel.IndexedColors;
import java.util.*;

public class ColorUtil {
    
    private static final short[] COULEURS_EXCEL = {
        IndexedColors.ROSE.getIndex(),
        IndexedColors.LIGHT_YELLOW.getIndex(),
        IndexedColors.LIGHT_GREEN.getIndex(),
        IndexedColors.LIGHT_BLUE.getIndex(),
        IndexedColors.LIGHT_ORANGE.getIndex(),
        IndexedColors.VIOLET.getIndex(),
        IndexedColors.TURQUOISE.getIndex()
    };
    
    private static final float[][] COULEURS_PDF = {
        {1.0f, 0.75f, 0.80f},
        {1.0f, 1.0f, 0.75f},
        {0.75f, 0.90f, 0.75f},
        {0.75f, 0.80f, 1.0f},
        {1.0f, 0.80f, 0.60f},
        {0.85f, 0.70f, 1.0f},
        {0.70f, 0.90f, 0.90f}
    };
    
    private static final String[] NOMS_COULEURS = {
        "ROSE", "JAUNE", "VERT", "BLEU", "ORANGE", "VIOLET", "TURQUOISE"
    };
    
    private static Map<String, Short> mapCouleursExcel = new LinkedHashMap<>();
    private static Map<String, float[]> mapCouleursPdf = new LinkedHashMap<>();
    private static Map<String, String> mapNomsCouleurs = new LinkedHashMap<>();
    private static boolean initialise = false;
    
    public static void initialiserCouleurs(Set<String> filieres) {
        if (!initialise && filieres != null && !filieres.isEmpty()) {
            mapCouleursExcel.clear();
            mapCouleursPdf.clear();
            mapNomsCouleurs.clear();
            
            int index = 0;
            for (String filiere : filieres) {
                mapCouleursExcel.put(filiere, COULEURS_EXCEL[index % COULEURS_EXCEL.length]);
                mapCouleursPdf.put(filiere, COULEURS_PDF[index % COULEURS_PDF.length]);
                mapNomsCouleurs.put(filiere, NOMS_COULEURS[index % NOMS_COULEURS.length]);
                index++;
            }
            
            initialise = true;
        }
    }
    
    public static short getCouleurExcel(String filiere) {
        if (filiere == null || filiere.isEmpty()) {
            return COULEURS_EXCEL[0];
        }
        return mapCouleursExcel.getOrDefault(filiere, COULEURS_EXCEL[0]);
    }
    
    public static float[] getCouleurPdf(String filiere) {
        if (filiere == null || filiere.isEmpty()) {
            return COULEURS_PDF[0];
        }
        return mapCouleursPdf.getOrDefault(filiere, COULEURS_PDF[0]);
    }
    
    public static String getNomCouleur(String filiere) {
        if (filiere == null || filiere.isEmpty()) {
            return NOMS_COULEURS[0];
        }
        return mapNomsCouleurs.getOrDefault(filiere, NOMS_COULEURS[0]);
    }
    
    public static Map<String, Short> getLegendeCouleurs() {
        return new LinkedHashMap<>(mapCouleursExcel);
    }
    
    public static Map<String, float[]> getLegendeCouleursPdf() {
        return new LinkedHashMap<>(mapCouleursPdf);
    }
    
    public static Map<String, String> getLegendeNomsCouleurs() {
        return new LinkedHashMap<>(mapNomsCouleurs);
    }
    
    public static void reinitialiser() {
        initialise = false;
        mapCouleursExcel.clear();
        mapCouleursPdf.clear();
        mapNomsCouleurs.clear();
    }
}