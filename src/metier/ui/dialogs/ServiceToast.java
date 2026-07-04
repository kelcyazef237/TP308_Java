package metier.ui.dialogs;

import metier.ui.icons.IconeLucide;
import metier.ui.theme.GestionnaireTheme;
import metier.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de notifications toast (en bas a droite de la fenetre).
 * Remplace le bandeau de notification en haut par un systeme moderne
 * type Vercel/Linear : toasts empiles, slide-in, hold, fade out.
 *
 * <p>API statique : {@link #info}, {@link #succes}, {@link #avertir}, {@link #erreur}.</p>
 */
public final class ServiceToast {

    public enum Niveau { INFO, SUCCES, AVERTIR, ERREUR }

    private static final int LARGEUR = 360;
    private static final int HAUTEUR = 56;
    private static final int ESPACE = 10;
    private static final int INSET_DROITE = 20;
    private static final int INSET_BAS = 20;
    private static final int DUREE_HOLD_MS = 4000;
    private static final int DUREE_ANIM_MS = 200;

    // Liste des toasts actuellement affiches (pour empilage)
    private static final List<FenetreToast> ACTIFS = new ArrayList<>();

    private ServiceToast() {}

    public static void info(String message) { afficher(Niveau.INFO, message); }
    public static void succes(String message) { afficher(Niveau.SUCCES, message); }
    public static void avertir(String message) { afficher(Niveau.AVERTIR, message); }
    public static void erreur(String message) { afficher(Niveau.ERREUR, message); }

    private static void afficher(Niveau niveau, String message) {
        // Verifier qu'il y a une fenetre visible sinon ignorer
        Window owner = Window.getWindows().length > 0 ? Window.getWindows()[0] : null;
        if (owner == null || !owner.isVisible()) {
            // Log console en fallback
            System.out.println("[Toast " + niveau + "] " + message);
            return;
        }

        FenetreToast toast = new FenetreToast(niveau, message);
        ACTIFS.add(toast);
        recompactage();
        toast.lancerAnimationEntree();
    }

    private static void recompactage() {
        // Repositionne tous les toasts actifs en pile (en bas a droite)
        Window owner = Window.getWindows()[0];
        if (owner == null) return;
        Rectangle bounds = owner.getBounds();
        int x = bounds.x + bounds.width - LARGEUR - INSET_DROITE;
        int yBase = bounds.y + bounds.height - INSET_BAS;
        synchronized (ACTIFS) {
            for (int i = 0; i < ACTIFS.size(); i++) {
                FenetreToast t = ACTIFS.get(ACTIFS.size() - 1 - i);
                int y = yBase - HAUTEUR * (i + 1) - ESPACE * i;
                if (y != t.getY()) {
                    t.setLocation(x, y);
                }
            }
        }
    }

    static void retirer(FenetreToast t) {
        synchronized (ACTIFS) {
            ACTIFS.remove(t);
        }
        recompactage();
    }

    // ==================== Fenetre interne ====================

    static class FenetreToast extends JWindow {
        private final Niveau niveau;
        private final long horodatageCreation;
        private float alpha = 0f;
        private int xCible;
        private Timer timerAnimation;
        private Timer timerHold;
        private long debutAnim;

        FenetreToast(Niveau niveau, String message) {
            this.niveau = niveau;
            this.horodatageCreation = System.currentTimeMillis();

            setSize(LARGEUR, HAUTEUR);
            setAlwaysOnTop(true);

            // Fond transparent (le JPanel fait le rendu)
            setBackground(new Color(0, 0, 0, 0));

            PanneauToast panneau = new PanneauToast(niveau, message, this);
            setContentPane(panneau);
        }

        void lancerAnimationEntree() {
            Window owner = Window.getWindows()[0];
            Rectangle bounds = owner.getBounds();
            xCible = bounds.x + bounds.width - LARGEUR - INSET_DROITE;
            int y = bounds.y + bounds.height - INSET_BAS - HAUTEUR;
            // Demarrer legerement a droite
            setLocation(xCible + 40, y);
            setVisible(true);

            debutAnim = System.currentTimeMillis();
            timerAnimation = new Timer(15, e -> animerEntree());
            timerAnimation.start();
        }

        private void animerEntree() {
            long elapsed = System.currentTimeMillis() - debutAnim;
            float t = Math.min(1f, (float) elapsed / DUREE_ANIM_MS);
            // Ease-out
            float eased = 1f - (1f - t) * (1f - t);
            int currentX = xCible + (int) ((1f - eased) * 40);
            setLocation(currentX, getY());
            alpha = eased;
            repaint();
            if (t >= 1f) {
                timerAnimation.stop();
                timerAnimation = null;
                // Lancer le hold puis le fade out
                timerHold = new Timer(DUREE_HOLD_MS, e -> lancerSortie());
                timerHold.setRepeats(false);
                timerHold.start();
            }
        }

        void lancerSortie() {
            if (timerHold != null) { timerHold.stop(); timerHold = null; }
            debutAnim = System.currentTimeMillis();
            timerAnimation = new Timer(15, e -> animerSortie());
            timerAnimation.start();
        }

        private void animerSortie() {
            long elapsed = System.currentTimeMillis() - debutAnim;
            float t = Math.min(1f, (float) elapsed / DUREE_ANIM_MS);
            alpha = 1f - t;
            repaint();
            if (t >= 1f) {
                timerAnimation.stop();
                timerAnimation = null;
                setVisible(false);
                dispose();
                retirer(this);
            }
        }

        void dismiss() {
            if (timerHold != null) { timerHold.stop(); timerHold = null; }
            lancerSortie();
        }

        @Override
        public Rectangle getBounds() {
            return super.getBounds();
        }
    }

    /**
     * Rendu visuel d'un toast : carte arrondie, icone a gauche, texte, fermeture au clic.
     */
    static class PanneauToast extends JPanel {
        private final Niveau niveau;
        private final String message;
        private final FenetreToast fenetre;

        PanneauToast(Niveau niveau, String message, FenetreToast fenetre) {
            this.niveau = niveau;
            this.message = message;
            this.fenetre = fenetre;
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { fenetre.dismiss(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Theme t = GestionnaireTheme.actif();
            int w = getWidth();
            int h = getHeight();
            int arc = 10;

            // Alpha global
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fenetre.alpha));

            // Ombre douce
            g2.setColor(new Color(0, 0, 0, (int) (60 * fenetre.alpha)));
            g2.fillRoundRect(2, 4, w - 4, h - 6, arc, arc);

            // Fond
            g2.setColor(t.surface());
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Bordure
            g2.setColor(t.bordureForte());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Bandeau de couleur a gauche
            Color accent = couleurAccent(niveau);
            g2.setColor(accent);
            g2.fillRoundRect(0, 0, 4, h, 4, 4);

            // Icone
            String nomIcone = switch (niveau) {
                case SUCCES -> "check";
                case ERREUR -> "x";
                case AVERTIR -> "triangle-alert";
                case INFO -> "circle-dot";
            };
            IconeLucide icone = IconeLucide.de(nomIcone, 18, accent);
            icone.paintIcon(this, g2, 18, (h - 18) / 2);

            // Texte
            g2.setColor(t.textePrimaire());
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            int tx = 50;
            int twMax = w - tx - 18;
            FontMetrics fm = g2.getFontMetrics();
            String texte = message != null ? message : "";
            // Troncature simple
            if (fm.stringWidth(texte) > twMax) {
                while (texte.length() > 0 && fm.stringWidth(texte + "...") > twMax) {
                    texte = texte.substring(0, texte.length() - 1);
                }
                texte = texte + "...";
            }
            g2.drawString(texte, tx, h / 2 + fm.getAscent() / 2 - 2);

            // Indicateur de fermeture (X subtil)
            g2.setColor(t.texteAtténué());
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String croix = "\u00D7";
            int cx = w - 18;
            int cy = h / 2 + 4;
            g2.drawString(croix, cx, cy);

            g2.dispose();
        }

        private Color couleurAccent(Niveau n) {
            Theme t = GestionnaireTheme.actif();
            return switch (n) {
                case SUCCES -> t.succes();
                case ERREUR -> t.danger();
                case AVERTIR -> t.avertissement();
                case INFO -> t.info();
            };
        }
    }
}
