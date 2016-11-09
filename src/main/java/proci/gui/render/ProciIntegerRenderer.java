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
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author simeo
 */
public class ProciIntegerRenderer implements TableCellRenderer {
    private final JTextField frend = new JTextField();
    private final Color COL_FBACK_IN = new Color(150,255,125);
    private final Color COL_FBACK_SEL = new Color(198,215,239);
    private final Color COL_FBACK_OUT = Color.WHITE;
    public ProciIntegerRenderer() {
        frend.setBorder(BorderFactory.createEmptyBorder());
        frend.setHorizontalAlignment(JTextField.TRAILING);
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        frend.setText("" +  value);
        if (hasFocus) {
            frend.setBackground(COL_FBACK_IN);
            return frend;
        } else {
            if (isSelected) {
                frend.setBackground(COL_FBACK_SEL);
            } else {
                frend.setBackground(COL_FBACK_OUT);
            }
            return frend;
        }
    }
}