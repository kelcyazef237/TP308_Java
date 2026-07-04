package cm.portdouala.interfaces;

/**
 * Contrat que tout simulateur du projet doit respecter.
 * Définit le cycle de vie : démarrer → pause → reprendre → arrêter.
 *
 * ⚡ INTERFACE partagée avec l'équipe IHM pour contrôler
 *    les boutons Démarrer/Arrêter de la fenêtre principale.
 */
public interface ISimulateur {

    /** Démarre le simulateur (lance le(s) thread(s)) */
    void demarrer();

    /** Arrête proprement le simulateur (pas de kill brutal) */
    void arreter();

    /** Met en pause sans détruire le thread */
    void mettreEnPause();

    /** Reprend après une pause */
    void reprendre();

    /** @return true si le simulateur tourne actuellement */
    boolean estActif();

    /** @return true si le simulateur est en pause */
    boolean estEnPause();
}