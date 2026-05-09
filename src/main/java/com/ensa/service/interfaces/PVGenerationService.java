package com.ensa.service.interfaces;

import com.ensa.model.Professeur;
import com.ensa.model.Etudiant;
import java.util.List;
import java.util.Map;

public interface PVGenerationService {
    
    /**
     * Récupère la liste de tous les encadrants avec leurs étudiants à partir du planning
     */
    Map<Professeur, List<Etudiant>> getEncadrantsWithEtudiants(String planningPath) throws Exception;
    
    /**
     * Génère un PDF de PV pour un encadrant
     */
    byte[] genererPV(Professeur encadrant, List<Etudiant> etudiants, List<Note> notes,
                     String president, String rapporteur1, String rapporteur2, 
                     String dateSoutenance) throws Exception;
    
    class Note {
        private String nomComplet;
        private double noteContenu;
        private double noteMemoire;
        private double noteSoutenance;
        
        // Getters et Setters
        public String getNomComplet() { return nomComplet; }
        public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
        public double getNoteContenu() { return noteContenu; }
        public void setNoteContenu(double noteContenu) { this.noteContenu = noteContenu; }
        public double getNoteMemoire() { return noteMemoire; }
        public void setNoteMemoire(double noteMemoire) { this.noteMemoire = noteMemoire; }
        public double getNoteSoutenance() { return noteSoutenance; }
        public void setNoteSoutenance(double noteSoutenance) { this.noteSoutenance = noteSoutenance; }
        
        public double getMoyenne() {
            return (noteContenu * 0.5) + (noteMemoire * 0.2) + (noteSoutenance * 0.3);
        }
    }
}