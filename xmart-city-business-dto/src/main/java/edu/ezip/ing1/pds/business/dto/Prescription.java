package edu.ezip.ing1.pds.business.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Prescription {
    private Ordonnance ordonnance;
    private Medicament medicament;
    private String posologie;

    // Constructeur
    public Prescription(Ordonnance ordonnance, Medicament medicament, String posologie) {
        this.ordonnance = ordonnance;
        this.medicament = medicament;
        this.posologie = posologie;
    }
   
    
    public Prescription(int idOrdonnance, int idMedicament, String posologie) {
        this.ordonnance = new Ordonnance(idOrdonnance);  // Créer un objet Ordonnance
        this.medicament = new Medicament(idMedicament);  // Créer un objet Medicament
        this.posologie = posologie;
    }
    

    // Getters
    public Ordonnance getOrdonnance() { return ordonnance; }
    public Medicament getMedicament() { return medicament; }
    public String getPosologie() { return posologie; }

    // Méthode pour enregistrer la prescription dans la base de données
    public void save(Connection conn) throws SQLException {
        String query = "INSERT INTO Prescription (id_ordonnance, id_medicament, posologie) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ordonnance.getIdOrdonnance()); // id_ordonnance
            stmt.setInt(2, medicament.getId()); // id_medicament
            stmt.setString(3, posologie);       // posologie
            stmt.executeUpdate();
        }
    }
    private List<Prescription> prescriptions = new ArrayList<>();

    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }

    public List<Prescription> getPrescriptions() {
         return prescriptions;
    }

    public int getIdMedicament() {
        return medicament.getId();
    }
    
   

    @Override
    public String toString() {
        return "Prescription{" +
                "ordonnance=" + ordonnance +
                ", medicament=" + medicament +
                ", posologie='" + posologie + '\'' +
                '}';
    }
}
