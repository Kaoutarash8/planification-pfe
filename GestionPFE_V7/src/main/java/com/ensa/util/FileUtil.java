package com.ensa.util;

import jakarta.servlet.ServletContext;
import java.io.*;
import java.util.Calendar;
import java.util.Arrays;

public class FileUtil {

    private static final String UPLOAD_DIR = "uploads";
    private static String uploadPath;
    private static String anneeActuelle;
    private static ServletContext servletContext;

    public static void init(ServletContext context) {
        if (context == null) {
            throw new IllegalArgumentException("ServletContext ne peut pas etre null");
        }
        
        servletContext = context;
        uploadPath = context.getRealPath("/") + UPLOAD_DIR;
        uploadPath = uploadPath.replace('\\', '/');

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Dossier uploads cree: " + uploadPath + " - " + (created ? "OK" : "ECHEC"));
        } else {
            System.out.println("Dossier uploads existe: " + uploadPath);
        }

        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);
        int anneePrecedente = annee - 1;
        anneeActuelle = anneePrecedente + "_" + annee;

        System.out.println("=== INITIALISATION FILEUTIL ===");
        System.out.println("Upload path: " + uploadPath);
        System.out.println("Annee universitaire: " + anneeActuelle);
    }

    public static String getUploadPath() {
        if (uploadPath == null) {
            throw new IllegalStateException("FileUtil non initialise. Appeler FileUtil.init() dans la servlet.");
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

    public static String getAnneeUniversitaire() {
        return getAnneeActuelle();
    }

    public static String getCheminFichierAffectation() {
        return getUploadPath() + "/affectation_" + getAnneeUniversitaire() + ".xlsx";
    }

    public static String getCheminFichierPlanning() {
        return getUploadPath() + "/planning_" + getAnneeUniversitaire() + ".xlsx";
    }

    public static boolean affectationExiste() {
        File file = new File(getCheminFichierAffectation());
        return file.exists();
    }

    public static void supprimerAncienneAffectation() {
        String path = getCheminFichierAffectation();
        File file = new File(path);
        if (file.exists()) {
            boolean deleted = file.delete();
            System.out.println("Ancienne affectation supprimee: " + path + " - " + (deleted ? "OK" : "ECHEC"));
        }
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
        
        System.out.println("Fichier sauvegarde: " + destination.getAbsolutePath());
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