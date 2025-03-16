package edu.ezip.ing1.pds;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.frontend.FenetreDiagnostic;
import edu.ezip.ing1.pds.frontend.OrdonnanceFrontEnd;
import edu.ezip.ing1.pds.services.PatientService;

public class MainFrontEnd {

    private final static String LoggingLabel = "FrontEnd";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";

    public static void main(String[] args) throws IOException, InterruptedException {
        // Charger la configuration du réseau
        final NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
        logger.debug("Load Network config file : {}", networkConfig.toString());

        // Récupération des patients
        final PatientService patientService = new PatientService(networkConfig);
        Patients patients = patientService.selectPatients();

        // Lancer l'interface graphique avec les boutons
        createUI(patients, patientService);
    }

    private static void createUI(Patients patients, PatientService patientService) {
        JFrame frame = new JFrame("Interface");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JButton btnOmar = new JButton("Omar");
        JButton btnAfrah = new JButton("Afrah");
        JButton btnEmna = new JButton("Emna");
        btnEmna.addActionListener(e -> {
            try {
                new OrdonnanceFrontEnd();
            } catch (InterruptedException | IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Erreur lors de l'ouverture de l'interface ordonnance : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ActionListener pour le bouton Omar
        btnOmar.addActionListener(e -> new PatientUI(patients, patientService));

         // ActionListener pour le bouton Afrah (ouvre l'interface FenetreDiagnostic)
    btnAfrah.addActionListener(e -> {
        FenetreDiagnostic fenetreDiagnostic = new FenetreDiagnostic();
        fenetreDiagnostic.setVisible(true); // Affiche la fenêtre directement ici
        fenetreDiagnostic.setLocationRelativeTo(null); // Centre la fenêtre
    });

        // ActionListener pour le bouton Emna (ouvre l'interface OrdonnanceFrontEnd)

        // Ajouter les boutons à l'interface
        frame.add(btnOmar);
        frame.add(btnAfrah);
        frame.add(btnEmna);

        frame.setVisible(true);
    }
}
