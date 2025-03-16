package edu.ezip.ing1.pds.frontend;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import edu.ezip.ing1.pds.business.dto.Ordonnance;

public class OrdonnanceTableModel extends AbstractTableModel {
    private final String[] columnNames = { "ID", "ID Patient", "ID Consultation", "Description", "ID Médecin" };
    private final List<Ordonnance> ordonnances;

    public OrdonnanceTableModel(List<Ordonnance> ordonnances) {
        this.ordonnances = ordonnances;
    }

    @Override
    public int getRowCount() {
        return ordonnances.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Ordonnance ordonnance = ordonnances.get(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0: value = ordonnance.getIdOrdonnance(); break;
            case 1: value = ordonnance.getIdPatient(); break;
            case 2: value = ordonnance.getIdConsultation(); break;
            case 3: value = ordonnance.getDescription(); break;
            case 4: value = ordonnance.getIdMedecin(); break;
        }

        
        System.out.println("Value at (" + rowIndex + ", " + columnIndex + "): " + value);
        return value;
    }
}
