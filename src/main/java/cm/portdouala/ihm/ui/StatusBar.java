package cm.portdouala.ihm.ui;

import fr.tp308.ihm.util.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class StatusBar extends JPanel {

    private final JLabel messageLabel;

    public StatusBar() {
        this.messageLabel = new JLabel("Prêt");
        messageLabel.setFont(AppTheme.STATUS_FONT);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_COLOR));
        setBackground(AppTheme.STATUS_BAR_BACKGROUND);
        add(messageLabel, BorderLayout.WEST);
    }

    public void showProgress(String message, int itemCount) {
        messageLabel.setText(String.format("%s - %d conteneur(s)", message, itemCount));
    }
}
