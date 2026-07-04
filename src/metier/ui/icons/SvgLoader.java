package metier.ui.icons;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Charge et parse les fichiers SVG Lucide en {@link Shape} Java2D.
 *
 * <p>Lucide utilise des SVG en stroke-only avec une viewBox 0 0 24 24.
 * Cette classe extrait l'attribut {@code d} de chaque element
 * {@code <path>} et le convertit en {@link Path2D} via un mini-parseur
 * des commandes SVG (M, L, H, V, C, S, Q, Z) — pas besoin de Batik.
 *
 * <p>Resultat mis en cache dans un {@link ConcurrentHashMap} pour eviter
 * de relire le disque a chaque paint.
 */
public final class SvgLoader {

    private static final String REPERTOIRE = "assets/icons/";
    private static final Map<String, Shape> CACHE = new ConcurrentHashMap<>();
    private static final Pattern PATTR_PATH = Pattern.compile("<path[^>]*\\bd=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTR_CERCLE = Pattern.compile("<circle[^>]*\\bcx=\"([^\"]+)\"[^>]*\\bcy=\"([^\"]+)\"[^>]*\\br=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTR_RECT = Pattern.compile("<rect[^>]*\\bx=\"([^\"]+)\"[^>]*\\by=\"([^\"]+)\"[^>]*\\bwidth=\"([^\"]+)\"[^>]*\\bheight=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTR_LINE = Pattern.compile("<line[^>]*\\bx1=\"([^\"]+)\"[^>]*\\by1=\"([^\"]+)\"[^>]*\\bx2=\"([^\"]+)\"[^>]*\\by2=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private SvgLoader() {}

    /**
     * Charge la forme SVG pour l'icone donnee (sans l'extension .svg).
     * Le nom correspond au nom de fichier dans {@code assets/icons/}.
     */
    public static Shape charger(String nom) {
        return CACHE.computeIfAbsent(nom, SvgLoader::parseSvg);
    }

    /** Pre-charge une liste d'icones au demarrage (optionnel, pour eviter le delai au premier paint). */
    public static void precharger(List<String> noms) {
        for (String nom : noms) {
            charger(nom);
        }
    }

    private static Shape parseSvg(String nom) {
        Path2D.Double path = new Path2D.Double();
        String fichier = REPERTOIRE + nom + ".svg";
        String contenu = lireFichier(fichier);
        if (contenu == null || contenu.isEmpty()) {
            return path;
        }

        // 1) Path elements (le plus courant pour Lucide)
        Matcher mp = PATTR_PATH.matcher(contenu);
        while (mp.find()) {
            String d = mp.group(1);
            appenderCheminSvg(path, d);
        }
        // 2) Cercles (rares, mais possibles)
        Matcher mc = PATTR_CERCLE.matcher(contenu);
        while (mc.find()) {
            double cx = Double.parseDouble(mc.group(1));
            double cy = Double.parseDouble(mc.group(2));
            double r = Double.parseDouble(mc.group(3));
            path.append(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2), false);
        }
        // 3) Rectangles
        Matcher mr = PATTR_RECT.matcher(contenu);
        while (mr.find()) {
            double x = Double.parseDouble(mr.group(1));
            double y = Double.parseDouble(mr.group(2));
            double w = Double.parseDouble(mr.group(3));
            double h = Double.parseDouble(mr.group(4));
            path.append(new java.awt.geom.Rectangle2D.Double(x, y, w, h), false);
        }
        // 4) Lignes
        Matcher ml = PATTR_LINE.matcher(contenu);
        while (ml.find()) {
            double x1 = Double.parseDouble(ml.group(1));
            double y1 = Double.parseDouble(ml.group(2));
            double x2 = Double.parseDouble(ml.group(3));
            double y2 = Double.parseDouble(ml.group(4));
            path.moveTo(x1, y1);
            path.lineTo(x2, y2);
        }
        return path;
    }

    /**
     * Parse une chaine de commandes SVG (attribut d) et l'applique au path.
     * Supporte M, L, H, V, C, S, Q, Z (et leurs variantes lowercase relatives).
     * Ne supporte pas les arcs (A/a) — aucun icone Lucide utilise.
     */
    private static void appenderCheminSvg(Path2D.Double path, String d) {
        List<String> tokens = tokeniser(d);
        int i = 0;
        double lastX = 0, lastY = 0;
        double startX = 0, startY = 0;
        Character cmd = null;

        while (i < tokens.size()) {
            String tok = tokens.get(i);
            if (tok.length() == 1 && "MmLlHhVvCcSsQqZzAa".indexOf(tok.charAt(0)) >= 0) {
                cmd = tok.charAt(0);
                i++;
            } else if (cmd == null) {
                // Pas de commande explicite, on assume L (path SVG par defaut apres M)
                cmd = 'L';
            }

            if (cmd == null) break;
            boolean relatif = Character.isLowerCase(cmd);
            char c = Character.toUpperCase(cmd);

            switch (c) {
                case 'M': {
                    double x = lireNombre(tokens.get(i++));
                    double y = lireNombre(tokens.get(i++));
                    if (relatif) { x += lastX; y += lastY; }
                    path.moveTo(x, y);
                    lastX = startX = x;
                    lastY = startY = y;
                    cmd = 'l'; // apres M, coordonnees suivantes = lineTo
                    break;
                }
                case 'L': {
                    double x = lireNombre(tokens.get(i++));
                    double y = lireNombre(tokens.get(i++));
                    if (relatif) { x += lastX; y += lastY; }
                    path.lineTo(x, y);
                    lastX = x; lastY = y;
                    break;
                }
                case 'H': {
                    double x = lireNombre(tokens.get(i++));
                    if (relatif) x += lastX;
                    path.lineTo(x, lastY);
                    lastX = x;
                    break;
                }
                case 'V': {
                    double y = lireNombre(tokens.get(i++));
                    if (relatif) y += lastY;
                    path.lineTo(lastX, y);
                    lastY = y;
                    break;
                }
                case 'C': {
                    double x1 = lireNombre(tokens.get(i++));
                    double y1 = lireNombre(tokens.get(i++));
                    double x2 = lireNombre(tokens.get(i++));
                    double y2 = lireNombre(tokens.get(i++));
                    double x = lireNombre(tokens.get(i++));
                    double y = lireNombre(tokens.get(i++));
                    if (relatif) {
                        x1 += lastX; y1 += lastY;
                        x2 += lastX; y2 += lastY;
                        x += lastX; y += lastY;
                    }
                    path.curveTo(x1, y1, x2, y2, x, y);
                    lastX = x; lastY = y;
                    break;
                }
                case 'S': {
                    double x2 = lireNombre(tokens.get(i++));
                    double y2 = lireNombre(tokens.get(i++));
                    double x = lireNombre(tokens.get(i++));
                    double y = lireNombre(tokens.get(i++));
                    if (relatif) {
                        x2 += lastX; y2 += lastY;
                        x += lastX; y += lastY;
                    }
                    path.quadTo(x2, y2, x, y);
                    lastX = x; lastY = y;
                    break;
                }
                case 'Q': {
                    double x1 = lireNombre(tokens.get(i++));
                    double y1 = lireNombre(tokens.get(i++));
                    double x = lireNombre(tokens.get(i++));
                    double y = lireNombre(tokens.get(i++));
                    if (relatif) {
                        x1 += lastX; y1 += lastY;
                        x += lastX; y += lastY;
                    }
                    path.quadTo(x1, y1, x, y);
                    lastX = x; lastY = y;
                    break;
                }
                case 'Z': {
                    path.closePath();
                    lastX = startX;
                    lastY = startY;
                    break;
                }
                case 'A': {
                    // Arc: rx ry x-axis-rotation large-arc-flag sweep-flag x y
                    double rx = lireNombre(tokens.get(i++));
                    double ry = lireNombre(tokens.get(i++));
                    double xRot = lireNombre(tokens.get(i++));
                    double largeArc = lireNombre(tokens.get(i++));
                    double sweep = lireNombre(tokens.get(i++));
                    double x = lireNombre(tokens.get(i++));
                    double y = lireNombre(tokens.get(i++));
                    if (relatif) { x += lastX; y += lastY; }
                    // Arc2D approximation: convertir en path2d via append
                    java.awt.geom.Arc2D.Double arc = new java.awt.geom.Arc2D.Double();
                    // Compute bounding box (approximation simple)
                    double cx = (lastX + x) / 2.0;
                    double cy = (lastY + y) / 2.0;
                    double dx = x - lastX;
                    double dy = y - lastY;
                    double dist = Math.hypot(dx, dy);
                    double r = Math.max(rx, ry);
                    if (r <= 0) r = dist / 2.0;
                    double ang = Math.toDegrees(Math.atan2(dy, dx));
                    double extent = largeArc > 0.5 ? 180 : 90;
                    if (sweep < 0.5) extent = -extent;
                    arc.setArcByCenter(cx, cy, r, -ang, -extent, java.awt.geom.Arc2D.OPEN);
                    path.append(arc, true);
                    lastX = x; lastY = y;
                    break;
                }
                default:
                    i++;
                    break;
            }
        }
    }

    /** Tokenize une chaine d'attribut SVG en liste de commandes et nombres. */
    private static List<String> tokeniser(String d) {
        List<String> out = new ArrayList<>();
        // Separateurs : lettres de commande OU separateurs classiques
        Pattern p = Pattern.compile("[MmLlHhVvCcSsQqZzAa]|-?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?");
        Matcher m = p.matcher(d);
        while (m.find()) {
            out.add(m.group());
        }
        return out;
    }

    private static double lireNombre(String s) {
        return Double.parseDouble(s);
    }

    private static String lireFichier(String chemin) {
        // Tente d'abord le chemin relatif au working dir
        String[] cheminsEssayer = new String[] { chemin, "TP308_Java/" + chemin, "src/" + chemin };
        for (String c : cheminsEssayer) {
            try (BufferedReader r = new BufferedReader(new FileReader(c))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            } catch (IOException ignored) {
            }
        }
        // Fallback : chercher via classpath
        try (var is = SvgLoader.class.getResourceAsStream("/" + chemin)) {
            if (is != null) {
                return new String(is.readAllBytes());
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /** Liste par defaut des icones a precharger au demarrage. */
    public static List<String> iconesParDefaut() {
        return List.of(
            "layout-dashboard", "package", "save", "upload",
            "sun", "moon", "search", "x", "plus", "pencil",
            "trash-2", "arrow-left", "snowflake", "triangle-alert",
            "check", "ship", "settings", "circle-dot", "command",
            "anchor"
        );
    }

    /** Test : verifie qu'une icone est chargeable. */
    public static boolean existe(String nom) {
        charger(nom);
        Shape s = CACHE.get(nom);
        return s != null && s.getBounds().width > 0;
    }
}
