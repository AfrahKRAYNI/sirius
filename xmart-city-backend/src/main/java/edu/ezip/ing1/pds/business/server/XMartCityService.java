package edu.ezip.ing1.pds.business.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Ordonnances;
import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.business.dto.Prescription;
import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;

public class XMartCityService {

    private final static String LoggingLabel = "B u s i n e s s - S e r v e r";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);

    private enum Queries {
        SELECT_ALL_PATIENTS("SELECT nom, prenom, age FROM patients "),
        INSERT_PATIENT("INSERT INTO patients (nom, prenom, age) VALUES (?, ?, ?)"),
        DELETE_PATIENT("DELETE FROM patients WHERE nom = ? AND prenom = ?"),
        SELECT_ALL_ORDONNANCES("SELECT * FROM Ordonnance"),
        INSERT_ORDONNANCE(
                "INSERT INTO ordonnance (description, id_patient, id_medecin, id_consultation) VALUES (?, ?, ?, ?)"),
        DELETE_ORDONNANCE("DELETE FROM ordonnance WHERE id_ordonnance = ?"),
        INSERT_PRESCRIPTION("INSERT INTO Prescription (id_ordonnance, id_medicament, posologie) VALUES (?, ?, ?)"),
        SELECT_ALL_MEDICAMENTS("SELECT id_medicament, nom_medicament FROM Medicaments"),
        SELECT_PRESCRIPTION_PAR_ORDONNANCE(
                "SELECT m.id_medicament, m.nom_medicament, p.posologie FROM Prescription p " +
                        "JOIN Medicaments m ON p.id_medicament = m.id_medicament " +
                        "WHERE p.id_ordonnance = ?"),
                        SELECT_ALL_SYMPTOMES("SELECT id, nom FROM symptomes"), 
    INSERT_SYMPTOME("INSERT INTO symptomes (nom) VALUES (?)"), 
    DELETE_SYMPTOME("DELETE FROM symptomes WHERE id = ?"), 
    UPDATE_SYMPTOME("UPDATE symptomes SET nom = ? WHERE id = ?"), 
    DIAGNOSTIC_PATIENT(
        "SELECT DISTINCT maladies.nom FROM maladies " +
        "JOIN symptomes_maladies ON maladies.id = symptomes_maladies.id_maladie " +
        "JOIN symptomes ON symptomes_maladies.id_symptome = symptomes.id " +
        "JOIN patients_symptomes ON symptomes.id = patients_symptomes.id_symptome " +
        "WHERE patients_symptomes.id_patient = ?"
    ); 
         
        private final String query;

        private Queries(final String query) {
            this.query = query;
        }
    }

    public static XMartCityService inst = null;

    public static final XMartCityService getInstance() {
        if (inst == null) {
            inst = new XMartCityService();
        }
        return inst;
    }

    private XMartCityService() {
    }

    public final Response dispatch(final Request request, final Connection connection)
            throws SQLException, IOException {
        Response response = null;
        final Queries queryEnum = Enum.valueOf(Queries.class, request.getRequestOrder());
        switch (queryEnum) {
            case SELECT_ALL_PATIENTS:
                response = SelectAllPatients(request, connection);
                break;

            case INSERT_PATIENT:
                response = InsertPatient(request, connection);
                break;

            case DELETE_PATIENT:
                response = DeletePatient(request, connection);
                break;
            case SELECT_ALL_ORDONNANCES:
                response = SelectAllOrdonnances(request, connection);
                break;
            case INSERT_ORDONNANCE:
                response = InsertOrdonnance(request, connection);
                break;
            case DELETE_ORDONNANCE:
                response = DeleteOrdonnance(request, connection);
                break;
            case SELECT_ALL_MEDICAMENTS:
                response = SelectAllMedicaments(request, connection);
                break;
            
                case SELECT_ALL_SYMPTOMES:
                response = SelectAllSymptomes(request, connection);
                break;
            case INSERT_SYMPTOME:
                response = InsertSymptome(request, connection);
                break;
            case DELETE_SYMPTOME:
                response = DeleteSymptome(request, connection);
                break;
            case UPDATE_SYMPTOME:
                response = UpdateSymptome(request, connection);
                break;
            case DIAGNOSTIC_PATIENT:
                response = DiagnostiquerPatient(request, connection);
                break;
    
            

            default:
                break;
        }
        return response;
    }
    

    private Response InsertPatient(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Patient requestData = objectMapper.readValue(request.getRequestBody(),
                Patient.class);

        String nom = requestData.getNom();
        String prenom = requestData.getPrenom();
        int age = requestData.getAge();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_PATIENT.query)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, prenom);
            pstmt.setInt(3, age);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "{\"message\": \"Patient ajouté avec succès\"}");
            } else {
                return new Response(request.getRequestId(), "{\"message\": \"Échec de l'ajout du patient\"}");
            }
        }
    }

    private Response SelectAllPatients(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try (Statement stmt = connection.createStatement();
                ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_PATIENTS.query)) {

            Patients patients = new Patients();
            while (res.next()) {
                Patient patient = new Patient();
                patient.setNom(res.getString(1));
                patient.setPrenom(res.getString(2));
                patient.setAge(res.getInt(3));
                patients.add(patient);
            }

            if (patients.getPatients().isEmpty()) {
                return new Response(request.getRequestId(), "Aucun patient trouvé");
            }

            return new Response(request.getRequestId(), objectMapper.writeValueAsString(patients));
        }
    }

    
    private Response DeletePatient(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Patient requestData = objectMapper.readValue(request.getRequestBody(), Patient.class);

        String nom = requestData.getNom();
        String prenom = requestData.getPrenom();

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_PATIENT.query)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, prenom);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "{\"message\": \"Patient supprimé avec succès\"}");
            } else {
                return new Response(request.getRequestId(), "{\"message\": \"Aucun patient trouvé à supprimer\"}");
            }
        }
    }

    private Response SelectAllOrdonnances(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        try (Statement stmt = connection.createStatement();
                ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_ORDONNANCES.query)) {

            Ordonnances ordonnances = new Ordonnances();

            while (res.next()) {
                Ordonnance ordonnance = new Ordonnance();
                ordonnance.setIdOrdonnance(res.getInt("id_ordonnance"));
                ordonnance.setDescription(res.getString("description"));
                ordonnance.setIdPatient(res.getInt("id_patient"));
                ordonnance.setIdMedecin(res.getInt("id_medecin"));
                ordonnance.setIdConsultation(res.getInt("id_consultation"));

                try (PreparedStatement pstmt = connection
                        .prepareStatement(Queries.SELECT_PRESCRIPTION_PAR_ORDONNANCE.query)) {
                    pstmt.setInt(1, ordonnance.getIdOrdonnance());
                    ResultSet resPrescriptions = pstmt.executeQuery();
                    while (resPrescriptions.next()) {
                        Prescription prescription = new Prescription(
                                resPrescriptions.getInt("id_ordonnance"),
                                resPrescriptions.getInt("id_medicament"),
                                resPrescriptions.getString("posologie"));

                        ordonnance.addPrescription(prescription);
                    }
                }

                ordonnances.addOrdonnance(ordonnance);
            }

            return new Response(request.getRequestId(),
                    ordonnances.getOrdonnances().isEmpty() ? "Aucune ordonnance trouvée"
                            : objectMapper.writeValueAsString(ordonnances));
        }
    }

    private Response DeleteOrdonnance(final Request request, final Connection connection)
            throws SQLException, JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Ordonnance ordonnance = null;
        try {
            ordonnance = objectMapper.readValue(request.getRequestBody(), Ordonnance.class);
        } catch (IOException e) {

            return new Response(request.getRequestId(), "Erreur lors de la lecture de l'ordonnance");
        }

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_ORDONNANCE.query)) {
            pstmt.setInt(1, ordonnance.getIdOrdonnance());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return new Response(request.getRequestId(), "Ordonnance supprimée avec succès");
            } else {
                return new Response(request.getRequestId(), "Aucune ordonnance trouvée pour suppression");
            }
        }
    }

    private Response InsertOrdonnance(final Request request, final Connection connection)
            throws SQLException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Ordonnance ordonnance = objectMapper.readValue(request.getRequestBody(), Ordonnance.class);

        try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_ORDONNANCE.query,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ordonnance.getDescription());
            pstmt.setInt(2, ordonnance.getIdPatient());
            pstmt.setInt(3, ordonnance.getIdMedecin());
            pstmt.setInt(4, ordonnance.getIdConsultation());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int idOrdonnance = rs.getInt(1);
                for (Prescription prescription : ordonnance.getPrescriptions()) {
                    try (PreparedStatement pstmtPres = connection.prepareStatement(Queries.INSERT_PRESCRIPTION.query)) {
                        pstmtPres.setInt(1, idOrdonnance);
                        pstmtPres.setInt(2, prescription.getIdMedicament());
                        pstmtPres.setString(3, prescription.getPosologie());
                        pstmtPres.executeUpdate();
                    }
                }
            }

            return new Response(request.getRequestId(), "Ordonnance et prescriptions ajoutées avec succès");
        }

    }


    private Response SelectAllMedicaments(final Request request, final Connection connection)
        throws SQLException, JsonProcessingException {
    final ObjectMapper objectMapper = new ObjectMapper();
    try (Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_MEDICAMENTS.query)) {

        List<Medicament> medicaments = new ArrayList<>();
        while (res.next()) {
            Medicament medicament = new Medicament();
            medicament.setIdMedicament(res.getInt("id_medicament"));
            medicament.setNomMedicament(res.getString("nom_medicament"));
            medicaments.add(medicament);
        }
        return new Response(request.getRequestId(),
                medicaments.isEmpty() ? "Aucun médicament trouvé"
                        : objectMapper.writeValueAsString(medicaments));
    }
}


private Response SelectAllSymptomes(final Request request, final Connection connection)
        throws SQLException, JsonProcessingException {
    final ObjectMapper objectMapper = new ObjectMapper();
    try (Statement stmt = connection.createStatement();
         ResultSet res = stmt.executeQuery(Queries.SELECT_ALL_SYMPTOMES.query)) {

        List<Symptomes> symptomes = new ArrayList<>();
        while (res.next()) {
            Symptomes symptome = new Symptomes();
            symptome.setId(res.getInt("id"));
            symptome.setNom(res.getString("nom"));
            symptomes.add(symptome);
        }

        System.out.println("Symptômes récupérés depuis la BD : " + symptomes); 

        return new Response(request.getRequestId(),
                symptomes.isEmpty() ? "Aucun symptôme trouvé"
                        : objectMapper.writeValueAsString(symptomes));
    }
}


private Response InsertSymptome(final Request request, final Connection connection)
        throws SQLException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);

    try (PreparedStatement pstmt = connection.prepareStatement(Queries.INSERT_SYMPTOME.query)) {
        pstmt.setString(1, symptome.getNom());
        int rowsAffected = pstmt.executeUpdate();

        return rowsAffected > 0
                ? new Response(request.getRequestId(), "{\"message\": \"Symptôme ajouté avec succès\"}")
                : new Response(request.getRequestId(), "{\"message\": \"Échec de l'ajout du symptôme\"}");
    }
}

private Response DeleteSymptome(final Request request, final Connection connection)
        throws SQLException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    Symptomes symptome = objectMapper.readValue(request.getRequestBody(), Symptomes.class);

    try (PreparedStatement pstmt = connection.prepareStatement(Queries.DELETE_SYMPTOME.query)) {
        pstmt.setInt(1, symptome.getId());
        int rowsAffected = pstmt.executeUpdate();

        return rowsAffected > 0
                ? new Response(request.getRequestId(), "{\"message\": \"Symptôme supprimé avec succès\"}")
                : new Response(request.getRequestId(), "{\"message\": \"Aucun symptôme trouvé à supprimer\"}");
    }
}

private Response UpdateSymptome(final Request request, final Connection connection)
        throws SQLException, IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> updateData = objectMapper.readValue(request.getRequestBody(), Map.class);

    try (PreparedStatement pstmt = connection.prepareStatement(Queries.UPDATE_SYMPTOME.query)) {
        pstmt.setString(1, updateData.get("nouveauNom"));
        pstmt.setInt(2, Integer.parseInt(updateData.get("id")));
        int rowsAffected = pstmt.executeUpdate();

        return rowsAffected > 0
                ? new Response(request.getRequestId(), "{\"message\": \"Symptôme mis à jour avec succès\"}")
                : new Response(request.getRequestId(), "{\"message\": \"Aucun symptôme trouvé pour mise à jour\"}");
    }
}

private Response DiagnostiquerPatient(final Request request, final Connection connection)
        throws SQLException, JsonProcessingException, IOException { // ← Ajoute IOException ici
    final ObjectMapper objectMapper = new ObjectMapper();
    int idPatient = objectMapper.readValue(request.getRequestBody(), Integer.class); // ← Plus d'erreur

    try (PreparedStatement pstmt = connection.prepareStatement(Queries.DIAGNOSTIC_PATIENT.query)) {
        pstmt.setInt(1, idPatient);
        ResultSet res = pstmt.executeQuery();

        List<String> maladies = new ArrayList<>();
        while (res.next()) {
            maladies.add(res.getString("nom"));
        }

        return new Response(request.getRequestId(),
                maladies.isEmpty() ? "Aucune maladie trouvée" : objectMapper.writeValueAsString(maladies));
    }
}



}
