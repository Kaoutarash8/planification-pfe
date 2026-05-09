package com.ensa.util;

import jakarta.servlet.ServletContext;
import java.io.*;
import java.util.Calendar;
import java.util.Arrays;

public class FileUtil {

    private static final String UPLOAD_DIR = "uploads";
    private static String uploadPath;
    private static String anneeActuelle;

    /**
     * INITIALISATION AVEC SERVLET CONTEXT (OBLIGATOIRE)
     */
    public static void init(ServletContext context) {
        if (context == null) {
            throw new IllegalArgumentException("ServletContext ne peut pas être null");
        }
        
        uploadPath = context.getRealPath("/") + UPLOAD_DIR;
        uploadPath = uploadPath.replace('\\', '/');

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Dossier uploads créé: " + uploadPath + " - " + (created ? "OK" : "ECHEC"));
        } else {
            System.out.println("Dossier uploads existe: " + uploadPath);
        }

        // Déterminer l'année universitaire
        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);
        int anneePrecedente = annee - 1;
        anneeActuelle = anneePrecedente + "_" + annee;

        System.out.println("=== INITIALISATION FILEUTIL ===");
        System.out.println("UPLOAD PATH (SERVER): " + uploadPath);
        System.out.println("ANNEE UNIVERSITAIRE: " + anneeActuelle);
    }

    // METHODE setUploadPath POUR LA COMPATIBILITE AVEC AffectationServlet
    public static void setUploadPath(String path) {
        uploadPath = path;
        uploadPath = uploadPath.replace('\\', '/');
        
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Dossier uploads créé: " + uploadPath + " - " + (created ? "OK" : "ECHEC"));
        } else {
            System.out.println("Dossier uploads existe: " + uploadPath);
        }
        
        // Déterminer l'année universitaire
        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);
        int anneePrecedente = annee - 1;
        anneeActuelle = anneePrecedente + "_" + annee;
        
        System.out.println("=== FILEUTIL INITIALISE AVEC SETUPLOADPATH ===");
        System.out.println("UPLOAD PATH: " + uploadPath);
        System.out.println("ANNEE UNIVERSITAIRE: " + anneeActuelle);
    }

    public static String getUploadPath() {
        if (uploadPath == null) {
            throw new IllegalStateException("FileUtil n'a pas été initialisé. Appeler FileUtil.init() ou FileUtil.setUploadPath().");
        }
        return uploadPath;
    }

    public static String getAnneeActuelle() {
        if (anneeActuelle == null) {
            Calendar cal = Calendar.getInstance();
            int annee = cal.get(Calendar.YEAR);
            int anneePrecedente = annee - 1;
            anneeActuelle = anneePrecedente + "_" + annee;
        }
        return anneeActuelle;
    }

    public static String getWebPath(String fileName) {
        return UPLOAD_DIR + "/" + fileName;
    }

    public static String sauvegarderFichier(InputStream inputStream, String nomFichier) throws IOException {
        String path = getUploadPath();
        File destination = new File(path, nomFichier);
        
        try (FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        System.out.println("Fichier sauvegardé: " + destination.getAbsolutePath());
        return destination.getAbsolutePath();
    }

    public static String getDernierFichierAffectation() {
        String path = getUploadPath();
        File dossier = new File(path);
        
        if (!dossier.exists()) return null;
        
        File[] fichiers = dossier.listFiles((dir, name) -> name.startsWith("affectation_") && name.endsWith(".xlsx"));
        
        if (fichiers != null && fichiers.length > 0) {
            Arrays.sort(fichiers, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            return fichiers[0].getAbsolutePath();
        }
        return null;
    }
}