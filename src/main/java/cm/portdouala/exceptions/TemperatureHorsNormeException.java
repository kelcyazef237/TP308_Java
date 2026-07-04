package cm.portdouala.exceptions;

import cm.portdouala.metier.ConteneurRefrigere;

public class TemperatureHorsNormeException extends Exception {

    private final String idConteneur;
    private final double temperatureActuelle;
    private final double temperatureConsigne;
    private final double ecart;

    public TemperatureHorsNormeException(ConteneurRefrigere conteneur) {
        super(String.format(
                "ALERTE [%s] : %.1f°C (consigne %.1f°C, écart %+.1f°C)",
                conteneur.getIdentifiant(),
                conteneur.getTemperatureActuelle(),
                conteneur.getTemperatureConsigne(),
                conteneur.getEcartTemperature()
        ));
        this.idConteneur         = conteneur.getIdentifiant();
        this.temperatureActuelle = conteneur.getTemperatureActuelle();
        this.temperatureConsigne = conteneur.getTemperatureConsigne();
        this.ecart               = conteneur.getEcartTemperature();
    }

    public String getIdConteneur()         { return idConteneur; }
    public double getTemperatureActuelle() { return temperatureActuelle; }
    public double getTemperatureConsigne() { return temperatureConsigne; }
    public double getEcart()               { return ecart; }
}