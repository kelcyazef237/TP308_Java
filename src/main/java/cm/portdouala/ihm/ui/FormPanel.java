package cm.portdouala.ihm.ui;

import fr.tp308.ihm.model.ConteneurDto;
import fr.tp308.ihm.util.AppTheme;
import fr.tp308.ihm.util.DialogUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;

public class FormPanel extends JPanel {

    private static final String TYPE_STANDARD = "Standard";
    private static final String TYPE_REFRIGERE = "Réfrigéré";

    private final JComboBox<String> typeCombo;
    private final JTextField codeField;
    private final JFormattedTextField poidsField;
    private final JFormattedTextField temperatureField;
    private final JButton ajouterButton;
    private final JButton modifierButton;
    private final JButton supprimerButton;
    private final JButton reinitialiserButton;
    private final JButton actualiserButton;
    private final JButton afficherAlertesButton;

    private Consumer<ConteneurDto> onAjouter;
    private Consumer<ConteneurDto> onModifier;
    private Consumer<UUID> onSupprimer;
    private Runnable onReinitialiser;
    private Runnable onActualiser;
    private Runnable onAfficherAlertes;
    private UUID currentSelectionId;

    public FormPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR), "Formulaire de conteneur"));
        setBackground(AppTheme.PANEL_BACKGROUND);

        typeCombo = new JComboBox<>(new String[]{TYPE_STANDARD, TYPE_REFRIGERE});
        typeCombo.setFont(AppTheme.LABEL_FONT);
        typeCombo.setPreferredSize(new Dimension(240, 28));
        codeField = new JTextField();
        poidsField = createNumberField();
        temperatureField = createNumberField();
        ajouterButton = createButton("Ajouter");
        modifierButton = createButton("Modifier");
        supprimerButton = createButton("Supprimer");
        reinitialiserButton = createButton("Réinitialiser");
        actualiserButton = createButton("Actualiser");
        afficherAlertesButton = createButton("Afficher uniquement les alertes");

        buildLayout();
        attachListeners();
        updateTemperatureVisibility();
    }

    public void setOnAjouter(Consumer<ConteneurDto> onAjouter) {
        this.onAjouter = onAjouter;
    }

    public void setOnModifier(Consumer<ConteneurDto> onModifier) {
        this.onModifier = onModifier;
    }

    public void setOnSupprimer(Consumer<UUID> onSupprimer) {
        this.onSupprimer = onSupprimer;
    }

    public void setOnReinitialiser(Runnable onReinitialiser) {
        this.onReinitialiser = onReinitialiser;
    }

    public void setOnActualiser(Runnable onActualiser) {
        this.onActualiser = onActualiser;
    }

    public void setOnAfficherAlertes(Runnable onAfficherAlertes) {
        this.onAfficherAlertes = onAfficherAlertes;
    }

    public void resetForm() {
        currentSelectionId = null;
        typeCombo.setSelectedIndex(0);
        codeField.setText("");
        poidsField.setValue(null);
        temperatureField.setValue(null);
        updateTemperatureVisibility();
    }

    public void populateForm(ConteneurDto conteneur) {
        if (conteneur == null) {
            return;
        }
        currentSelectionId = conteneur.getId();
        typeCombo.setSelectedItem(conteneur.getType());
        codeField.setText(conteneur.getCode());
        poidsField.setValue(conteneur.getPoids());
        temperatureField.setValue(conteneur.getTemperature());
        updateTemperatureVisibility();
    }

    private void buildLayout() {
        GridBagConstraints gbc = createDefaultConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(createLabel("Type"), gbc);
        gbc.gridx = 1;
        add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(createLabel("Code"), gbc);
        gbc.gridx = 1;
        add(codeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(createLabel("Poids (kg)"), gbc);
        gbc.gridx = 1;
        add(poidsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(createLabel("Température (°C)"), gbc);
        gbc.gridx = 1;
        add(temperatureField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 8, 4, 8);
        add(new JSeparator(SwingConstants.HORIZONTAL), gbc);

        gbc.gridy = 5;
        add(createButtonPanel(), gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panel.add(ajouterButton);
        panel.add(Box.createRigidArea(new Dimension(8, 0)));
        panel.add(modifierButton);
        panel.add(Box.createRigidArea(new Dimension(8, 0)));
        panel.add(supprimerButton);
        panel.add(Box.createRigidArea(new Dimension(8, 0)));
        panel.add(reinitialiserButton);
        panel.add(Box.createRigidArea(new Dimension(8, 0)));
        panel.add(actualiserButton);
        panel.add(Box.createHorizontalGlue());
        panel.add(afficherAlertesButton);
        return panel;
    }

    private void attachListeners() {
        typeCombo.addItemListener(this::handleTypeChanged);
        ajouterButton.addActionListener(e -> handleAjouter());
        modifierButton.addActionListener(e -> handleModifier());
        supprimerButton.addActionListener(e -> handleSupprimer());
        reinitialiserButton.addActionListener(e -> {
            if (onReinitialiser != null) {
                onReinitialiser.run();
            }
        });
        actualiserButton.addActionListener(e -> {
            if (onActualiser != null) {
                onActualiser.run();
            }
        });
        afficherAlertesButton.addActionListener(e -> {
            if (onAfficherAlertes != null) {
                onAfficherAlertes.run();
            }
        });
    }

    private void handleTypeChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            updateTemperatureVisibility();
        }
    }

    private void handleAjouter() {
        if (onAjouter != null) {
            ConteneurDto conteneur = buildConteneurDto();
            if (conteneur != null) {
                onAjouter.accept(conteneur);
            }
        }
    }

    private void handleModifier() {
        if (currentSelectionId == null) {
            DialogUtils.showError(this, "Aucun conteneur sélectionné pour la modification.");
            return;
        }
        if (onModifier != null) {
            ConteneurDto conteneur = buildConteneurDto();
            if (conteneur != null) {
                conteneur = new ConteneurDto(currentSelectionId,
                        conteneur.getType(),
                        conteneur.getCode(),
                        conteneur.getPoids(),
                        conteneur.getTemperature(),
                        conteneur.isAlerte(),
                        conteneur.getTaxe());
                onModifier.accept(conteneur);
            }
        }
    }

    private void handleSupprimer() {
        if (currentSelectionId == null) {
            DialogUtils.showError(this, "Aucun conteneur sélectionné pour la suppression.");
            return;
        }
        if (onSupprimer != null) {
            onSupprimer.accept(currentSelectionId);
            resetForm();
        }
    }

    private ConteneurDto buildConteneurDto() {
        String type = String.valueOf(typeCombo.getSelectedItem());
        String code = codeField.getText().trim();
        Number poidsValue = (Number) poidsField.getValue();
        Number temperatureValue = (Number) temperatureField.getValue();

        if (code.isEmpty()) {
            DialogUtils.showError(this, "Le code du conteneur ne peut pas être vide.");
            return null;
        }

        if (poidsValue == null || poidsValue.doubleValue() <= 0) {
            DialogUtils.showError(this, "Le poids doit être un nombre strictement positif.");
            return null;
        }

        Double temperature = null;
        boolean alerte = false;
        if (TYPE_REFRIGERE.equals(type)) {
            if (temperatureValue == null) {
                DialogUtils.showError(this, "La température est requise pour un conteneur réfrigéré.");
                return null;
            }
            temperature = temperatureValue.doubleValue();
            alerte = temperature < 0 || temperature > 5;
        }

        return new ConteneurDto(UUID.randomUUID(), type, code, poidsValue.doubleValue(), temperature, alerte, 0.0);
    }

    private void updateTemperatureVisibility() {
        boolean isRefrigerated = TYPE_REFRIGERE.equals(typeCombo.getSelectedItem());
        temperatureField.setEnabled(isRefrigerated);
        temperatureField.setVisible(isRefrigerated);
    }

    private JFormattedTextField createNumberField() {
        NumberFormatter formatter = new NumberFormatter(new DecimalFormat("###0.##"));
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0.0);
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setPreferredSize(new Dimension(220, 28));
        return field;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(AppTheme.BUTTON_FONT);
        button.setBackground(AppTheme.BUTTON_BACKGROUND);
        button.setForeground(AppTheme.BUTTON_FOREGROUND);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return button;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.LABEL_FONT);
        return label;
    }

    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;
        return gbc;
    }
}
