package metier.ui.renderers;

import metier.*;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Rendu des cellules du tableau des cargaisons.
 * Theme-aware, pill badges colores pour le statut, alignements precis.
 */
public class RenduTableauCargaison extends DefaultTableCellRenderer {

    private Color couleurBadge = null;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Theme t = GestionnaireTheme.actif();
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setBorder(new EmptyBorder(0, 16, 0, 16));
        label.setVerticalAlignment(SwingConstants.CENTER);

        if (table.getModel() instanceof metier.ui.models.ModeleTableCargaison modele) {
            Marchandise marchandise = modele.getCargaisonAt(table.convertRowIndexToModel(row));
            boolean estRefrigere = marchandise instanceof ConteneurRefrigere;
            boolean estEnAlerte = false;
            if (estRefrigere) {
                estEnAlerte = ((ConteneurRefrigere) marchandise).estEnAlerte();
            }

            if (isSelected) {
                label.setBackground(t.accent());
                label.setForeground(t.selectionFg());
                this.couleurBadge = null;
            } else {
                // Zebra
                boolean sombre = t.estSombre();
                if (row % 2 == 0) {
                    label.setBackground(sombre ? t.surface() : t.surface());
                } else {
                    label.setBackground(sombre ? t.surfaceElevee() : new Color(0xFA, 0xFB, 0xFC));
                }
                // Alerte -> teinte rouge
                if (estEnAlerte) {
                    label.setBackground(sombre
                        ? new Color(0x3A, 0x1F, 0x1F)
                        : new Color(0xFE, 0xF2, 0xF2));
                }
                label.setForeground(t.textePrimaire());
            }

            int colStatut = metier.ui.models.ModeleTableCargaison.getColonneStatut();
            int colType = metier.ui.models.ModeleTableCargaison.getColonneType();

            if (column == colStatut && !isSelected) {
                Color c = getCouleurStatut(value != null ? value.toString() : "");
                label.setForeground(c);
                label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(0, 0, 0, 0));
                this.couleurBadge = c;
            } else {
                this.couleurBadge = null;
                if (column == colType && !isSelected) {
                    if (estRefrigere) {
                        label.setForeground(new Color(0x56, 0xC2, 0xE0));
                    } else {
                        label.setForeground(t.texteSecondaire());
                    }
                }
            }
        }

        switch (column) {
            case 0: case 1: case 6: case 8: case 10:
                label.setHorizontalAlignment(SwingConstants.CENTER);
                break;
            case 3: case 9:
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                break;
            default:
                label.setHorizontalAlignment(SwingConstants.LEFT);
        }

        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (couleurBadge != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 12;
            int badgeH = 22;
            int badgeY = (h - badgeH) / 2;
            int badgeW = w - 16;
            int badgeX = 8;

            g2.setColor(new Color(couleurBadge.getRed(), couleurBadge.getGreen(), couleurBadge.getBlue(), 30));
            g2.fill(new RoundRectangle2D.Float(badgeX, badgeY, badgeW, badgeH, arc, arc));
            g2.setColor(new Color(couleurBadge.getRed(), couleurBadge.getGreen(), couleurBadge.getBlue(), 120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(badgeX, badgeY, badgeW, badgeH, arc, arc));
            g2.dispose();
        }
        super.paintComponent(g);
    }

    private Color getCouleurStatut(String statut) {
        if (statut == null) return GestionnaireTheme.actif().textePrimaire();
        Theme t = GestionnaireTheme.actif();
        return switch (statut.toLowerCase()) {
            case "arrivee", "arrivée", "livre", "livré" -> t.succes();
            case "en transit" -> t.info();
            case "en attente" -> t.avertissement();
            case "alerte" -> t.danger();
            case "inspection" -> new Color(0x9B, 0x59, 0xB6);
            default -> t.textePrimaire();
        };
    }
}
