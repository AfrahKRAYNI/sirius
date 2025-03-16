package edu.ezip.ing1.pds.business.dto;

import java.util.ArrayList;
import java.util.List;

public class Ordonnances {
    private List<Ordonnance> ordonnances;

    public Ordonnances() {
        this.ordonnances = new ArrayList<>();
    }

    public List<Ordonnance> getOrdonnances() {
        return ordonnances;
    }
    public Ordonnances(List<Ordonnance> ordonnances) {
       
    } 

    public void addOrdonnance(Ordonnance ordonnance) {
        this.ordonnances.add(ordonnance);
    }

    public void removeOrdonnance(Ordonnance ordonnance) {
        this.ordonnances.remove(ordonnance);
    }

    public void clear() {
        this.ordonnances.clear();
    }
}
