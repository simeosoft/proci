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

package proci.eventi;
import java.awt.Color;
import java.awt.Component;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import proci.App;
import proci.EDirectories;

/**
 *
 * @author simeo
 */
public class GruppiTreeCellRenderer extends DefaultTreeCellRenderer {
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,boolean sel,
                                                boolean expanded, boolean leaf, int row,
                                                boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof Gruppo) {
            Gruppo gru = (Gruppo) node.getUserObject();
            ImageIcon ienabled = new ImageIcon(App.getInstance().getAppPath(EDirectories.IMAGES_COMMON) + "gruppo.png");
            ImageIcon edisabled = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)ienabled).getImage()));
            setIcon(ienabled);
            setDisabledIcon(edisabled);
            if (gru.isNew()) {
                setForeground(Color.BLUE);
            }
        }
        return this;
    }
}    
