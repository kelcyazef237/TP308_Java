package metier.ui.components;

import metier.ui.icons.IconeLucide;
import metier.ui.icons.SvgLoader;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Carte de metrique compacte (style Linear/Vercel).
 * Layout vertical : petit icone + valeur (semi-bold) + label.
 * Pas d'ombre en mode sombre — uniquement un 1px de bordure fine.
 */
public class CarteMetrique extends JPanel {

    private final JLabel lblValeur;
    private final JLabel lblEtiquette;
    private final JLabel lblIcone;
    private final String nomIcone;
    private final Supplier<Color> couleurIcone;

    public CarteMetrique(String label, String nomIcone) {
        this(label, nomIcone, () -> GestionnaireTheme.actif().texteSecondaire());
    }

    public CarteMetrique(String label, String nomIcone, Supplier<Color> couleurIcone) {
        this.nomIcone = nomIcone;
        this.couleurIcone = couleurIcone;

        setOpaque(false);
        setLayout(new BorderLayout(0, 6));
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setPreferredSize(new Dimension(220, 110));
        setMinimumSize(new Dimension(160, 100));

        // Bandeau superieur : icone + espace
        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);

        lblIcone = new JLabel(IconeLucide.de(nomIcone, 16, couleurIcone));
        haut.add(lblIcone, BorderLayout.WEST);
        add(haut, BorderLayout.NORTH);

        // Centre : valeur
        lblValeur = new JLabel("0");
        Theme t = GestionnaireTheme.actif();
        lblValeur.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        lblValeur.setForeground(t.textePrimaire());
        add(lblValeur, BorderLayout.CENTER);

        // Bas : label
        lblEtiquette = new JLabel(label);
        lblEtiquette.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblEtiquette.setForeground(t.texteSecondaire());
        add(lblEtiquette, BorderLayout.SOUTH);

        // Theme change listener for light/dark mode
        GestionnaireTheme.ajouterEcouteur(evt -> {
            Theme tt = GestionnaireTheme.actif();
            lblValeur.setForeground(tt.textePrimaire());
            lblEtiquette.setForeground(tt.texteSecondaire());
            lblIcone.setIcon(IconeLucide.de(nomIcone, 16, couleurIcone));
            repaint();
        });
    }

    public void setValeur(String valeur) {
        lblValeur.setText(valeur);
    }

    public void setCouleurValeur(Color c) {
        lblValeur.setForeground(c);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme t = GestionnaireTheme.actif();
        int w = getWidth();
        int h = getHeight();
        int radius = 10;

        // Fond surface
        g2.setColor(t.surface());
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        // Bordure 1px
        g2.setColor(t.bordure());
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
