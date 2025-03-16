package edu.ezip.ing1.pds.business.dto;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ordonnance {
    private int idOrdonnance;
    private String description;
    private int idPatient;
    private int idMedecin;
    private int idConsultation;
    private String posologie;  

    public Ordonnance() {
    }


    public Ordonnance(int idOrdonnance, String description, int idPatient, int idMedecin, int idConsultation, String posologie) {
        this.idOrdonnance = idOrdonnance;
        this.description = description;
        this.idPatient = idPatient;
        this.idMedecin = idMedecin;
        this.idConsultation = idConsultation;
        this.posologie = posologie;
    }

    public Ordonnance(int idOrdonnance) {
        this.idOrdonnance = idOrdonnance;
    }
    


    public final Ordonnance build(final ResultSet resultSet)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        setFieldsFromResultSet(resultSet, "idOrdonnance", "description", "idPatient", "idMedecin", "idConsultation", "posologie");
        return this;
    }

    public final PreparedStatement build(PreparedStatement preparedStatement)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        return buildPreparedStatement(preparedStatement, 
                String.valueOf(idOrdonnance), description, String.valueOf(idPatient), 
                String.valueOf(idMedecin), String.valueOf(idConsultation), posologie);
    }

    public int getIdOrdonnance() {
        return idOrdonnance;
    }

    public String getDescription() {
        return description;
    }

    public int getIdPatient() {
        return idPatient;
    }

    public int getIdMedecin() {
        return idMedecin;
    }

    public int getIdConsultation() {
        return idConsultation;
    }

    public String getPosologie() {
        return posologie;
    }

    public void setIdOrdonnance(int idOrdonnance) {
        this.idOrdonnance = idOrdonnance;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }

    public void setIdMedecin(int idMedecin) {
        this.idMedecin = idMedecin;
    }

    public void setIdConsultation(int idConsultation) {
        this.idConsultation = idConsultation;
    }
    

    public void setPosologie(String posologie) {
        this.posologie = posologie;
    }
    public static int generateIdOrdonnance() {
        
        return new Random().nextInt(1000); 
    }
    private List<Medicament> medicaments = new ArrayList<>();

    public void addMedicament(Medicament medicament) {
        this.medicaments.add(medicament);
    }
   

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    private void setFieldsFromResultSet(final ResultSet resultSet, final String... fieldNames)
            throws NoSuchFieldException, SQLException, IllegalAccessException {
        for (final String fieldName : fieldNames) {
            final Field field = this.getClass().getDeclaredField(fieldName);
            if (field.getType() == int.class) {
                field.set(this, resultSet.getInt(fieldName));
            } else {
                field.set(this, resultSet.getObject(fieldName));
            }
        }
    }

    private final PreparedStatement buildPreparedStatement(PreparedStatement preparedStatement, final String... fieldValues)
            throws SQLException {
        int ix = 0;
        for (final String fieldValue : fieldValues) {
            preparedStatement.setString(++ix, fieldValue);
        }
        return preparedStatement;
    }

    private List<Prescription> prescriptions = new ArrayList<>();

    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }

    


    @Override
    public String toString() {
        return "Ordonnance{" +
                "idOrdonnance=" + idOrdonnance +
                ", description='" + description + '\'' +
                ", idPatient=" + idPatient +
                ", idMedecin=" + idMedecin +
                ", idConsultation=" + idConsultation +
                ", posologie='" + posologie + '\'' +
                '}';
    }
}
