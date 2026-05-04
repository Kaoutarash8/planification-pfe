package com.ensa.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUploadUtil {
    
    private static final String UPLOAD_DIR = "uploads";
    private static final String DATA_DIR = UPLOAD_DIR + "/data";
    private static String basePath = null;
    
    // Chemin FIXE vers votre projet
    private static final String PROJECT_PATH = "C:/Users/e/workspace-pfe/pfe-affectation";
    
    public static void creerRepertoireUpload() {
        if (basePath == null) {
            basePath = PROJECT_PATH;
        }
        
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
        
        System.out.println("=== DOSSIERS DANS LE PROJET ===");
        System.out.println("Base path: " + basePath);
        System.out.println("Uploads: " + uploadDir.getAbsolutePath());
        System.out.println("Data: " + dataDir.getAbsolutePath());
    }
    
    public static String getBasePath() {
        if (basePath != null) return basePath;
        basePath = PROJECT_PATH;
        return basePath;
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
    
    public static String sauvegarderFichierPermanent(InputStream inputStream, String nomFichier) throws Exception {
        String dataPath = getDataPath();
        File destination = new File(dataPath, nomFichier);
        Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Fichier sauvegardé: " + destination.getAbsolutePath());
        return destination.getAbsolutePath();
    }
    
    public static String getDernierFichier(String prefix) {
        String dataPath = getDataPath();
        File dataDir = new File(dataPath);
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