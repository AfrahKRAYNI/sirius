package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import edu.ezip.ing1.pds.services.PatientService;

public class PatientUI {
    private final static Logger logger = LoggerFactory.getLogger("PatientUI");

    private PatientService patientService; // Service pour gérer les patients

    public PatientUI(Patients patients, PatientService patientService) {
        this.patientService = patientService; // Injection du service
        SwingUtilities.invokeLater(() -> createAndShowGUI(patients));
    }

    private void createAndShowGUI(Patients patients) {
        // Créer le modèle de la table
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Nom");
        tableModel.addColumn("Prénom");
        tableModel.addColumn("Âge");

        // Ajouter les patients existants au modèle de table
        if (patients != null && patients.getPatients() != null && !patients.getPatients().isEmpty()) {
            for (Patient patient : patients.getPatients()) {
                tableModel.addRow(new Object[] { patient.getNom(), patient.getPrenom(), patient.getAge() });
            }
        } else {
            logger.debug("Aucun patient trouvé.");
        }

        // Créer une JTable pour afficher les patients
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        // Créer les champs de saisie pour le formulaire
        JTextField nomField = new JTextField(10);
        JTextField prenomField = new JTextField(10);
        JTextField ageField = new JTextField(5);
        JButton ajouterButton = new JButton("Ajouter");
        JButton afficherButton = new JButton("Afficher la liste");
        JButton supprimerButton = new JButton("Supprimer"); // Nouveau bouton pour supprimer un patient

        // Panel pour le formulaire d'ajout
        JPanel formPanel = new JPanel();
        formPanel.add(new JLabel("Nom:"));
        formPanel.add(nomField);
        formPanel.add(new JLabel("Prénom:"));
        formPanel.add(prenomField);
        formPanel.add(new JLabel("Âge:"));
        formPanel.add(ageField);
        formPanel.add(ajouterButton);
        formPanel.add(afficherButton);
        formPanel.add(supprimerButton); // Ajouter le bouton de suppression

        // Action du bouton Ajouter
        ajouterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String ageText = ageField.getText().trim();

                // Vérification que tous les champs sont remplis
                if (!nom.isEmpty() && !prenom.isEmpty() && !ageText.isEmpty()) {
                    try {
                        // Conversion de l'âge et ajout à la table
                        int age = Integer.parseInt(ageText);

                        // Créer un objet Patient
                        Patient patient = new Patient(nom, prenom, age);

                        // Insérer le patient dans la base de données via le service
                        patientService.InsertPatient(patient);

                        // Ajouter le patient à la table (interface graphique)
                        tableModel.addRow(new Object[] { nom, prenom, age });

                        // Réinitialiser les champs de texte
                        nomField.setText("");
                        prenomField.setText("");
                        ageField.setText("");
                    } catch (NumberFormatException ex) {
                        // Afficher une erreur si l'âge est invalide
                        JOptionPane.showMessageDialog(null, "Veuillez entrer un âge valide.", "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (IOException | InterruptedException ex) {
                        // Capturer les exceptions IOException et InterruptedException
                        JOptionPane.showMessageDialog(null, "Une erreur est survenue lors de l'insertion du patient.",
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                        logger.error("Erreur lors de l'insertion du patient", ex);
                    }
                } else {
                    // Afficher une erreur si un champ est vide
                    JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs.", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Action du bouton Afficher
        afficherButton.addActionListener(e -> table.repaint());

        // Action du bouton Supprimer
        supprimerButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                // Récupérer le patient sélectionné
                String nom = (String) tableModel.getValueAt(selectedRow, 0);
                String prenom = (String) tableModel.getValueAt(selectedRow, 1);
                int age = (int) tableModel.getValueAt(selectedRow, 2);

                // Créer un objet Patient à supprimer
                Patient patientToDelete = new Patient(nom, prenom, age);

                try {
                    // Appeler le service pour supprimer le patient
                    patientService.DeletePatient(patientToDelete);

                    // Supprimer la ligne de la table
                    tableModel.removeRow(selectedRow);
                } catch (IOException | InterruptedException ex) {
                    // Capturer les exceptions IOException et InterruptedException
                    JOptionPane.showMessageDialog(null, "Une erreur est survenue lors de la suppression du patient.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    logger.error("Erreur lors de la suppression du patient", ex);
                }
            } else {
                // Afficher une erreur si aucune ligne n'est sélectionnée
                JOptionPane.showMessageDialog(null, "Veuillez sélectionner un patient à supprimer.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Créer une fenêtre JFrame pour afficher la table et le formulaire
        JFrame frame = new JFrame("Gestion des Patients");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Centrer la fenêtre
        frame.setLayout(new BorderLayout());
        frame.add(formPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
