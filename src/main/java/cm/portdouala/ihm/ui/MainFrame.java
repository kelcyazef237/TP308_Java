package cm.portdouala.ihm.ui;

import fr.tp308.ihm.model.ConteneurDto;
import fr.tp308.ihm.service.IConteneurService;
import fr.tp308.ihm.util.AppTheme;
import fr.tp308.ihm.util.DialogUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.UUID;

public class MainFrame extends JFrame {

    private final IConteneurService conteneurService;
    private final FormPanel formPanel;
    private final TablePanel tablePanel;
    private final StatusBar statusBar;

    public MainFrame(IConteneurService conteneurService) {
        super("Gestion de trafic de fret portuaire");
        this.conteneurService = conteneurService;
        this.formPanel = new FormPanel();
        this.tablePanel = new TablePanel();
        this.statusBar = new StatusBar();
        initializeFrame();
        initializeBindings();
        loadData();
    }

    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        contentPanel.setBackground(AppTheme.BACKGROUND_COLOR);

        formPanel.setBackground(AppTheme.PANEL_BACKGROUND);
        tablePanel.setBackground(AppTheme.PANEL_BACKGROUND);

        contentPanel.add(formPanel);
        contentPanel.add(tablePanel);

        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void initializeBindings() {
        formPanel.setOnAjouter(this::handleAdd);
        formPanel.setOnModifier(this::handleUpdate);
        formPanel.setOnSupprimer(this::handleDelete);
        formPanel.setOnReinitialiser(this::handleReset);
        formPanel.setOnActualiser(this::handleRefresh);
        formPanel.setOnAfficherAlertes(this::handleShowAlerts);

        tablePanel.setSelectionListener(this::handleTableSelection);
    }

    private void loadData() {
        tablePanel.updateTable(conteneurService.findAll());
        statusBar.showProgress("Chargement des conteneurs terminé", tablePanel.getRowCount());
    }

    private void handleAdd(ConteneurDto conteneur) {
        try {
            ConteneurDto saved = conteneurService.save(conteneur);
            tablePanel.addRow(saved);
            statusBar.showProgress("Conteneur ajouté", tablePanel.getRowCount());
            DialogUtils.showInfo(this, "Le conteneur a été ajouté avec succès.");
        } catch (Exception e) {
            DialogUtils.showError(this, "Impossible d'ajouter le conteneur : " + e.getMessage());
        }
    }

    private void handleUpdate(ConteneurDto conteneur) {
        try {
            conteneurService.update(conteneur);
            tablePanel.updateRow(conteneur);
            statusBar.showProgress("Conteneur modifié", tablePanel.getRowCount());
            DialogUtils.showInfo(this, "Le conteneur a été mis à jour avec succès.");
        } catch (Exception e) {
            DialogUtils.showError(this, "Impossible de modifier le conteneur : " + e.getMessage());
        }
    }

    private void handleDelete(UUID id) {
        if (!DialogUtils.confirm(this, "Voulez-vous vraiment supprimer ce conteneur ?")) {
            return;
        }

        try {
            conteneurService.delete(id);
            tablePanel.removeRow(id);
            statusBar.showProgress("Conteneur supprimé", tablePanel.getRowCount());
        } catch (Exception e) {
            DialogUtils.showError(this, "Impossible de supprimer le conteneur : " + e.getMessage());
        }
    }

    private void handleReset() {
        formPanel.resetForm();
        statusBar.showProgress("Formulaire réinitialisé", tablePanel.getRowCount());
    }

    private void handleRefresh() {
        conteneurService.refresh();
        loadData();
    }

    private void handleShowAlerts() {
        List<ConteneurDto> alertes = conteneurService.findAlerts();
        tablePanel.updateTable(alertes);
        statusBar.showProgress(String.format("Affichage des alertes (%d conteneurs)", alertes.size()), alertes.size());
    }

    private void handleTableSelection(ConteneurDto selection) {
        formPanel.populateForm(selection);
        statusBar.showProgress("Conteneur sélectionné : " + selection.getCode(), tablePanel.getRowCount());
    }
}
