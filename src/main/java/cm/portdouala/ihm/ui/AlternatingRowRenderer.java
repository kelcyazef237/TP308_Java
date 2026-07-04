package cm.portdouala.ihm.ui;

import fr.tp308.ihm.util.AppTheme;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class AlternatingRowRenderer extends DefaultTableCellRenderer {

    public AlternatingRowRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            component.setBackground(AppTheme.TABLE_SELECTION_BACKGROUND);
            component.setForeground(table.getForeground());
        } else {
            component.setBackground(row % 2 == 0 ? AppTheme.TABLE_ROW_EVEN : AppTheme.TABLE_ROW_ODD);
            component.setForeground(table.getForeground());
        }
        if (component instanceof JLabel) {
            ((JLabel) component).setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        }
        return component;
    }
}
