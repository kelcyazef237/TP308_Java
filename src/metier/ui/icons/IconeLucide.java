package metier.ui.icons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Implementation Swing d'une icone Lucide vectorielle.
 * La forme est mise a l'echelle et dessinee en stroke avec le {@link Graphics2D}.
 *
 * <p>La couleur peut etre fixee statiquement ou resolue dynamiquement
 * via un {@link Supplier} (utile pour le mode sombre/clair).
 */
public class IconeLucide implements Icon {

    private final String nom;
    private final int taille;
    private final Supplier<Color> fournisseurCouleur;

    private IconeLucide(String nom, int taille, Supplier<Color> couleur) {
        this.nom = nom;
        this.taille = taille;
        this.fournisseurCouleur = couleur;
    }

    /** Cree une icone avec couleur statique. */
    public static IconeLucide de(String nom, Color couleur) {
        return new IconeLucide(nom, 18, () -> couleur);
    }

    /** Cree une icone avec couleur resolue au paint (theme-aware). */
    public static IconeLucide de(String nom, Supplier<Color> couleur) {
        return new IconeLucide(nom, 18, couleur);
    }

    /** Icone avec taille et couleur statique. */
    public static IconeLucide de(String nom, int taille, Color couleur) {
        return new IconeLucide(nom, taille, () -> couleur);
    }

    /** Icone avec taille et couleur dynamique. */
    public static IconeLucide de(String nom, int taille, Supplier<Color> couleur) {
        return new IconeLucide(nom, taille, couleur);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Shape forme = SvgLoader.charger(nom);
        if (forme == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Lucide utilise une viewBox 24x24, on met a l'echelle vers la taille cible
        double scale = (double) taille / 24.0;
        g2.translate(x, y);
        g2.scale(scale, scale);

        g2.setColor(fournisseurCouleur.get());
        // Stroke fin avec coins arrondis (style Lucide)
        g2.setStroke(new BasicStroke(1.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(forme);
        g2.dispose();
    }

    @Override
    public int getIconWidth() { return taille; }

    @Override
    public int getIconHeight() { return taille; }
}
