package metier.ui.theme;

import java.awt.Color;

/**
 * Palette de couleurs d'un theme (sombre ou clair).
 * Implemente en record pour l'immutabilite et la concision.
 */
public record Theme(
    String nom,
    Color bg,
    Color surface,
    Color surfaceElevee,
    Color bordure,
    Color bordureForte,
    Color textePrimaire,
    Color texteSecondaire,
    Color texteAtténué,
    Color accent,
    Color accentHover,
    Color succes,
    Color avertissement,
    Color danger,
    Color info,
    Color focus,
    Color selectionBg,
    Color selectionFg,
    Color ombre
) {

    /** Theme sombre — style Linear/Vercel. */
    public static Theme sombre() {
        return new Theme(
            "sombre",
            new Color(0x0B, 0x0D, 0x10),     // bg
            new Color(0x13, 0x16, 0x1B),     // surface
            new Color(0x1A, 0x1E, 0x25),     // surfaceElevee
            new Color(0x26, 0x2B, 0x33),     // bordure
            new Color(0x3A, 0x40, 0x4A),     // bordureForte
            new Color(0xE6, 0xE8, 0xEB),     // textePrimaire
            new Color(0x9B, 0xA1, 0xAC),     // texteSecondaire
            new Color(0x6B, 0x72, 0x80),     // texteAtténué
            new Color(0x5E, 0x6A, 0xD2),     // accent (indigo Linear)
            new Color(0x7B, 0x85, 0xE5),     // accentHover
            new Color(0x4C, 0xB7, 0x82),     // succes (vert)
            new Color(0xF2, 0xC9, 0x4C),     // avertissement (ambre)
            new Color(0xEB, 0x57, 0x57),     // danger (rouge)
            new Color(0x56, 0xA8, 0xF5),     // info (bleu clair)
            new Color(0x5E, 0x6A, 0xD2),     // focus
            new Color(0x5E, 0x6A, 0xD2),     // selectionBg
            Color.WHITE,                      // selectionFg
            new Color(0, 0, 0, 60)           // ombre
        );
    }

    /** Theme clair — minimal et propre. */
    public static Theme clair() {
        return new Theme(
            "clair",
            new Color(0xFA, 0xFA, 0xFA),     // bg
            new Color(0xFF, 0xFF, 0xFF),     // surface
            new Color(0xF5, 0xF6, 0xF8),     // surfaceElevee
            new Color(0xE5, 0xE7, 0xEB),     // bordure
            new Color(0xD1, 0xD5, 0xDB),     // bordureForte
            new Color(0x0B, 0x0D, 0x10),     // textePrimaire
            new Color(0x4B, 0x55, 0x63),     // texteSecondaire
            new Color(0x9C, 0xA3, 0xAF),     // texteAtténué
            new Color(0x5E, 0x6A, 0xD2),     // accent
            new Color(0x4F, 0x5A, 0xC2),     // accentHover
            new Color(0x2E, 0x8B, 0x57),     // succes
            new Color(0xD9, 0x8E, 0x0E),     // avertissement
            new Color(0xDC, 0x26, 0x26),     // danger
            new Color(0x2B, 0x6C, 0xB0),     // info
            new Color(0x5E, 0x6A, 0xD2),     // focus
            new Color(0xE8, 0xEA, 0xFD),     // selectionBg
            new Color(0x0B, 0x0D, 0x10),     // selectionFg
            new Color(0, 0, 0, 25)           // ombre
        );
    }

    /** Detecte si c'est un theme sombre. */
    public boolean estSombre() {
        return "sombre".equals(nom);
    }
}
