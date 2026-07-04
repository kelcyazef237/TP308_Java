package cm.portdouala.threading;

import cm.portdouala.collections.CargaisonManager;
import cm.portdouala.metier.ConteneurRefrigere;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WorkerMiseAJourIHM
        extends SwingWorker<Void, List<ConteneurRefrigere>> {

    private final DefaultTableModel tableModel;
    private final JLabel            labelStatuts;
    private final JLabel            labelStats;
    private final StatistiquesSimulation stats =
            StatistiquesSimulation.getInstance();

    public WorkerMiseAJourIHM(DefaultTableModel tableModel,
                              JLabel labelStatuts,
                              JLabel labelStats) {
        this.tableModel   = tableModel;
        this.labelStatuts = labelStatuts;
        this.labelStats   = labelStats;
    }

    @Override
    protected Void doInBackground() throws Exception {
        List<ConteneurRefrigere> snapshot =
                CargaisonManager.getInstance().getConteneurRefrigeres();
        publish(snapshot);
        return null;
    }

    @Override
    protected void process(List<List<ConteneurRefrigere>> chunks) {
        List<ConteneurRefrigere> dernierSnapshot =
                chunks.get(chunks.size() - 1);
        rafraichirTableau(dernierSnapshot);
        mettreAJourStatuts(dernierSnapshot);
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("Erreur WorkerIHM : " + e.getCause().getMessage());
        }
    }

    private void rafraichirTableau(List<ConteneurRefrigere> liste) {
        tableModel.setRowCount(0);
        for (ConteneurRefrigere c : liste) {
            tableModel.addRow(new Object[]{
                    c.getIdentifiant(),
                    c.getDesignation(),
                    String.format("%.1f°C", c.getTemperatureConsigne()),
                    String.format("%.1f°C", c.getTemperatureActuelle()),
                    String.format("%+.1f°C", c.getEcartTemperature()),
                    c.isTemperatureHorsNorme() ? "⚠ ALERTE" : "✓ Normal"
            });
        }
    }

    private void mettreAJourStatuts(List<ConteneurRefrigere> liste) {
        long nbAlertes = liste.stream()
                .filter(ConteneurRefrigere::isTemperatureHorsNorme)
                .count();

        if (labelStatuts != null) {
            labelStatuts.setText(String.format(
                    "Conteneurs: %d | ⚠ Alertes: %d", liste.size(), nbAlertes));
            labelStatuts.setForeground(
                    nbAlertes > 0
                            ? java.awt.Color.RED
                            : new java.awt.Color(0, 128, 0));
        }
        if (labelStats != null) {
            labelStats.setText("Stats: " + stats);
        }
    }
}