package com.ensa.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUploadUtil {
    
    private static final String UPLOAD_DIR = "uploads";
    private static final String DATA_DIR = UPLOAD_DIR + "/data";
    private static final String AFFECTATION_DIR = UPLOAD_DIR + "/Affectation";
    private static String basePath = null;
    
    // CHEMIN FIXE VERS TON PROJET
    private static final String PROJECT_PATH = "C:/Users/e/workspace-pfe/pfe-affectation";
    
    public static void setBasePath(String path) {
        basePath = path;
    }
    
    public static void creerRepertoireUpload() {
        // Utiliser le chemin FIXE du projet
        basePath = PROJECT_PATH;
        
        File uploadDir = new File(basePath, UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("Dossier uploads créé: " + uploadDir.getAbsolutePath());
        }
        
        File dataDir = new File(basePath, DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            System.out.println("Dossier data créé: " + dataDir.getAbsolutePath());
        }
        
        File pvsDir = new File(basePath, UPLOAD_DIR + "/pvs");
        if (!pvsDir.exists()) {
            pvsDir.mkdirs();
            System.out.println("Dossier pvs créé: " + pvsDir.getAbsolutePath());
        }
        
        File affectationDir = new File(basePath, AFFECTATION_DIR);
        if (!affectationDir.exists()) {
            affectationDir.mkdirs();
            System.out.println("Dossier Affectation créé: " + affectationDir.getAbsolutePath());
        }
        
        System.out.println("=== DOSSIERS DANS LE PROJET ===");
        System.out.println("Base path: " + basePath);
        System.out.println("Uploads: " + uploadDir.getAbsolutePath());
        System.out.println("Data: " + dataDir.getAbsolutePath());
        System.out.println("Affectation: " + affectationDir.getAbsolutePath());
    }
    
    public static String getBasePath() {
        // Retourner le chemin FIXE du projet
        return PROJECT_PATH;
    }
    
    public static String getUploadPath() {
        return getBasePath() + File.separator + UPLOAD_DIR;
    }
    
    public static String getDataPath() {
        return getBasePath() + File.separator + DATA_DIR;
    }
    
    public static String getPVsPath() {
        return getBasePath() + File.separator + UPLOAD_DIR + File.separator + "pvs";
    }
    
    public static String getAffectationPath() {
        return getBasePath() + File.separator + AFFECTATION_DIR;
    }
    
    public static String sauvegarderFichierPermanent(InputStream inputStream, String nomFichier) throws Exception {
        String dataPath = getDataPath();
        File dataDir = new File(dataPath);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        File destination = new File(dataDir, nomFichier);
        Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Fichier sauvegardé: " + destination.getAbsolutePath());
        return destination.getAbsolutePath();
    }
    
    public static String getDernierFichier(String prefix) {
        String dataPath = getDataPath();
        File dataDir = new File(dataPath);
        if (!dataDir.exists()) {
            System.out.println("Dossier data n'existe pas: " + dataPath);
            return null;
        }
        File[] fichiers = dataDir.listFiles((dir, name) -> name.startsWith(prefix));
        
        if (fichiers != null && fichiers.length > 0) {
            File dernier = fichiers[0];
            for (File f : fichiers) {
                if (f.lastModified() > dernier.lastModified()) {
                    dernier = f;
                }
            }
            System.out.println("Dernier fichier trouvé pour " + prefix + ": " + dernier.getAbsolutePath());
            return dernier.getAbsolutePath();
        }
        System.out.println("Aucun fichier trouvé pour le préfixe: " + prefix);
        return null;
    }
    
    public static void nettoyerFichiersTemporaires(String... fichiers) {
        for (String fichier : fichiers) {
            if (fichier != null) {
                File file = new File(fichier);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }
        }
    }
}