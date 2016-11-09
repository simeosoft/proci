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
package proci.gui.editor;

import java.awt.Component;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simeo
 */
public class ProciDecimalEditor extends DefaultCellEditor {

    private static final long serialVersionUID = -5952196269895329535L;
    private static final Logger logger = LoggerFactory.getLogger(ProciDecimalEditor.class);
    private DecimalFormat df;

    public ProciDecimalEditor(JTextField textField) {
        super(textField);
        df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        df.setMinimumFractionDigits(3);
        df.setGroupingUsed(false);
        df.setParseBigDecimal(true);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        logger.error("DBG: RETURNING COMPONNT: {}", editorComponent.hashCode());
        if (value != null && value instanceof BigDecimal) {
            ((JTextField) editorComponent).setText(df.format(value));
        } else {
            ((JTextField) editorComponent).setText("0.000");
        }
        return ((JTextField) editorComponent);
    }

    @Override
    public Object getCellEditorValue() {
        String textvalue = "0";
        if (((JTextField) editorComponent).getText() == null) {
            logger.error("DBG: RET ZERO:");
            return BigDecimal.ZERO;
        }
        BigDecimal ret;
        textvalue = ((JTextField) editorComponent).getText();
        logger.error("DBG: TEXTVALUE: {}", textvalue);
        try {
            ret = (BigDecimal) df.parse(textvalue);
        } catch (ParseException ex) {
            logger.trace("PARSE EX: {}", ex);
            return BigDecimal.ZERO;
        }
        logger.error("DBG: RET BD VALUE: {}", ret);
        return ret;
    }

//    
//    /**
//     * Called when the value editated in the filed is correct.
//     */
//    @Override
//    public Object getCellEditorValue() {
//        if (jt.getText() == null || jt.getText().length() == 0) {
//            return BigDecimal.ZERO;
//        }
//        return new BigDecimal(jt.getText());
//    }
//    
//    @Override
//    public boolean shouldSelectCell(EventObject anEvent) {
//        return true;
//    }
//       
//    /**
//     * Called when the editing is complete: check the value in the cell and 
//     * return true if it is correct.
//     */
//    @Override
//    public boolean stopCellEditing() {
//        try {
//            BigDecimal b;
//            if (jt.getText() == null || jt.getText().length() == 0) {
//                b = new BigDecimal(0);
//            } else {
//                b = new BigDecimal(jt.getText());
//            }            
//            fc.check();
//        } catch (FieldException fe) { 
//            if (warning) {
//                JOptionPane.showMessageDialog(jtable,fe.getMessage(),"Input Warning",JOptionPane.WARNING_MESSAGE);
//            } else {
//                final String msg = fe.getMessage();
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        jlMessage.setText(msg);
//                    }
//                });
//                return false;
//            }
//        } catch(NumberFormatException e) {            
//            final String msg = "formato non valido";
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    jlMessage.setText(msg);
//                }
//            });
//            return false;
//        }
//        // 
//        CellEditorListener listener;
//        Object[] listeners = listenerList.getListenerList();
//        for (int i = 0; i < listeners.length; i++) {
//            if (listeners[i] == CellEditorListener.class) {
//                listener = (CellEditorListener) listeners[i + 1];
//                listener.editingStopped(changeEvent);
//            }
//        }
//        return true;
//    }
//    
//    @Override
//    public void cancelCellEditing() {
//        fireEditingCanceled();
//    }
//    
//    @Override
//    public void addCellEditorListener(CellEditorListener l) {
//        listenerList.add(CellEditorListener.class, l);
//    }
//    
//    @Override
//    public void removeCellEditorListener(CellEditorListener l) {
//        listenerList.remove(CellEditorListener.class, l);
//    }
//    
//    @Override
//    protected void fireEditingCanceled() {
//        CellEditorListener listener;
//        Object[] listeners = listenerList.getListenerList();
//        for (int i = 0; i < listeners.length; i++) {
//            if (listeners[i] == CellEditorListener.class) {
//                listener = (CellEditorListener) listeners[i + 1];
//                listener.editingCanceled(changeEvent);
//            }
//        }
//    }
}
