/*
    Gestione Soci Protezione Civile
    Copyright (C) Simeosoft di Carlo Simeone
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package proci.gui.render;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author simeo
 */
public class ProciBooleanRenderer implements TableCellRenderer {
    private final JPanel panel = new JPanel();
    private final JCheckBox cb = new JCheckBox();
    private final Color COL_FBACK_IN = new Color(150,255,125);
    private final Color COL_FBACK_SEL = new Color(198,215,239);
    private final Color COL_FBACK_OUT = Color.WHITE;
    public ProciBooleanRenderer() {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
        panel.add(cb);
        panel.setBorder(BorderFactory.createEmptyBorder());
        cb.setBorder(BorderFactory.createEmptyBorder());
        cb.setMargin(new Insets(0,0,0,0));
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        cb.setSelected(((Boolean) value));
        if (hasFocus) {
            panel.setBackground(COL_FBACK_IN);
        } else {
            if (isSelected) {
                panel.setBackground(COL_FBACK_SEL);
            } else {
                panel.setBackground(COL_FBACK_OUT);
            }
        }
        return panel;
    }
}