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
package proci.gui;

import proci.gui.render.ProciStringRenderer;
import proci.gui.render.ProciIntegerRenderer;
import com.simeosoft.form.AddFieldException;
import com.simeosoft.form.FieldException;
import com.simeosoft.form.FormController;
import com.simeosoft.form.IFormListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import proci.DBHandler;
import proci.ETipoSocio;
import proci.App;
import proci.EDirectories;
import proci.model.DotQuant;
import proci.model.Dotazione;
import proci.model.Esercitazione;
import proci.model.Intervento;
import proci.model.Specializzazione;
import proci.model.TreeItem;
import proci.model.TreeItem.TreeItemType;
import proci.util.ProciUtils;


/**
 * Classe pannello task SocioPanel
 */
public class SocioPanel extends javax.swing.JPanel implements IFormListener,ListDataListener,
        ChangeListener,ItemListener,TableModelListener {
    
    private static int currentTab = 0;
    private static final long serialVersionUID = -4617719045637514470L;
    private JLabel message_label = null;
    private EventListener el = null;
    private boolean changed = false;
    private boolean inserting = false;
    File imageFile = null;          // se diverso da null devo riaggiornare il file
    String oldImageName = null;
    String imageName = null;
    private boolean eliminaFoto = false;
    
    private GestioneSoci gs;
    private TreeItem ti;
    private FormController fc;
    private int idSocio;
   
    private Connection conn = null;
    private Statement stmt = null;
    private PreparedStatement ps = null;    
    private ResultSet rs = null;    
    private DotSocioTableModel tmDotSocio = null;
    private DefaultListModel lmDotDispo = new DefaultListModel();
    private DefaultListModel lmIntSocio = new DefaultListModel();
    private DefaultListModel lmIntDispo = new DefaultListModel();
    private DefaultListModel lmEseSocio = new DefaultListModel();
    private DefaultListModel lmEseDispo = new DefaultListModel();
    private DefaultListModel lmSpeSocio = new DefaultListModel();
    private DefaultListModel lmSpeDispo = new DefaultListModel();

    private final App app = App.getInstance();
    
    /** Creates new form JcSocioGuiPanel */
    public SocioPanel(TreeItem ti,GestioneSoci gs,JLabel message_label) {
        initComponents();
        this.ti = ti;
        if (ti == null) {
            idSocio = 0;
            inserting = true;
            currentTab = 0;
        } else {
            idSocio = ti.getId();
        }
        this.gs = gs;
        this.message_label = message_label;
        fc = new FormController(this,message_label);
        this.el = el;
        //
        try {
            fc.addTextField(jtSOCOGNOME,"Cognome",50,false);
            fc.addTextField(jtSONOME,"Nome",50,false);
            fc.addTextField(jtSOCONAS,"Comune di nascita",50,false);
            fc.addDateField(jtSODATANAS,"Data nascita",false);
            fc.addTextField(jtSOINDI,"Indirizzo",255,false);
            fc.addTextField(jtSOREDI,"Residenza",50,false);
            fc.addTextField(jtSOCAP,"CAP",6,false);
            fc.addTextField(jtSOTELABI,"Telefono",30,false);
            fc.addTextField(jtSOCODFISC,"Codice fiscale",50,false);
            fc.addTextField(jtSOTELCELL,"Cellulare",30,false);
            fc.addTextField(jtSOEMAIL,"E-mail",100,false);
            fc.addIntegerField(jtSOANNOTESS,"Anno tessera",4,false,false);
            fc.addIntegerField(jtSONUMTESS,"Numero tessera",4,false,false);
            fc.addTextField(jtSOPATENTE,"Patente",50,false);
            fc.addTextField(jtSOPROFESSIONE,"Professione",50,false);
            fc.addTextField(jtSOGRUPPO,"Gruppo sanguigno",20,false);
            fc.addTextField(jtSOSERVIZIO,"Servizio Militare",50,false);
            fc.addTextField(jtSOALTRE,"Altre associazioni",100,false);
            fc.addTextField(jtaSONOTE,"Note",255,false);
        } catch (AddFieldException afe) {
            JOptionPane.showMessageDialog(this,"ECCEZIONE: " + afe,"ERRORE",
                    JOptionPane.ERROR_MESSAGE);
        }
        DefaultComboBoxModel cbm = new DefaultComboBoxModel(ETipoSocio.values());
        jcoSOTIPO.setModel(cbm);
        jrM.setSelected(true);
        try {
            conn = DBHandler.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE conn!",JOptionPane.ERROR_MESSAGE);
        }
        
        jlDotDispo.setModel(lmDotDispo);        
        jlIntSocio.setModel(lmIntSocio);
        jlIntDispo.setModel(lmIntDispo);
        lmIntSocio.addListDataListener(this);
        jlEseSocio.setModel(lmEseSocio);
        jlEseDispo.setModel(lmEseDispo);
        lmEseSocio.addListDataListener(this);
        jlSpeSocio.setModel(lmSpeSocio);
        jlSpeDispo.setModel(lmSpeDispo);
        lmSpeSocio.addListDataListener(this);
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                jtSOCOGNOME.requestFocus();
                jtSOCOGNOME.setCaretPosition(0);
            }
        });
        
        jTabbedPane1.setSelectedIndex(currentTab);
        jTabbedPane1.addChangeListener(this);
        loadSocio();
        
        jcoSOTIPO.addItemListener(this);
        jrM.addItemListener(this);
        jrF.addItemListener(this);   
          
    }
    
    private void loadSocio() {
        try {
            if (inserting) {             // NUOVO SOCIO
                stmt = conn.createStatement();
                rs = stmt.executeQuery("select MAX(SOID) from SOCIO");
                if (! rs.next()) {
                    idSocio = 1;
                } else {
                    idSocio = rs.getInt(1) + 1;
                }
                jbCancel.setEnabled(true);  // permette di annullare l'inserimento nuovo socio                
            } else {                
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery("select * from SOCIO where SOID = " + idSocio);
                if (! rs.next()) {
                    throw new SQLException("Socio non trovato ???");
                }
            }
            if (inserting) {
                jlOutSOID.setText(idSocio + "");            
                fc.clear();
            } else {
                jlOutSOID.setText(rs.getInt("SOID") + "");
                fc.setString(jtSOCOGNOME,rs.getString("SOCOGNOME"));
                fc.setString(jtSONOME,rs.getString("SONOME"));
                fc.setString(jtSOCONAS,rs.getString("SOCONAS"));
                jrM.setSelected(false);
                jrF.setSelected(false);
                if (rs.getString("SOSESSO").equalsIgnoreCase("M")) {
                    jrM.setSelected(true);
                } else {
                    jrF.setSelected(true);
                }
                if (rs.getDate("SODATANAS") == null) {
                    fc.setDate(jtSODATANAS,"");
                } else {
                    fc.setDate(jtSODATANAS,rs.getDate("SODATANAS"));
                }
                fc.setString(jtSOINDI,rs.getString("SOINDI"));
                fc.setString(jtSOREDI,rs.getString("SORESI"));
                fc.setString(jtSOCAP,rs.getString("SOCAP"));
                fc.setString(jtSOTELABI,rs.getString("SOTELABI"));
                fc.setString(jtSOCODFISC,rs.getString("SOCODFISC"));
                fc.setString(jtSOTELCELL,rs.getString("SOTELCELL"));
                fc.setString(jtSOEMAIL,rs.getString("SOEMAIL"));
                fc.setInt(jtSOANNOTESS,rs.getInt("SOANNOTESS"));
                fc.setInt(jtSONUMTESS,rs.getInt("SONUMTESS"));
                fc.setString(jtSOPATENTE,rs.getString("SOPATENTE"));
                fc.setString(jtSOPROFESSIONE,rs.getString("SOPROFESSIONE"));
                if (rs.getString("SOFOTO") != null && rs.getString("SOFOTO").length() > 0) {
                    imageName = rs.getString("SOFOTO");
                    jlSOFOTO.setIcon(new ImageIcon(app.getAppPath(EDirectories.IMAGES_PHOTOS) + imageName));
                    jlPath.setText(imageName);
                    oldImageName = imageName;
                } else {
                    jlSOFOTO.setIcon(new ImageIcon(app.getAppPath(EDirectories.IMAGES_PHOTOS) + "noimage.png"));
                    jlPath.setText("");
                }
                ETipoSocio ets = ETipoSocio.OPERATIVO;
                if (rs.getString("SOTIPO").equalsIgnoreCase("N")) {
                    ets = ETipoSocio.NON_OPERATIVO;
                }
                if (rs.getString("SOTIPO").equalsIgnoreCase("X")) {
                    ets = ETipoSocio.ONORARIO;
                }
                jcoSOTIPO.setSelectedItem(ets);
                fc.setString(jtSOGRUPPO,rs.getString("SOGRUPPO"));
                fc.setString(jtSOSERVIZIO,rs.getString("SOSERVIZIO"));
                fc.setString(jtSOALTRE,rs.getString("SOALTRE"));
                fc.setString(jtaSONOTE,rs.getString("SONOTE"));
            }
            String sql = null;            
            /* BUG HSQLDB right join */
//            if (inserting) {
//                sql = "select NULL as SOINSO,* FROM INTERVENTO order by INTID";
//            } else {
//                sql = "select SOINSO,INTID,INTDESCR FROM SOIN right outer join INTERVENTO on (SOININ=INTID and SOINSO="
//                        + idSocio + ")";
//            }            
            // Dotazioni
            tmDotSocio = new DotSocioTableModel(conn,idSocio);
            tmDotSocio.addTableModelListener(this);
            jtDotSocio = new JTable(tmDotSocio) {
                @Override
                public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                    super.changeSelection(rowIndex, 3, toggle, extend);
                }
            };        
            jScrollPane1.setViewportView(jtDotSocio);
            jtDotSocio.setDefaultRenderer(String.class, new ProciStringRenderer());
            jtDotSocio.setDefaultRenderer(Integer.class, new ProciIntegerRenderer());
            if (tmDotSocio.getRowCount() > 0) {
                jtDotSocio.changeSelection(0, 3, false, false);
            }
            
            lmDotDispo.removeAllElements();
            jlDotDispo.setPrototypeCellValue("************************");
            sql = String.format("select * from DOTAZIONE where DOTID not in " +
                                "(select SODODO from SODO where sodoso = %d) " +
                                "order by DOTID", idSocio);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Dotazione dot = new Dotazione(rs.getInt("DOTID"),rs.getString("DOTDESCR"),rs.getString("DOTTAGLIA"));                    
                lmDotDispo.addElement(dot);
            }
            // Interventi
            lmIntSocio.removeAllElements();
            lmIntDispo.removeAllElements();
            jlIntDispo.setPrototypeCellValue("************************");
            jlIntSocio.setPrototypeCellValue("************************");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            sql = "select INTID,INTDESCR from SOIN left join INTERVENTO on SOININ=INTID where SOINSO = " 
                    + idSocio;
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Intervento inte = new Intervento(rs.getInt("INTID"),rs.getString("INTDESCR"));
                lmIntSocio.addElement(inte);
            }
            sql = "select * from INTERVENTO where INTID not in (select SOININ from SOIN where SOINSO = " +
                    idSocio + ")";
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Intervento inte = new Intervento(rs.getInt("INTID"),rs.getString("INTDESCR"));
                lmIntDispo.addElement(inte);
            }
            // Esercitazioni
            lmEseSocio.removeAllElements();
            lmEseDispo.removeAllElements();
            jlEseDispo.setPrototypeCellValue("************************");
            jlEseSocio.setPrototypeCellValue("************************");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            sql = "select ESEID,ESEDESCR from SOES left join ESERCITAZIONE on SOESES=ESEID where SOESSO = " 
                    + idSocio;
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Esercitazione ese = new Esercitazione(rs.getInt("ESEID"),rs.getString("ESEDESCR"));
                lmEseSocio.addElement(ese);
            }
            sql = "select * from ESERCITAZIONE where ESEID not in (select SOESES from SOES where SOESSO = " +
                    idSocio + ")";
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Esercitazione ese = new Esercitazione(rs.getInt("ESEID"),rs.getString("ESEDESCR"));
                lmEseDispo.addElement(ese);
            }
            // Specializzazioni
            lmSpeSocio.removeAllElements();
            lmSpeDispo.removeAllElements();
            jlSpeDispo.setPrototypeCellValue("************************");
            jlSpeSocio.setPrototypeCellValue("************************");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            sql = "select SPEID,SPEDESCR from SOSP left join SPECIALIZZAZIONE on SOSPSP=SPEID where SOSPSO = " 
                    + idSocio;
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Specializzazione spe = new Specializzazione(rs.getInt("SPEID"),rs.getString("SPEDESCR"));
                lmSpeSocio.addElement(spe);
            }
            sql = "select * from SPECIALIZZAZIONE where SPEID not in (select SOSPSP from SOSP where SOSPSO = " +
                    idSocio + ")";
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Specializzazione spe = new Specializzazione(rs.getInt("SPEID"),rs.getString("SPEDESCR"));
                lmSpeDispo.addElement(spe);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE load socio!",JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void tableChanged(javax.swing.event.TableModelEvent e) {
        changed(null);
    }    
    
    @Override
    public void itemStateChanged(ItemEvent e) {
        changed((JComponent) e.getSource());
    }
    
    @Override
    public void contentsChanged(ListDataEvent e) {
        changed((JComponent) e.getSource());
    }
    @Override
    public void changed(JComponent jc) {
        jbOk.setEnabled(true);
        jbCancel.setEnabled(true);
        changed = true;
        gs.setEditing(true);
    }
    @Override
    public void intervalRemoved(ListDataEvent e) {
    }
    @Override
    public void intervalAdded(ListDataEvent e) {
    }
    @Override
    public void stateChanged(ChangeEvent ce) {
        currentTab = jTabbedPane1.getSelectedIndex();
    }
    
    private void okActionPerformed() {
        try {
            fc.check();
        } catch (FieldException fe) {
            JOptionPane.showMessageDialog(this,fe.getMessage(),"ERRORE",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (jtDotSocio.isEditing()) {
            jtDotSocio.getCellEditor(jtDotSocio.getEditingRow(), jtDotSocio.getEditingColumn()).stopCellEditing();        
        }
        
        String sesso = jrM.isSelected() ? "M" : "F";
        try {
            if (inserting) {
                ps = conn.prepareStatement("insert into SOCIO values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                ps.setInt(1,idSocio);
            } else {
                ps = conn.prepareStatement("update SOCIO set " +
                        "SOID = ?,SOCOGNOME = ?,SONOME = ?,SOCONAS = ?,SOSESSO = ?,SODATANAS = ?,SOINDI = ?," +
                        "SORESI = ?,SOCAP = ?,SOTELABI = ?,SOCODFISC = ?,SOTELCELL = ?,SOEMAIL = ?,SOANNOTESS = ?," +
                        "SONUMTESS = ?,SOPATENTE = ?,SOPROFESSIONE = ?,SOFOTO = ?,SOTIPO = ?,SOGRUPPO = ?," +
                        "SOSERVIZIO = ?,SOALTRE = ?,SONOTE = ? where SOID = ?");
                ps.setInt(1,idSocio);
            }
            ps.setString(2,fc.getString(jtSOCOGNOME));
            ps.setString(3,fc.getString(jtSONOME));
            ps.setString(4,fc.getString(jtSOCONAS));
            ps.setString(5,sesso);
            ps.setDate(6,fc.getDate(jtSODATANAS));
            ps.setString(7,fc.getString(jtSOINDI));
            ps.setString(8,fc.getString(jtSOREDI));
            ps.setString(9,fc.getString(jtSOCAP));
            ps.setString(10,fc.getString(jtSOTELABI));
            ps.setString(11,fc.getString(jtSOCODFISC));
            ps.setString(12,fc.getString(jtSOTELCELL));
            ps.setString(13,fc.getString(jtSOEMAIL));
            ps.setInt(14,fc.getInt(jtSOANNOTESS));
            ps.setInt(15,fc.getInt(jtSONUMTESS));
            ps.setString(16,fc.getString(jtSOPATENTE));
            ps.setString(17,fc.getString(jtSOPROFESSIONE));
            // foto
            if (inserting) {
                if (imageFile != null) {
                    ProciUtils.copiaFotoSoci(imageFile);
                    ProciUtils.creaMiniatura(imageFile);
                    imageName = imageFile.getName();
                }
            } else {
                if (oldImageName != null && eliminaFoto == true) {
                    File oldImage = new File(app.getAppPath(EDirectories.IMAGES_PHOTOS) + oldImageName);
                    oldImage.delete();
                    oldImage = new File(app.getAppPath(EDirectories.IMAGES_THUMBS) + oldImageName);
                    oldImage.delete();
                    imageName = null;
                }
                if (imageFile != null) {
                    ProciUtils.copiaFotoSoci(imageFile);
                    ProciUtils.creaMiniatura(imageFile);
                    imageName = imageFile.getName();
                }
            }            
            ps.setString(18,imageName);
            // 
            ps.setString(19,((ETipoSocio) jcoSOTIPO.getSelectedItem()).getVal());
            ps.setString(20,fc.getString(jtSOGRUPPO));
            ps.setString(21,fc.getString(jtSOSERVIZIO));
            ps.setString(22,fc.getString(jtSOALTRE));
            ps.setString(23,fc.getString(jtaSONOTE));
            if (! inserting) {
                ps.setInt(24,idSocio);
            }
            conn.setAutoCommit(false);
            ps.execute();
            //
            if (! inserting) {
                ps = conn.prepareStatement("delete from SODO where SODOSO = ?");
                ps.setInt(1,idSocio);
                ps.execute();
                ps = conn.prepareStatement("delete from SOIN where SOINSO = ?");
                ps.setInt(1,idSocio);
                ps.execute();
                ps = conn.prepareStatement("delete from SOES where SOESSO = ?");
                ps.setInt(1,idSocio);
                ps.execute();
                ps = conn.prepareStatement("delete from SOSP where SOSPSO = ?");
                ps.setInt(1,idSocio);
                ps.execute();
            }
            ps = conn.prepareStatement("insert into SODO values(?,?,?)");
            for (DotQuant dq : tmDotSocio.getValues()) {
                ps.setInt(1,idSocio);
                ps.setInt(2,dq.getId());
                ps.setInt(3,dq.getQuant());
                ps.execute();
            }
            Enumeration enume = lmIntSocio.elements();
            ps = conn.prepareStatement("insert into SOIN values(?,?)");
            while (enume.hasMoreElements()) {
                Intervento inte = (Intervento) enume.nextElement();
                ps.setInt(1,idSocio);
                ps.setInt(2,inte.getINTID());
                ps.execute();
            }
            enume = lmEseSocio.elements();
            ps = conn.prepareStatement("insert into SOES values(?,?)");
            while (enume.hasMoreElements()) {
                Esercitazione ese = (Esercitazione) enume.nextElement();
                ps.setInt(1,idSocio);
                ps.setInt(2,ese.getESEID());
                ps.execute();
            }            
            enume = lmSpeSocio.elements();
            ps = conn.prepareStatement("insert into SOSP values(?,?)");
            while (enume.hasMoreElements()) {
                Specializzazione spe = (Specializzazione) enume.nextElement();
                ps.setInt(1,idSocio);
                ps.setInt(2,spe.getSPEID());
                ps.execute();
            }            
            conn.commit();
            if (inserting) {
                TreeItem ti = new TreeItem(TreeItemType.SOCIO, idSocio, 
                    fc.getString(jtSOCOGNOME) + " " + fc.getString(jtSONOME));
                ti.setImgFile(imageName);
                gs.insertSocio(ti);
                // nel caso si rimodifichi il socio dopo un inserimento
                inserting = false;
                this.ti = ti;
            } else {
                // nel caso cambi nome e cognome o immagine
                ti.setDescr(fc.getString(jtSOCOGNOME) + " " + fc.getString(jtSONOME));
                ti.setImgFile(imageName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE updating socio!",JOptionPane.ERROR_MESSAGE);
            try {
                conn.rollback();
            } catch (SQLException ignored) { /* vuota */ }
        }
        jbOk.setEnabled(false);
        jbCancel.setEnabled(false);
        changed = false;
        gs.setEditing(false);
    }
    
    private void cancelActionPerformed() {
        gs.setEditing(false);
        jbOk.setEnabled(false);
        jbCancel.setEnabled(false);
        if (! inserting) {
            loadSocio();
        } else {
            fc.clear();
            jlPath.setText("");
            jlSOFOTO.setIcon(null);
        }
    }
    
    private class DotSocioTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1003182762192664938L;
        final String[] columnNames = { "id","descrizione","taglia","quantità"};
        final Class[] classes = { Integer.class,String.class,String.class,Integer.class};
        HashMap<Integer,Dotazione> hmDot = new HashMap<>();     // per lookup
        ArrayList<DotQuant> alDotQuant = new ArrayList<>();
        
        public DotSocioTableModel(Connection conn, int id) {
            try {
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery("select * from SODO where sodoso = " + id);
                while(rs.next()) {
                    DotQuant dq = new DotQuant(rs.getInt("SODODO"),rs.getInt("SODOQUANT"));
                    alDotQuant.add(dq);
                }                
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery("select * from DOTAZIONE");
                while(rs.next()) {
                    Dotazione dot = new Dotazione(rs.getInt("DOTID"),rs.getString("DOTDESCR"),rs.getString("DOTTAGLIA"));
                    hmDot.put(rs.getInt("DOTID"),dot);
                }    
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE init DotSocioTableModel!",JOptionPane.ERROR_MESSAGE);
            }                
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public Class getColumnClass(int column) {
            return classes[column];
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            DotQuant dq = alDotQuant.get(row);
            Dotazione dot = lookupDotazione(dq.getId());
            switch (column) {
                case 0:
                    return dq.getId();
                case 1:
                    return dot.getDOTDESCR();
                case 2:
                    return dot.getDOTTAGLIA();
                case 3:
                    return dq.getQuant();
            }
            return null;
        }
        
        @Override
        public int getRowCount() {
            return alDotQuant.size();
        }

        @Override
        public void setValueAt(Object val,int r, int c) {
            DotQuant dq = alDotQuant.get(r);
            if (c == 3) {
                dq.setQuant(((Integer) val).intValue());
                fireTableDataChanged();
            }
        }
        
        // cell editing
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 3;
        }
        public ArrayList<DotQuant> getValues() {
            return alDotQuant;
        }
        
        private Dotazione lookupDotazione(int id) {
            if (hmDot.containsKey(id)) {
                return hmDot.get(id);
            } else {
                return new Dotazione(0, "NON TROVATA????", "");
            }
        }
        
        public void add(Dotazione dot) {
            alDotQuant.add(new DotQuant(dot.getDOTID(),1));
            fireTableDataChanged();
        }
        public void remove(int i) {
            alDotQuant.remove(i);
            fireTableDataChanged();
        }
    }

    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jlSOID = new javax.swing.JLabel();
        jlSOCOGNOME = new javax.swing.JLabel();
        jlSONOME = new javax.swing.JLabel();
        jlSOCONAS = new javax.swing.JLabel();
        jlSODATANAS = new javax.swing.JLabel();
        jlSOINDI = new javax.swing.JLabel();
        jlSOREDI = new javax.swing.JLabel();
        jlSOCAP = new javax.swing.JLabel();
        jlOutSOID = new javax.swing.JLabel();
        jlSOTIPO = new javax.swing.JLabel();
        jlSOSESSO = new javax.swing.JLabel();
        jlSOANNOTESS = new javax.swing.JLabel();
        jlSONUMTESS = new javax.swing.JLabel();
        jlSOTELABI = new javax.swing.JLabel();
        jlSOTELCELL = new javax.swing.JLabel();
        jlSOEMAIL = new javax.swing.JLabel();
        jtSOCOGNOME = new javax.swing.JFormattedTextField();
        jtSONOME = new javax.swing.JFormattedTextField();
        jtSOCONAS = new javax.swing.JFormattedTextField();
        jtSODATANAS = new javax.swing.JFormattedTextField();
        jtSOINDI = new javax.swing.JFormattedTextField();
        jtSOREDI = new javax.swing.JFormattedTextField();
        jtSOCAP = new javax.swing.JFormattedTextField();
        jcoSOTIPO = new javax.swing.JComboBox();
        jrM = new javax.swing.JRadioButton();
        jrF = new javax.swing.JRadioButton();
        jtSOANNOTESS = new javax.swing.JFormattedTextField();
        jtSONUMTESS = new javax.swing.JFormattedTextField();
        jtSOTELABI = new javax.swing.JFormattedTextField();
        jtSOTELCELL = new javax.swing.JFormattedTextField();
        jtSOEMAIL = new javax.swing.JFormattedTextField();
        jPanel2 = new javax.swing.JPanel();
        jlSONOTE = new javax.swing.JLabel();
        jtaSONOTE = new javax.swing.JTextArea();
        jPanel11 = new javax.swing.JPanel();
        jlSOCODFISC = new javax.swing.JLabel();
        jtSOCODFISC = new javax.swing.JFormattedTextField();
        jlSOPATENTE = new javax.swing.JLabel();
        jtSOPATENTE = new javax.swing.JFormattedTextField();
        jtSOPROFESSIONE = new javax.swing.JFormattedTextField();
        jtSOGRUPPO = new javax.swing.JFormattedTextField();
        jtSOSERVIZIO = new javax.swing.JFormattedTextField();
        jtSOALTRE = new javax.swing.JFormattedTextField();
        jlSOPROFESSIONE = new javax.swing.JLabel();
        jlSOGRUPPO = new javax.swing.JLabel();
        jlSOSERVIZIO = new javax.swing.JLabel();
        jlSOALTRE = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtDotSocio = new javax.swing.JTable();
        jbAddDotazione = new javax.swing.JButton();
        jbDeleteDotazione = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jlDotDispo = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jlIntSocio = new javax.swing.JList();
        jbAddIntervento = new javax.swing.JButton();
        jbDeleteIntervento = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jlIntDispo = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jlEseSocio = new javax.swing.JList();
        jbAddEsercitazione = new javax.swing.JButton();
        jbDeleteEsercitazione = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jlEseDispo = new javax.swing.JList();
        jPanel10 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jlSpeSocio = new javax.swing.JList();
        jbAddSpecializzazione = new javax.swing.JButton();
        jbDeleteSpecializzazione = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        jlSpeDispo = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jlSOFOTO = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jbCarica = new javax.swing.JButton();
        jbCarica1 = new javax.swing.JButton();
        jlPath = new javax.swing.JLabel();
        jbElimina = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jbOk = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(680, 680));

        jlSOID.setText("ID Socio");

        jlSOCOGNOME.setText("Cognome");

        jlSONOME.setText("Nome");

        jlSOCONAS.setText("Comune di nascita");

        jlSODATANAS.setText("Data nascita");

        jlSOINDI.setText("Indirizzo");

        jlSOREDI.setText("Residenza");

        jlSOCAP.setText("CAP");

        jlOutSOID.setText("jLabel1");

        jlSOTIPO.setText("Tipo socio");

        jlSOSESSO.setText("Sesso");

        jlSOANNOTESS.setText("Anno tessera");

        jlSONUMTESS.setText("Numero tessera");

        jlSOTELABI.setText("Telefono");

        jlSOTELCELL.setText("Cellulare");

        jlSOEMAIL.setText("E-mail");

        jtSOCOGNOME.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jtSOCOGNOME.setText("SOCOGNOME");
        jtSOCOGNOME.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOCOGNOME.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSONOME.setText("SONOME");
        jtSONOME.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSONOME.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOCONAS.setText("SOCONAS");
        jtSOCONAS.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOCONAS.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSODATANAS.setText("SODATANAS");
        jtSODATANAS.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSODATANAS.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOINDI.setText("SOINDI");
        jtSOINDI.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOINDI.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOREDI.setText("SOREDI");
        jtSOREDI.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOREDI.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOCAP.setText("SOCAP");
        jtSOCAP.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOCAP.setPreferredSize(new java.awt.Dimension(100, 19));

        buttonGroup1.add(jrM);
        jrM.setText("M");

        buttonGroup1.add(jrF);
        jrF.setText("F");

        jtSOANNOTESS.setText("SOANNOTESS");
        jtSOANNOTESS.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOANNOTESS.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSONUMTESS.setText("SONUMTESS");
        jtSONUMTESS.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSONUMTESS.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOTELABI.setText("SOTELABI");
        jtSOTELABI.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOTELABI.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOTELCELL.setText("SOTELCELL");
        jtSOTELCELL.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOTELCELL.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOEMAIL.setText("SOEMAIL");
        jtSOEMAIL.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOEMAIL.setPreferredSize(new java.awt.Dimension(100, 19));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jlSOID)
                                    .add(jlSOCOGNOME)
                                    .add(jlSONOME)
                                    .add(jlSOCONAS)
                                    .add(jlSOINDI)
                                    .add(jlSOREDI))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jlOutSOID)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                                .add(jtSOCAP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                                                .add(89, 89, 89))
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jtSOINDI, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(jtSODATANAS, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jtSONOME, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jtSOCOGNOME, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jtSOCONAS, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jtSOREDI, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jlSOEMAIL)
                                            .add(jlSOTELCELL)
                                            .add(jlSOTELABI)
                                            .add(jlSONUMTESS)
                                            .add(jlSOANNOTESS)
                                            .add(jlSOSESSO)
                                            .add(jlSOTIPO)))))
                            .add(jlSODATANAS))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jtSOEMAIL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                            .add(jtSOTELABI, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jtSOTELCELL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jcoSOTIPO, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jtSOANNOTESS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jtSONUMTESS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(jrM)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jrF)))
                                .add(115, 115, 115)))
                        .addContainerGap())
                    .add(jlSOCAP)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOID)
                    .add(jlOutSOID))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOCOGNOME)
                    .add(jtSOCOGNOME, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSOTIPO)
                    .add(jcoSOTIPO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSONOME)
                    .add(jrM)
                    .add(jrF)
                    .add(jtSONOME, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSOSESSO))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOCONAS)
                    .add(jtSOANNOTESS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtSOCONAS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSOANNOTESS))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSODATANAS)
                    .add(jtSONUMTESS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtSODATANAS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSONUMTESS))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOINDI)
                    .add(jtSOTELABI, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtSOINDI, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSOTELABI))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jtSOTELCELL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSOREDI)
                    .add(jtSOREDI, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSOTELCELL))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOCAP)
                    .add(jtSOCAP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtSOEMAIL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jlSOEMAIL))
                .add(26, 26, 26))
        );

        jTabbedPane1.addTab("dati principali", jPanel1);

        jlSONOTE.setText("Note");

        jtaSONOTE.setMinimumSize(new java.awt.Dimension(200, 180));
        jtaSONOTE.setPreferredSize(new java.awt.Dimension(300, 180));

        jlSOCODFISC.setText("Codice fiscale");

        jtSOCODFISC.setText("SOCODFISC");
        jtSOCODFISC.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOCODFISC.setPreferredSize(new java.awt.Dimension(100, 19));

        jlSOPATENTE.setText("Patente");

        jtSOPATENTE.setText("SOPATENTE");
        jtSOPATENTE.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOPATENTE.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOPROFESSIONE.setText("SOPROFESSIONE");
        jtSOPROFESSIONE.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOPROFESSIONE.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOGRUPPO.setText("SOGRUPPO");
        jtSOGRUPPO.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOGRUPPO.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOSERVIZIO.setText("SOSERVIZIO");
        jtSOSERVIZIO.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOSERVIZIO.setPreferredSize(new java.awt.Dimension(100, 19));

        jtSOALTRE.setText("SOALTRE");
        jtSOALTRE.setMinimumSize(new java.awt.Dimension(100, 19));
        jtSOALTRE.setPreferredSize(new java.awt.Dimension(100, 19));

        jlSOPROFESSIONE.setText("Professione");

        jlSOGRUPPO.setText("Gruppo sanguigno");

        jlSOSERVIZIO.setText("Servizio Militare");

        jlSOALTRE.setText("Altre associazioni");

        org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jlSOGRUPPO)
                    .add(jlSOPROFESSIONE)
                    .add(jlSOPATENTE)
                    .add(jlSOCODFISC)
                    .add(jlSOSERVIZIO)
                    .add(jlSOALTRE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jtSOALTRE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .add(jtSOSERVIZIO, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .add(jtSOCODFISC, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .add(jtSOPATENTE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .add(jtSOPROFESSIONE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jtSOGRUPPO, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOCODFISC)
                    .add(jtSOCODFISC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOPATENTE)
                    .add(jtSOPATENTE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOPROFESSIONE)
                    .add(jtSOPROFESSIONE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOGRUPPO)
                    .add(jtSOGRUPPO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOSERVIZIO)
                    .add(jtSOSERVIZIO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlSOALTRE)
                    .add(jtSOALTRE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jlSONOTE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtaSONOTE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                .add(40, 40, 40))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jlSONOTE)
                            .add(jtaSONOTE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 144, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(214, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("altri dati", jPanel2);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("socio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel1, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("disponibili");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        jtDotSocio.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jtDotSocio);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel3.add(jScrollPane1, gridBagConstraints);

        jbAddDotazione.setText("<");
        jbAddDotazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddDotazioneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jbAddDotazione, gridBagConstraints);

        jbDeleteDotazione.setText(">");
        jbDeleteDotazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteDotazioneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jbDeleteDotazione, gridBagConstraints);

        jScrollPane2.setViewportView(jlDotDispo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel3.add(jScrollPane2, gridBagConstraints);

        jTabbedPane1.addTab("dotazioni", jPanel3);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("socio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jLabel3, gridBagConstraints);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("disponibili");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jLabel4, gridBagConstraints);

        jScrollPane3.setViewportView(jlIntSocio);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel4.add(jScrollPane3, gridBagConstraints);

        jbAddIntervento.setText("<");
        jbAddIntervento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddInterventoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jbAddIntervento, gridBagConstraints);

        jbDeleteIntervento.setText(">");
        jbDeleteIntervento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteInterventoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jbDeleteIntervento, gridBagConstraints);

        jScrollPane4.setViewportView(jlIntDispo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel4.add(jScrollPane4, gridBagConstraints);

        jTabbedPane1.addTab("interventi", jPanel4);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("socio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(jLabel5, gridBagConstraints);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("disponibili");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(jLabel6, gridBagConstraints);

        jScrollPane5.setViewportView(jlEseSocio);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel5.add(jScrollPane5, gridBagConstraints);

        jbAddEsercitazione.setText("<");
        jbAddEsercitazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddEsercitazioneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(jbAddEsercitazione, gridBagConstraints);

        jbDeleteEsercitazione.setText(">");
        jbDeleteEsercitazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteEsercitazioneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(jbDeleteEsercitazione, gridBagConstraints);

        jScrollPane6.setViewportView(jlEseDispo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel5.add(jScrollPane6, gridBagConstraints);

        jTabbedPane1.addTab("esercitazioni", jPanel5);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("socio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel10.add(jLabel7, gridBagConstraints);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("disponibili");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel10.add(jLabel8, gridBagConstraints);

        jScrollPane7.setViewportView(jlSpeSocio);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel10.add(jScrollPane7, gridBagConstraints);

        jbAddSpecializzazione.setText("<");
        jbAddSpecializzazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddSpecializzazioneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel10.add(jbAddSpecializzazione, gridBagConstraints);

        jbDeleteSpecializzazione.setText(">");
        jbDeleteSpecializzazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteSpecializzazioneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel10.add(jbDeleteSpecializzazione, gridBagConstraints);

        jScrollPane8.setViewportView(jlSpeDispo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.weighty = 100.0;
        jPanel10.add(jScrollPane8, gridBagConstraints);

        jTabbedPane1.addTab("specializzazioni", jPanel10);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel8.add(jlSOFOTO, gridBagConstraints);

        jPanel6.add(jPanel8, java.awt.BorderLayout.CENTER);

        jbCarica.setIcon(new javax.swing.ImageIcon("/home/simeo/java/projects/netbeans/Proci/immagini/common/salva.png")); // NOI18N
        jbCarica.setMnemonic('c');
        jbCarica.setLabel("carica immagine da file");
        jbCarica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCaricaActionPerformed(evt);
            }
        });

        jbCarica1.setMnemonic('c');
        jbCarica1.setText("scegli  immagine caricata da app");
        jbCarica1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCarica1ActionPerformed(evt);
            }
        });

        jlPath.setBorder(null);

        jbElimina.setMnemonic('e');
        jbElimina.setText("elimina immagine");
        jbElimina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbEliminaActionPerformed(evt);
            }
        });

        jLabel9.setText("filename:");

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jlPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(jPanel9Layout.createSequentialGroup()
                .add(jbCarica)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbCarica1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 338, Short.MAX_VALUE)
                .add(jbElimina))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel9)
                    .add(jlPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jbCarica)
                        .add(jbCarica1))
                    .add(jbElimina)))
        );

        jPanel6.add(jPanel9, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("foto", jPanel6);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jbOk.setText("ok");
        jbOk.setEnabled(false);
        jbOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOkActionPerformed(evt);
            }
        });
        jPanel7.add(jbOk);

        jbCancel.setText("annulla");
        jbCancel.setEnabled(false);
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });
        jPanel7.add(jbCancel);

        add(jPanel7, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jbEliminaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbEliminaActionPerformed
        imageFile = null;
        jlSOFOTO.setIcon(null);        
        if (jlPath.getText().length() > 0) {
            changed(null);
            eliminaFoto = true;
        }
        jlPath.setText("");
    }//GEN-LAST:event_jbEliminaActionPerformed
    
    private void jbAddSpecializzazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddSpecializzazioneActionPerformed
        if (jlSpeDispo.getSelectedIndex() != -1) {
            Object[] values = jlSpeDispo.getSelectedValues();
            for (Object value : values) {
                Specializzazione spe = (Specializzazione) value;
                lmSpeDispo.removeElement(spe);
                lmSpeSocio.addElement(spe);
            }
            changed(null);
        }
    }//GEN-LAST:event_jbAddSpecializzazioneActionPerformed
    
    private void jbDeleteSpecializzazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteSpecializzazioneActionPerformed
        if (jlSpeSocio.getSelectedIndex() != -1) {
            Object[] values = jlSpeSocio.getSelectedValues();
            for (Object value : values) {
                Specializzazione spe = (Specializzazione) value;
                lmSpeSocio.removeElement(spe);
                lmSpeDispo.addElement(spe);
            }
            changed(null);
        }
    }//GEN-LAST:event_jbDeleteSpecializzazioneActionPerformed
    
    private void jbCaricaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCaricaActionPerformed
        if (app.getImagePath() == null) {
            app.setImagePath(app.getAppPath());
        }
        File curDir = new File(app.getImagePath());
        JFileChooser jfc = new JFileChooser(curDir);
        jfc.setMultiSelectionEnabled(false);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imageFile = jfc.getSelectedFile();
            eliminaFoto = true;
            app.setImagePath(imageFile.getParent());
            changed(null);
            jlSOFOTO.setIcon(new javax.swing.ImageIcon(imageFile.getAbsolutePath()));
            jlPath.setText(imageFile.getName());
        }
    }//GEN-LAST:event_jbCaricaActionPerformed
    
    private void jbAddEsercitazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddEsercitazioneActionPerformed
        if (jlEseDispo.getSelectedIndex() != -1) {
            Object[] values = jlEseDispo.getSelectedValues();
            for (Object value : values) {
                Esercitazione ese = (Esercitazione) value;
                lmEseDispo.removeElement(ese);
                lmEseSocio.addElement(ese);
            }
            changed(null);
        }
    }//GEN-LAST:event_jbAddEsercitazioneActionPerformed
    
    private void jbDeleteEsercitazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteEsercitazioneActionPerformed
        if (jlEseSocio.getSelectedIndex() != -1) {
            Object[] values = jlEseSocio.getSelectedValues();
            for (Object value : values) {
                Esercitazione ese = (Esercitazione) value;
                lmEseSocio.removeElement(ese);
                lmEseDispo.addElement(ese);
            }
            changed(null);
        }
    }//GEN-LAST:event_jbDeleteEsercitazioneActionPerformed
    
    private void jbDeleteInterventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteInterventoActionPerformed
        if (jlIntSocio.getSelectedIndex() != -1) {
            Object[] values = jlIntSocio.getSelectedValues();
            for (Object value : values) {
                Intervento inte = (Intervento) value;
                lmIntSocio.removeElement(inte);
                lmIntDispo.addElement(inte);
            }
            changed(null);
        }
    }//GEN-LAST:event_jbDeleteInterventoActionPerformed
    
    private void jbAddInterventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddInterventoActionPerformed
        if (jlIntDispo.getSelectedIndex() != -1) {
            Object[] values = jlIntDispo.getSelectedValues();
            for (Object value : values) {
                Intervento inte = (Intervento) value;
                lmIntDispo.removeElement(inte);
                lmIntSocio.addElement(inte);
            }
            changed(null);
        }
    }//GEN-LAST:event_jbAddInterventoActionPerformed
    
    private void jbDeleteDotazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteDotazioneActionPerformed
        if (jtDotSocio.getSelectedRow() != -1) {
            int[] values = jtDotSocio.getSelectedRows();
            for (int i=values.length - 1; i>=0 ;i--) {
                int idDot = ((Integer) tmDotSocio.getValueAt(values[i], 0));
                tmDotSocio.remove(values[i]);
                lmDotDispo.addElement(tmDotSocio.lookupDotazione(idDot));
            }
            changed(null);
        }
    }//GEN-LAST:event_jbDeleteDotazioneActionPerformed
    
    private void jbAddDotazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddDotazioneActionPerformed
        if (jlDotDispo.getSelectedIndex() != -1) {
            Object[] values = jlDotDispo.getSelectedValues();
            for (Object value : values) {
                Dotazione dot = (Dotazione) value;
                lmDotDispo.removeElement(dot);
                tmDotSocio.add(dot);
            }
            changed(null);
        }
    }//GEN-LAST:event_jbAddDotazioneActionPerformed
    
    private void jbOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOkActionPerformed
        okActionPerformed();
    }//GEN-LAST:event_jbOkActionPerformed
    
    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        cancelActionPerformed();
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jbCarica1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCarica1ActionPerformed
   
//        SwingWorker worker = new SwingWorker() {
//            @Override
//            protected Object doInBackground() throws Exception {
//                return new LoadImage();
//            }
//        };
//        worker.run();    

    }//GEN-LAST:event_jbCarica1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton jbAddDotazione;
    private javax.swing.JButton jbAddEsercitazione;
    private javax.swing.JButton jbAddIntervento;
    private javax.swing.JButton jbAddSpecializzazione;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbCarica;
    private javax.swing.JButton jbCarica1;
    private javax.swing.JButton jbDeleteDotazione;
    private javax.swing.JButton jbDeleteEsercitazione;
    private javax.swing.JButton jbDeleteIntervento;
    private javax.swing.JButton jbDeleteSpecializzazione;
    private javax.swing.JButton jbElimina;
    private javax.swing.JButton jbOk;
    private javax.swing.JComboBox jcoSOTIPO;
    private javax.swing.JList jlDotDispo;
    private javax.swing.JList jlEseDispo;
    private javax.swing.JList jlEseSocio;
    private javax.swing.JList jlIntDispo;
    private javax.swing.JList jlIntSocio;
    private javax.swing.JLabel jlOutSOID;
    private javax.swing.JLabel jlPath;
    private javax.swing.JLabel jlSOALTRE;
    private javax.swing.JLabel jlSOANNOTESS;
    private javax.swing.JLabel jlSOCAP;
    private javax.swing.JLabel jlSOCODFISC;
    private javax.swing.JLabel jlSOCOGNOME;
    private javax.swing.JLabel jlSOCONAS;
    private javax.swing.JLabel jlSODATANAS;
    private javax.swing.JLabel jlSOEMAIL;
    private javax.swing.JLabel jlSOFOTO;
    private javax.swing.JLabel jlSOGRUPPO;
    private javax.swing.JLabel jlSOID;
    private javax.swing.JLabel jlSOINDI;
    private javax.swing.JLabel jlSONOME;
    private javax.swing.JLabel jlSONOTE;
    private javax.swing.JLabel jlSONUMTESS;
    private javax.swing.JLabel jlSOPATENTE;
    private javax.swing.JLabel jlSOPROFESSIONE;
    private javax.swing.JLabel jlSOREDI;
    private javax.swing.JLabel jlSOSERVIZIO;
    private javax.swing.JLabel jlSOSESSO;
    private javax.swing.JLabel jlSOTELABI;
    private javax.swing.JLabel jlSOTELCELL;
    private javax.swing.JLabel jlSOTIPO;
    private javax.swing.JList jlSpeDispo;
    private javax.swing.JList jlSpeSocio;
    private javax.swing.JRadioButton jrF;
    private javax.swing.JRadioButton jrM;
    private javax.swing.JTable jtDotSocio;
    private javax.swing.JFormattedTextField jtSOALTRE;
    private javax.swing.JFormattedTextField jtSOANNOTESS;
    private javax.swing.JFormattedTextField jtSOCAP;
    private javax.swing.JFormattedTextField jtSOCODFISC;
    private javax.swing.JFormattedTextField jtSOCOGNOME;
    private javax.swing.JFormattedTextField jtSOCONAS;
    private javax.swing.JFormattedTextField jtSODATANAS;
    private javax.swing.JFormattedTextField jtSOEMAIL;
    private javax.swing.JFormattedTextField jtSOGRUPPO;
    private javax.swing.JFormattedTextField jtSOINDI;
    private javax.swing.JFormattedTextField jtSONOME;
    private javax.swing.JFormattedTextField jtSONUMTESS;
    private javax.swing.JFormattedTextField jtSOPATENTE;
    private javax.swing.JFormattedTextField jtSOPROFESSIONE;
    private javax.swing.JFormattedTextField jtSOREDI;
    private javax.swing.JFormattedTextField jtSOSERVIZIO;
    private javax.swing.JFormattedTextField jtSOTELABI;
    private javax.swing.JFormattedTextField jtSOTELCELL;
    private javax.swing.JTextArea jtaSONOTE;
    // End of variables declaration//GEN-END:variables
    
}
