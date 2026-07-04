package cm.portdouala.threading;

import cm.portdouala.threading.GestionnaireThreads;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Fenêtre de test TEMPORAIRE pour valider le threading
 * sans attendre l'équipe IHM.
 */
public class FenetreTestThreading extends JFrame {

    private GestionnaireThreads gestionnaire;

    public FenetreTestThreading() {
        setTitle("🚢 Test Threading — Port de Douala");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ─── Tableau ──────────────────────────────────────────────────
        String[] colonnes = {
                "ID", "Désignation", "Consigne", "Actuelle", "Écart", "Statut", "État"
        };
        DefaultTableModel tableModel = new DefaultTableModel(colonnes, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ─── Labels de statut ─────────────────────────────────────────
        JLabel labelStatuts  = new JLabel("En attente...");
        JLabel labelStats    = new JLabel("Simulation non démarrée");

        JPanel panelStatut = new JPanel(new GridLayout(2, 1));
        panelStatut.add(labelStatuts);
        panelStatut.add(labelStats);
        add(panelStatut, BorderLayout.NORTH);

        // ─── Journal des alertes ──────────────────────────────────────
        JTextArea journalAlertes = new JTextArea(5, 40);
        journalAlertes.setEditable(false);
        journalAlertes.setBackground(new Color(255, 240, 240));
        journalAlertes.setFont(new Font("Monospaced", Font.PLAIN, 11));
        add(new JScrollPane(journalAlertes), BorderLayout.SOUTH);

        // ─── Boutons ──────────────────────────────────────────────────
        JButton btnDemarrer = new JButton("▶ Démarrer");
        JButton btnPause    = new JButton("⏸ Pause");
        JButton btnArreter  = new JButton("⏹ Arrêter");

        btnPause.setEnabled(false);
        btnArreter.setEnabled(false);

        // Instancier le gestionnaire avec nos composants de test
        gestionnaire = new GestionnaireThreads(tableModel, labelStatuts, labelStats, journalAlertes);

        // Brancher les boutons
        btnDemarrer.addActionListener(e -> {
            gestionnaire.demarrerSimulation();
            btnDemarrer.setEnabled(false);
            btnPause.setEnabled(true);
            btnArreter.setEnabled(true);
        });

        btnPause.addActionListener(e -> {
            if (gestionnaire.estEnPause()) {
                gestionnaire.reprendreSimulation();
                btnPause.setText("⏸ Pause");
            } else {
                gestionnaire.pauseSimulation();
                btnPause.setText("▶ Reprendre");
            }
        });

        btnArreter.addActionListener(e -> {
            gestionnaire.arreterSimulation();
            btnDemarrer.setEnabled(true);
            btnPause.setEnabled(false);
            btnArreter.setEnabled(false);
            btnPause.setText("⏸ Pause");
        });

        JPanel panelBoutons = new JPanel();
        panelBoutons.add(btnDemarrer);
        panelBoutons.add(btnPause);
        panelBoutons.add(btnArreter);
        add(panelBoutons, BorderLayout.EAST);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        // Toujours lancer une JFrame sur l'EDT
        SwingUtilities.invokeLater(FenetreTestThreading::new);
    }
}