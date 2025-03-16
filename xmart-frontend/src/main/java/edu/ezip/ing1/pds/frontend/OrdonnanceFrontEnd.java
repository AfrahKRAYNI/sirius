package edu.ezip.ing1.pds.frontend;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Medicament;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Ordonnances;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.MedicamentService;
import edu.ezip.ing1.pds.services.OrdonnanceService;

public class OrdonnanceFrontEnd extends JFrame {

    private final static String LoggingLabel = "FrontEnd - Ordonnance";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";

    private OrdonnanceService ordonnanceService;

    private JTextField idPatientField; 
    private JTextField idConsultationField;
    private JTextField descriptionField;
    private JTextField idMedecinField;
    private JComboBox<String> medicamentComboBox;
    private JTextArea displayArea;

    private DefaultTableModel ordonnanceTableModel;
    private JTable ordonnanceTable;

    public OrdonnanceFrontEnd() throws InterruptedException, IOException {
        NetworkConfig networkConfig = null;
        try {
            networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
            logger.debug("Load Network config file : {}", networkConfig);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du fichier de configuration", e);
            JOptionPane.showMessageDialog(null, "Erreur lors du chargement de la configuration réseau.");
            return;
        }

        final OrdonnanceService ordonnanceService = new OrdonnanceService(networkConfig);
        final MedicamentService medicamentService = new MedicamentService(networkConfig);

        List<Medicament> medicamentsList = medicamentService.selectMedicaments();
        medicamentComboBox = new JComboBox<>();
        if (medicamentsList != null) {
            for (Medicament medicament : medicamentsList) {
                medicamentComboBox.addItem(medicament.getNomMedicament());
            }
        } else {
            medicamentComboBox.addItem("Aucun médicament disponible");
        }

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        idPatientField = new JTextField();
        idConsultationField = new JTextField();
        descriptionField = new JTextField();
        idMedecinField = new JTextField();

        inputPanel.add(new JLabel("ID Patient:"));
        inputPanel.add(idPatientField);
        inputPanel.add(new JLabel("ID Consultation:"));
        inputPanel.add(idConsultationField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("ID Médecin:"));
        inputPanel.add(idMedecinField);
        inputPanel.add(new JLabel("Choisir Médicament:"));
        inputPanel.add(medicamentComboBox);

        // Création du modèle de table pour afficher les ordonnances
        ordonnanceTableModel = new DefaultTableModel();
        ordonnanceTableModel.addColumn("ID Ordonnance");
        ordonnanceTableModel.addColumn("ID Patient");
        ordonnanceTableModel.addColumn("ID Consultation");
        ordonnanceTableModel.addColumn("Description");
        ordonnanceTableModel.addColumn("ID Médecin");

        ordonnanceTable = new JTable(ordonnanceTableModel);
        JScrollPane scrollPane = new JScrollPane(ordonnanceTable);

        // Panel pour afficher la table des ordonnances
        JPanel tablePanel = new JPanel();
        tablePanel.add(scrollPane);

        displayArea = new JTextArea(5, 30);
        displayArea.setEditable(false);
        JPanel displayPanel = new JPanel();
        displayPanel.add(new JLabel("Informations saisies :"));
        displayPanel.add(displayArea);

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            String idPatient = idPatientField.getText();
            String idConsultationStr = idConsultationField.getText();
            String description = descriptionField.getText();
            String idMedecin = idMedecinField.getText();
            String selectedMedicament = (String) medicamentComboBox.getSelectedItem();

            if (idPatient.isEmpty() || idConsultationStr.isEmpty() || description.isEmpty() || idMedecin.isEmpty() || selectedMedicament == null) {
                JOptionPane.showMessageDialog(null, "Tous les champs doivent être remplis");
            } else {
                try {
                    int idConsultation = Integer.parseInt(idConsultationStr);
                    int idMedecinInt = Integer.parseInt(idMedecin);

                    Ordonnance newOrdonnance = new Ordonnance();
                    newOrdonnance.setIdOrdonnance(Ordonnance.generateIdOrdonnance());
                    newOrdonnance.setIdPatient(Integer.parseInt(idPatient));
                    newOrdonnance.setIdConsultation(idConsultation);
                    newOrdonnance.setDescription(description);
                    newOrdonnance.setIdMedecin(idMedecinInt);

                    ordonnanceService.insertOrdonnance(newOrdonnance, List.of(selectedMedicament));

                    displayArea.append("Ordonnance enregistrée :\n");
                    displayArea.append("ID Patient: " + idPatient + "\n");
                    displayArea.append("ID Consultation: " + idConsultation + "\n");
                    displayArea.append("Description: " + description + "\n");
                    displayArea.append("ID Médecin: " + idMedecin + "\n");
                    displayArea.append("Médicament: " + selectedMedicament + "\n");
                    displayArea.append("-----------------------------\n");

                    JOptionPane.showMessageDialog(null, "Ordonnance enregistrée avec succès");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Veuillez entrer des valeurs valides pour les ID.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de l'enregistrement de l'ordonnance: " + ex.getMessage());
                }
            }
        });

        // Afficher toutes les ordonnances
        JButton afficherButton = new JButton("Afficher toutes les ordonnances");
        afficherButton.addActionListener(e -> {
            actualiserOrdonnances();
        });

        // Action du bouton Supprimer
        JButton supprimerButton = new JButton("Supprimer");
        supprimerButton.addActionListener(e -> {
            int selectedRow = ordonnanceTable.getSelectedRow(); // Récupérer la ligne sélectionnée dans la table
            if (selectedRow != -1) {
                // Récupérer l'ID de l'ordonnance sélectionnée
                int idOrdonnance = (int) ordonnanceTableModel.getValueAt(selectedRow, 0);

                try {
                    // Créer un objet Ordonnance à supprimer en utilisant l'ID
                    Ordonnance ordonnanceToDelete = new Ordonnance();
                    ordonnanceToDelete.setIdOrdonnance(idOrdonnance);

                    // Appeler le service pour supprimer l'ordonnance
                    ordonnanceService.deleteOrdonnance(ordonnanceToDelete);

                    // Supprimer la ligne de la table
                    ordonnanceTableModel.removeRow(selectedRow);

                    // Afficher un message de succès
                    JOptionPane.showMessageDialog(null, "Ordonnance supprimée avec succès.");
                } catch (Exception ex) {
                    // Capturer les exceptions si une erreur se produit
                    JOptionPane.showMessageDialog(null, "Erreur lors de la suppression de l'ordonnance: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    logger.error("Erreur lors de la suppression de l'ordonnance", ex);
                }
            } else {
                // Afficher une erreur si aucune ligne n'est sélectionnée
                JOptionPane.showMessageDialog(null, "Veuillez sélectionner une ordonnance à supprimer.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(afficherButton);
        buttonPanel.add(supprimerButton);

        this.setLayout(new BorderLayout());
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(displayPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setSize(600, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void actualiserOrdonnances() {
        try {
            Ordonnances ordonnances = ordonnanceService.selectOrdonnances(); // Retourne un objet Ordonnances
            if (ordonnances != null) {
                List<Ordonnance> ordonnancesList = ordonnances.getOrdonnances(); // Récupérer la liste d'ordonnances
                ordonnanceTableModel.setRowCount(0); // Réinitialiser la table
                for (Ordonnance ordonnance : ordonnancesList) {
                    ordonnanceTableModel.addRow(new Object[]{
                        ordonnance.getIdOrdonnance(),
                        ordonnance.getIdPatient(),
                        ordonnance.getIdConsultation(),
                        ordonnance.getDescription(),
                        ordonnance.getIdMedecin()
                    });
                }
            } else {
                JOptionPane.showMessageDialog(null, "Aucune ordonnance trouvée.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erreur lors de l'affichage des ordonnances: " + ex.getMessage());
        }
    }

   
    public static void main(String[] args) {
        try {
            new OrdonnanceFrontEnd();
        } catch (InterruptedException | IOException e) {
            System.err.println("Erreur lors du démarrage de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
