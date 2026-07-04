package metier.ui.components;

import metier.ConteneurRefrigere;
import metier.persistance.MesureTemperature;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Graphique lineaire des temperatures en temps reel.
 * Une ligne par conteneur refrigere, bandeau de seuil par conteneur,
 * grille horizontale, legende sur la droite.
 */
public class GraphiqueTemperature extends JPanel {

    private final List<ConteneurRefrigere> conteneurs = new ArrayList<>();
    private Map<Integer, Deque<MesureTemperature>> historique = new LinkedHashMap<>();
    private static final int LECTURES = 60;

    public GraphiqueTemperature() {
        setOpaque(false);
    }

    public void setConteneurs(List<ConteneurRefrigere> liste) {
        this.conteneurs.clear();
        this.conteneurs.addAll(liste);
        repaint();
    }

    public void actualiser(Map<Integer, Deque<MesureTemperature>> hist) {
        this.historique = hist;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Theme t = GestionnaireTheme.actif();
        int w = getWidth();
        int h = getHeight();
        int radius = 10;

        // Fond carte
        g2.setColor(t.surface());
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        g2.setColor(t.bordure());
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

        // Titre
        g2.setColor(t.textePrimaire());
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.drawString("Temperatures en temps reel", 20, 28);

        // Zone graphique
        int paddingTop = 50;
        int paddingBottom = 80; // place pour la legende
        int paddingLeft = 20;
        int paddingRight = 220; // place pour la legende
        int zoneW = w - paddingLeft - paddingRight;
        int zoneH = h - paddingTop - paddingBottom;
        int zoneX = paddingLeft;
        int zoneY = paddingTop;

        // Bornes globales de temperature
        double tMin = Double.POSITIVE_INFINITY;
        double tMax = Double.NEGATIVE_INFINITY;
        for (ConteneurRefrigere cr : conteneurs) {
            tMin = Math.min(tMin, cr.getTemperatureMin());
            tMax = Math.max(tMax, cr.getTemperatureMax());
            // inclure dernieres lectures
            Deque<MesureTemperature> h2 = historique.get(cr.getId());
            if (h2 != null) {
                for (MesureTemperature m : h2) {
                    tMin = Math.min(tMin, m.temperature);
                    tMax = Math.max(tMax, m.temperature);
                }
            }
        }
        if (conteneurs.isEmpty()) {
            tMin = -5;
            tMax = 15;
        }
        // Pad
        double pad = Math.max(2.0, (tMax - tMin) * 0.15);
        tMin -= pad;
        tMax += pad;
        if (tMax == tMin) tMax = tMin + 1;

        // Grille horizontale
        g2.setColor(new Color(t.bordure().getRed(), t.bordure().getGreen(), t.bordure().getBlue(), 60));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 4; i++) {
            int y = zoneY + (int) (i * (zoneH / 4.0));
            g2.drawLine(zoneX, y, zoneX + zoneW, y);
            double tempLabel = tMax - (tMax - tMin) * (i / 4.0);
            g2.setColor(t.texteAtténué());
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g2.drawString(String.format("%.0f\u00B0C", tempLabel), zoneX + zoneW + 6, y + 4);
            g2.setColor(new Color(t.bordure().getRed(), t.bordure().getGreen(), t.bordure().getBlue(), 60));
        }

        // Couleurs des lignes
        Color[] palette = new Color[] {
            new Color(0x5E, 0x6A, 0xD2),
            new Color(0x4C, 0xB7, 0x82),
            new Color(0xF2, 0xC9, 0x4C),
            new Color(0xEB, 0x57, 0x57),
            new Color(0x56, 0xA8, 0xF5),
            new Color(0x9B, 0x59, 0xB6),
            new Color(0xE0, 0x70, 0x4F),
            new Color(0x37, 0xC8, 0xC8)
        };

        // Tracer une ligne par conteneur
        for (int idx = 0; idx < conteneurs.size(); idx++) {
            ConteneurRefrigere cr = conteneurs.get(idx);
            Color couleur = palette[idx % palette.length];
            Deque<MesureTemperature> dq = historique.get(cr.getId());
            if (dq == null) continue;

            // Seuils acceptables - bandeau translucide
            int yMin = (int) (zoneY + ((tMax - cr.getTemperatureMin()) / (tMax - tMin)) * zoneH);
            int yMax = (int) (zoneY + ((tMax - cr.getTemperatureMax()) / (tMax - tMin)) * zoneH);
            int top = Math.min(yMin, yMax);
            int bot = Math.max(yMin, yMax);
            top = Math.max(zoneY, Math.min(zoneY + zoneH, top));
            bot = Math.max(zoneY, Math.min(zoneY + zoneH, bot));
            g2.setColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 30));
            g2.fillRect(zoneX, top, zoneW, bot - top);

            // Ligne
            List<MesureTemperature> lectures = new ArrayList<>(dq);
            if (lectures.size() < 2) continue;
            Path2D path = new Path2D.Double();
            int n = lectures.size();
            for (int i = 0; i < n; i++) {
                MesureTemperature m = lectures.get(i);
                double ratioX = (double) i / (LECTURES - 1);
                int x = zoneX + (int) (ratioX * zoneW);
                double ratioY = (tMax - m.temperature) / (tMax - tMin);
                int y = zoneY + (int) (ratioY * zoneH);
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            g2.setColor(couleur);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);

            // Point final (dernier)
            MesureTemperature last = lectures.get(n - 1);
            double ratioX = (double) (n - 1) / (LECTURES - 1);
            int x = zoneX + (int) (ratioX * zoneW);
            double ratioY = (tMax - last.temperature) / (tMax - tMin);
            int y = zoneY + (int) (ratioY * zoneH);
            g2.setColor(last.enAlerte ? t.danger() : couleur);
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.setColor(t.surface());
            g2.fillOval(x - 2, y - 2, 4, 4);
        }

        // Legende (sous le graphique)
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        int legX = paddingLeft;
        int legY = zoneY + zoneH + 24;
        int colW = 180;
        for (int idx = 0; idx < conteneurs.size(); idx++) {
            ConteneurRefrigere cr = conteneurs.get(idx);
            Color couleur = palette[idx % palette.length];
            int row = idx / 3;
            int col = idx % 3;
            int x = legX + col * colW;
            int y = legY + row * 18;
            // Pastille
            g2.setColor(couleur);
            g2.fillOval(x, y - 6, 8, 8);
            // Texte
            g2.setColor(t.textePrimaire());
            String lbl = cr.getNumeroConteneur() + "  " + String.format("%.1f\u00B0C", cr.getTemperatureActuelle());
            g2.drawString(lbl, x + 14, y);
        }

        // Si aucun conteneur
        if (conteneurs.isEmpty()) {
            g2.setColor(t.texteAtténué());
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            String msg = "Aucun conteneur refrigeré — rien a afficher";
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(msg);
            g2.drawString(msg, (w - tw) / 2, h / 2);
        }

        g2.dispose();
    }
}
