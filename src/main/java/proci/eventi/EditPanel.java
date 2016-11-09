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

import com.simeosoft.form.FormController;
import com.simeosoft.form.IFormListener;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import proci.App;
import proci.EDirectories;
import proci.gui.render.ProciBooleanRenderer;
import proci.gui.render.ProciIntegerRenderer;
import proci.gui.render.ProciStringRenderer;

/**
 *
 * @author  simeo
 */
public class EditPanel extends javax.swing.JPanel implements TableModelListener,IFormListener, ChangeListener {
    
    private Gruppo gruppo;
    private GestioneEvento ge;
    private Connection conn;
    private ResultSet rs;
    private Statement stmt;
    private PreparedStatement ps;    
    private PartecipantiEditTableModel petm;
    private Partecipante responsabile = null;
    private AutomezziEditTableModel aetm;
    private int idGruppo = 1;    
    
    private FormController fc;
    
    private static int currentTab = 0;

    private final App app = App.getInstance();
    
    /** Creates new form EditPanel */
    public EditPanel(Gruppo gruppo,GestioneEvento ge,Connection conn) {
        initComponents();
        this.gruppo = gruppo;
        this.ge = ge;
        this.conn = conn;
        jTabbedPane1.setIconAt(0,new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "user.png"));
        jTabbedPane1.setIconAt(1,new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "auto.png"));
        jbSalva.setEnabled(false);
        jbAnnulla.setEnabled(false);
        fc = new FormController(this,jlMessage);
        fc.addTextField(jtDescr,"Descrizione",50,false);
        fc.addTextField(jtCognome,"Cognome",50,false);
        fc.addTextField(jtNome,"Nome",50,false);
        fc.addTextField(jtCell,"Cellulare",50,false);
        fc.addTextField(jtSpec,"Specializzazione",50,false);
        fc.addTextField(jtNote,"Note",100,false);        
        jTabbedPane1.setSelectedIndex(currentTab);
        jTabbedPane1.addChangeListener(this);        
        loadForm();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jtDescr.requestFocus();
            }
        });
    }

    
    public void loadForm() {
        if (gruppo.isNew()) {
            fc.clear();
            try {
                String sql = "select max(GRUID) as MAXGROUP from GRUPPO";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    idGruppo = rs.getInt("MAXGROUP");
                    idGruppo++;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
            }
            responsabile = new Partecipante(1,idGruppo);
        } else {
            // responsabile (partecipante codice 1)
            try {
                String sql = "select * from PARTECIPANTE where PARGRUPPO = " + gruppo.getId() +
                        " and PARID = 1";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    responsabile = new Partecipante(rs); 
                    fc.setString(jtCognome,responsabile.getCognome());
                    fc.setString(jtNome,responsabile.getNome());
                    fc.setString(jtCell,responsabile.getCellulare());
                    fc.setString(jtSpec,responsabile.getSpecializzazione());
                    fc.setString(jtNote,responsabile.getNote());                    
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
            }
            fc.setString(jtDescr,gruppo.getDescr());
        }
        // partecipanti
        petm = new PartecipantiEditTableModel(conn,gruppo.getId());
        jtaEditParte = new JTable(petm) {
            private final KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if(currentEvent instanceof KeyEvent){
                    KeyEvent ke = (KeyEvent)currentEvent;
                    if(ke.getSource()!=this) {
                        return;
                    }
                    if(rowIndex==0 && columnIndex==0 && KeyStroke.getKeyStrokeForEvent(ke).equals(tabKeyStroke)) {
                        petm.addItem();
                        rowIndex = getRowCount()-1;
                    }
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };
        jtaEditParte.setDefaultRenderer(String.class, new ProciStringRenderer());
        jtaEditParte.setDefaultRenderer(Integer.class, new ProciIntegerRenderer());
        jtaEditParte.setDefaultRenderer(Boolean.class, new ProciBooleanRenderer());
        jScrollPane2.setViewportView(jtaEditParte);
        petm.addTableModelListener(this);
        if (petm.getRowCount() > 0) {
            jtaEditParte.changeSelection(0, 0, false, false);
        }
        // elimina palloso stopCellEditing
        jtaEditParte.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        // automezzi
        aetm = new AutomezziEditTableModel(conn,gruppo.getId());
        jtaEditAuto = new JTable(aetm) {
            private final KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if(currentEvent instanceof KeyEvent){
                    KeyEvent ke = (KeyEvent)currentEvent;
                    if(ke.getSource()!=this) {
                        return;
                    }
                    if(rowIndex==0 && columnIndex==0 && KeyStroke.getKeyStrokeForEvent(ke).equals(tabKeyStroke)) {
                        aetm.addItem();
                        rowIndex = getRowCount()-1;
                    }
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };
        jtaEditAuto.setDefaultRenderer(String.class, new ProciStringRenderer());
        jScrollPane1.setViewportView(jtaEditAuto);
        aetm.addTableModelListener(this);
        if (aetm.getRowCount() > 0) {
            jtaEditAuto.changeSelection(0, 0, false, false);
        }
        // elimina palloso stopCellEditing
        jtaEditAuto.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }
    public void newPar() {
        petm.addItem();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jtaEditParte.changeSelection(petm.getRowCount() - 1, 1, false, false);
            }
        });
    }
    
    private void deletePar() {
        if (jtaEditParte.getSelectedRow() != -1) {
            int[] values = jtaEditParte.getSelectedRows();
            for (int i=values.length - 1; i>=0 ;i--) {
                petm.deleteItem(values[i]);
            }
        }
    }    

    private void azzeraPar() {
        if (petm.getRowCount() > 0) {
            if (JOptionPane.showConfirmDialog(this,"Vuoi eliminare tutti i partecipanti?",
                    "Conferma",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            petm.deleteAllItems();
            setChanged(true);
        }
    }
    public void newAuto() {
        aetm.addItem();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jtaEditAuto.changeSelection(aetm.getRowCount() - 1, 1, false, false);
            }
        });
    }
    
    private void deleteAuto() {
        if (jtaEditAuto.getSelectedRow() != -1) {
            int[] values = jtaEditAuto.getSelectedRows();
            for (int i=values.length - 1; i>=0 ;i--) {
                aetm.deleteItem(values[i]);
            }
        }
    }    

    private void azzeraAuto() {
        if (aetm.getRowCount() > 0) {
            if (JOptionPane.showConfirmDialog(this,"Vuoi eliminare tutti gli automezzi?",
                    "Conferma",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            aetm.deleteAllItems();
            setChanged(true);
        }
    }
            
    private void annulla() {
        loadForm();
        setChanged(false);
    }
    
    @Override
    public void changed(JComponent comp) {
        setChanged(true);
    }
    
    private void setChanged(boolean changed) {
        ge.setChanged(changed);
        jbSalva.setEnabled(changed);
        jbAnnulla.setEnabled(changed);
    }
    
    @Override
    public void tableChanged(javax.swing.event.TableModelEvent e) {
        setChanged(true);
    }   
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        currentTab = jTabbedPane1.getSelectedIndex();
    }
    
    private void salva() {
        // HACK
        if (responsabile == null) {
            responsabile = new Partecipante(1,idGruppo);
        }
        responsabile.setCognome(fc.getString(jtCognome));
        responsabile.setNome(fc.getString(jtNome));
        responsabile.setCellulare(fc.getString(jtCell));
        responsabile.setSpecializzazione(fc.getString(jtSpec));
        responsabile.setNote(fc.getString(jtNote));
        if (gruppo.isNew()) {
            gruppo.setId(idGruppo);
            gruppo.setNew(false);
        }
        gruppo.setDescr(fc.getString(jtDescr));
        try {
            conn.setAutoCommit(false);
            // elimina tutti i partecipanti del gruppo
            String sql = "delete from PARTECIPANTE where PARGRUPPO = " + gruppo.getId();
            stmt = conn.createStatement();
            stmt.execute(sql);
            // reinserisce i partecipanti
            sql = "insert into PARTECIPANTE values (?,?,?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            // responsabile
            ps.setInt(1, gruppo.getId());
            ps.setInt(2, responsabile.getId());
            ps.setString(3, responsabile.getCognome());
            ps.setString(4, responsabile.getNome());
            ps.setString(5, responsabile.getCellulare());
            ps.setString(6, responsabile.getSpecializzazione());
            ps.setString(7, responsabile.getNote());
            ps.setTimestamp(8, responsabile.getSyncdate());
            ps.setString(9, responsabile.getSyncip());
            ps.setString(10, responsabile.isTesseraStampata() ? "Y" : "N");
            ps.setString(11, responsabile.isAttestatoStampato() ? "Y" : "N");
            ps.execute();
            // altri partecipanti
            for (Partecipante par : petm.getValues()) {
                ps.setInt(1, gruppo.getId());
                ps.setInt(2, par.getId());
                ps.setString(3, par.getCognome());
                ps.setString(4, par.getNome());
                ps.setString(5, par.getCellulare());
                ps.setString(6, par.getSpecializzazione());
                ps.setString(7, par.getNote());
                ps.setTimestamp(8, par.getSyncdate());
                ps.setString(9, par.getSyncip());
                ps.setString(10, par.isTesseraStampata() ? "Y" : "N");
                ps.setString(11, par.isAttestatoStampato() ? "Y" : "N");
                ps.execute();
            }
            // elimina tutti gli automezzi del gruppo
            sql = "delete from AUTOMEZZO where AUTGRUPPO = " + gruppo.getId();
            stmt = conn.createStatement();
            stmt.execute(sql);
            // reinserisce gli automezzi
            sql = "insert into AUTOMEZZO values (?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            for (Automezzo aut : aetm.getValues()) {
                ps.setInt(1, gruppo.getId());
                ps.setInt(2, aut.getId());
                ps.setString(3, aut.getDescrizione());
                ps.setString(4, aut.getTarga());
                ps.setString(5, aut.getResponsabile());
                ps.setString(6, aut.getNote());
                ps.execute();
            }
            // aggiorna gruppo
            sql = "delete from GRUPPO where GRUID = " + gruppo.getId();
            stmt = conn.createStatement();
            stmt.execute(sql);
            sql = "insert into GRUPPO values (?,?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, gruppo.getId());
            ps.setString(2, gruppo.getDescr());
            ps.execute();
            conn.commit();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }         
        setChanged(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jtDescr = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jtCognome = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jtNome = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        jtCell = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        jtSpec = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        jtNote = new javax.swing.JFormattedTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jbNewPar = new javax.swing.JButton();
        jbDeletePar = new javax.swing.JButton();
        jbAzzeraPar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtaEditParte = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jbNewAuto = new javax.swing.JButton();
        jbDeleteAuto = new javax.swing.JButton();
        jbAzzeraAuto = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtaEditAuto = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jlMessage = new javax.swing.JLabel();
        jbSalva = new javax.swing.JButton();
        jbAnnulla = new javax.swing.JButton();

        jLabel1.setText("gruppo:");

        jLabel2.setText("responsabile:");

        jLabel3.setText("cognome:");

        jLabel4.setText("nome:");

        jLabel5.setText("cellulare:");

        jLabel6.setText("spec.:");

        jLabel7.setText("note:");

        jbNewPar.setText("Nuovo");
        jbNewPar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbNewParActionPerformed(evt);
            }
        });

        jbDeletePar.setText("Elimina");
        jbDeletePar.setPreferredSize(new java.awt.Dimension(74, 25));
        jbDeletePar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteParActionPerformed(evt);
            }
        });

        jbAzzeraPar.setText("Azzera");
        jbAzzeraPar.setPreferredSize(new java.awt.Dimension(74, 25));
        jbAzzeraPar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAzzeraParActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(null);
        jtaEditParte.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jtaEditParte);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jbNewPar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jbAzzeraPar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jbDeletePar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jbNewPar)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbDeletePar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbAzzeraPar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(334, Short.MAX_VALUE))
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
        );
        jTabbedPane1.addTab("partecipanti", jPanel2);

        jbNewAuto.setText("Nuovo");
        jbNewAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbNewAutoActionPerformed(evt);
            }
        });

        jbDeleteAuto.setText("Elimina");
        jbDeleteAuto.setPreferredSize(new java.awt.Dimension(74, 25));
        jbDeleteAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteAutoActionPerformed(evt);
            }
        });

        jbAzzeraAuto.setText("Azzera");
        jbAzzeraAuto.setPreferredSize(new java.awt.Dimension(74, 25));
        jbAzzeraAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAzzeraAutoActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jtaEditAuto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jtaEditAuto);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jbNewAuto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .add(jbAzzeraAuto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .add(jbDeleteAuto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jbNewAuto)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbDeleteAuto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbAzzeraAuto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(334, Short.MAX_VALUE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
        );
        jTabbedPane1.addTab("automezzi", jPanel1);

        jlMessage.setForeground(new java.awt.Color(255, 0, 0));

        jbSalva.setText("salva");
        jbSalva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSalvaActionPerformed(evt);
            }
        });

        jbAnnulla.setText("annulla");
        jbAnnulla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAnnullaActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jlMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbSalva)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbAnnulla)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jbAnnulla)
                    .add(jbSalva)
                    .add(jlMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jLabel5)
                    .add(jLabel7)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jtDescr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jtCell, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                            .add(jtCognome, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jtSpec)
                            .add(jtNome, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)))
                    .add(jtNote, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE))
                .addContainerGap())
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jtDescr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel3))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jtNome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel4)
                        .add(jtCognome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jtCell, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6)
                    .add(jtSpec, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jtNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jbAnnullaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAnnullaActionPerformed
        annulla();
    }//GEN-LAST:event_jbAnnullaActionPerformed

    private void jbAzzeraParActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAzzeraParActionPerformed
        azzeraPar();
    }//GEN-LAST:event_jbAzzeraParActionPerformed

    private void jbDeleteParActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteParActionPerformed
        deletePar();
    }//GEN-LAST:event_jbDeleteParActionPerformed

    private void jbNewParActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbNewParActionPerformed
        newPar();
    }//GEN-LAST:event_jbNewParActionPerformed

    private void jbSalvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSalvaActionPerformed
        salva();
    }//GEN-LAST:event_jbSalvaActionPerformed

    private void jbAzzeraAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAzzeraAutoActionPerformed
        azzeraAuto();
    }//GEN-LAST:event_jbAzzeraAutoActionPerformed

    private void jbDeleteAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteAutoActionPerformed
        deleteAuto();
    }//GEN-LAST:event_jbDeleteAutoActionPerformed

    private void jbNewAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbNewAutoActionPerformed
        newAuto();
    }//GEN-LAST:event_jbNewAutoActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton jbAnnulla;
    private javax.swing.JButton jbAzzeraAuto;
    private javax.swing.JButton jbAzzeraPar;
    private javax.swing.JButton jbDeleteAuto;
    private javax.swing.JButton jbDeletePar;
    private javax.swing.JButton jbNewAuto;
    private javax.swing.JButton jbNewPar;
    private javax.swing.JButton jbSalva;
    private javax.swing.JLabel jlMessage;
    private javax.swing.JFormattedTextField jtCell;
    private javax.swing.JFormattedTextField jtCognome;
    private javax.swing.JFormattedTextField jtDescr;
    private javax.swing.JFormattedTextField jtNome;
    private javax.swing.JFormattedTextField jtNote;
    private javax.swing.JFormattedTextField jtSpec;
    private javax.swing.JTable jtaEditAuto;
    private javax.swing.JTable jtaEditParte;
    // End of variables declaration//GEN-END:variables
    
}
