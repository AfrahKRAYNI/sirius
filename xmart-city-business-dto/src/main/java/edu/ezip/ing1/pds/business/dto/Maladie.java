package edu.ezip.ing1.pds.business.dto;

import java.util.List;

public class Maladie {
    private int id;
    private String nom;
    private List<Symptomes> symptomes;

    public Maladie(int id, String nom, List<Symptomes> symptomes) {
        this.id = id;
        this.nom = nom;
        this.symptomes = symptomes;
    }

    public int getId() { return id; }
    public void setId(int id) { 
        this.id = id; 
    }
    public String getNom() { return nom; }
    public List<Symptomes> getSymptomes() { return symptomes; }

    @Override
    public String toString() {
        return nom;
    }
}