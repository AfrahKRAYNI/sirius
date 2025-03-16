package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.LinkedHashSet;
import java.util.Set;

@JsonRootName("Patients")
public class Patients {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("patients")
    private Set<Patient> patients;

    // Constructeur pour garantir l'initialisation de la collection
    public Patients() {
        this.patients = new LinkedHashSet<>();
    }

    // Getter
    public Set<Patient> getPatients() {
        return patients;
    }

    // Setter
    public void setPatients(Set<Patient> patients) {
        if (patients != null) {
            this.patients = patients;
        } else {
            this.patients = new LinkedHashSet<>(); // Initialisation par défaut si null
        }
    }

    // Méthode pour ajouter un patient
    public Patients add(Patient patient) {
        if (patient != null) {
            patients.add(patient);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Patients{" +
                "patients=" + patients +
                '}';
    }
}
