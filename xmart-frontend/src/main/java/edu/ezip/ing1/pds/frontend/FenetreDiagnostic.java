package edu.ezip.ing1.pds.frontend;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.ServiceSymptome;

public class FenetreDiagnostic extends JFrame {
    private JTextField champSymptome;
    private DefaultListModel<String> modeleListe;
    private JList<String> listeSymptomes;
    private JTextField champModification;
    private JTextArea resultatDiagnostic;
    private JButton boutonAjouter, boutonAfficher, boutonModifier, boutonSupprimer, boutonDiagnostiquer;

    private ServiceSymptome serviceSymptome;
    private int idPatient = 1; 

    public FenetreDiagnostic() {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setIpaddress("127.0.0.1");
        networkConfig.setTcpport(8080);

        serviceSymptome = new ServiceSymptome(networkConfig);

        setTitle("Diagnostic Médical");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelHaut = new JPanel();
        champSymptome = new JTextField(20);
        boutonAjouter = new JButton("Ajouter");
        boutonAfficher = new JButton("Afficher");
        panelHaut.add(new JLabel("Symptôme :"));
        panelHaut.add(champSymptome);
        panelHaut.add(boutonAjouter);
        panelHaut.add(boutonAfficher);
        add(panelHaut, BorderLayout.NORTH);

        modeleListe = new DefaultListModel<>();
        listeSymptomes = new JList<>(modeleListe);
        JScrollPane scrollPane = new JScrollPane(listeSymptomes);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBas = new JPanel();
        champModification = new JTextField(15);
        boutonModifier = new JButton("Modifier");
        boutonSupprimer = new JButton("Supprimer");
        panelBas.add(new JLabel("Modifier le symptôme sélectionné :"));
        panelBas.add(champModification);
        panelBas.add(boutonModifier);
        panelBas.add(boutonSupprimer);
        add(panelBas, BorderLayout.SOUTH);

        JPanel panelDroite = new JPanel(new BorderLayout());
        boutonDiagnostiquer = new JButton("Diagnostiquer");
        resultatDiagnostic = new JTextArea(10, 20);
        resultatDiagnostic.setEditable(false);
        panelDroite.add(boutonDiagnostiquer, BorderLayout.NORTH);
        panelDroite.add(new JScrollPane(resultatDiagnostic), BorderLayout.CENTER);
        add(panelDroite, BorderLayout.EAST);

        boutonAjouter.addActionListener(e -> ajouterSymptome());
        boutonAfficher.addActionListener(e -> afficherSymptomes());
        boutonModifier.addActionListener(e -> modifierSymptome());
        boutonSupprimer.addActionListener(e -> supprimerSymptome());
        boutonDiagnostiquer.addActionListener(e -> diagnostiquer());
    }

    private void ajouterSymptome() {
        String symptomeTexte = champSymptome.getText().trim();
        if (!symptomeTexte.isEmpty()) {
            try {
                Symptomes symptome = new Symptomes(0, symptomeTexte);
                serviceSymptome.insertSymptome(symptome);
                System.out.println("Symptôme ajouté : " + symptomeTexte);
                champSymptome.setText("");
                afficherSymptomes();
            } catch (Exception ex) {
                ex.printStackTrace();
                
            }
        }
    }

    private void afficherSymptomes() {
        modeleListe.clear();
        try {
            List<Symptomes> symptomes = serviceSymptome.selectSymptomes();
            System.out.println("Liste de symptômes récupérée : " + symptomes);
            for (Symptomes s : symptomes) {
                modeleListe.addElement(s.getNom());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void modifierSymptome() {
        String symptomeSelectionne = listeSymptomes.getSelectedValue();
        String nouveauNom = champModification.getText().trim();
        if (symptomeSelectionne != null && !nouveauNom.isEmpty()) {
            try {
                Symptomes symptome = new Symptomes(0, symptomeSelectionne);
                serviceSymptome.updateSymptome(symptome, nouveauNom);
                afficherSymptomes();
                champModification.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void supprimerSymptome() {
        String symptomeSelectionne = listeSymptomes.getSelectedValue();
        if (symptomeSelectionne != null) {
            try {
                Symptomes symptome = new Symptomes(0, symptomeSelectionne);
                serviceSymptome.deleteSymptome(symptome);
                afficherSymptomes();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void diagnostiquer() {
        try {
            List<String> maladies = serviceSymptome.diagnostiquer(idPatient);
            if (maladies.isEmpty()) {
                resultatDiagnostic.setText("Aucune maladie trouvée.");
            } else {
                StringBuilder result = new StringBuilder("Maladies possibles :\n");
                for (String maladie : maladies) {
                    result.append("- ").append(maladie).append("\n");
                }
                resultatDiagnostic.setText(result.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FenetreDiagnostic().setVisible(true));
    }
}
