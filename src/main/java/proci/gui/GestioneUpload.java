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

import proci.gui.image.ImageFileFilter;
import com.sun.net.httpserver.HttpServer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import static java.nio.charset.CoderResult.OVERFLOW;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import proci.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proci.gui.image.MetaImage;
import proci.gui.image.MetaImageService;


/**
 *
 * @author simeo
 */
public class GestioneUpload extends javax.swing.JInternalFrame implements ProciInternalFrame {
    static final Logger logger = LoggerFactory.getLogger(GestioneUpload.class);    
    private static final long serialVersionUID = 5814535751682385164L;
    private boolean editing = false;
    private Connection conn = null;
    private ServerWorker sw;
    private DirListener dl;
    private final App app = App.getInstance();
    private File curDir = null;
    //
    private MetaImageService miservice;
    /**
     * Creates new form GestioneUpload
     */
    public GestioneUpload() {
        initComponents();
        jProgressBar1.setStringPainted(true);
        // servizio per la identificazione e immagazzinamento immagini
        miservice = new MetaImageService();
        miservice.setNoImage(new File(app.getAppPath(EDirectories.IMAGES_COMMON) + "noimage.png"));
        // setta layout
        jpLayer.setLayout(new WrapLayout());
        // fle già presenti nella directory immagini_upload
        LoadFilesWorker l = new LoadFilesWorker();
        l.execute();
        // avvia il listener sulla directory (potrebbe essere stato copiato un file
        // nella directory oppure un upload dall'app)
        dl = new DirListener();
        dl.execute();
        //
        jbElimina.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elimina();
            }
        });
        //
        jbAggiungi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addImage();
            }
        });
    }

    /**
     * Restituisce lo stato corrente
     *
     * @return
     */
    @Override
    public boolean isBusy() {
        return editing;
    }
    public void closeFrame() {
        if (sw != null) { sw.stop(); sw.cancel(true); };
        //
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        dl.cancel(true);
        this.dispose();
    }

    private void addImage() {
        if (curDir == null) {
            curDir = new File(System.getProperty("user.dir"));
        }
        JFileChooser jfc = new JFileChooser(curDir);
        jfc.setMultiSelectionEnabled(false);
        FileFilter f = new ImageFileFilter();
        jfc.setFileFilter(f);
        try {
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                curDir = jfc.getSelectedFile();
                Path dest = Paths.get(app.getAppPath(EDirectories.IMAGES_UPLOADS) + curDir.getName());
                Files.copy(jfc.getSelectedFile().toPath(), 
                        dest,
                        StandardCopyOption.REPLACE_EXISTING);
            }                
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,ioe,"Errore caricamento immagine!",JOptionPane.ERROR_MESSAGE);
            logger.error("Errore caricamento immagine: {}",ioe);
        }
        
    }
    private void elimina() {
        Path dir = Paths.get(app.getAppPath(EDirectories.IMAGES_UPLOADS));
        int rc = JOptionPane.showConfirmDialog(this,"Confermi cancellazione di tutte le anteprime nella directory: " 
                + dir.toFile().getAbsolutePath() + "?","Conferma",
                JOptionPane.YES_NO_OPTION);
        if (rc == JOptionPane.NO_OPTION) {
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<java.nio.file.Path>(){
                @Override
                public FileVisitResult visitFile(java.nio.file.Path file,BasicFileAttributes attrs) throws IOException {
                    logger.debug("Deleting: {}",file);
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ioe) {
            logger.error("ERRORE: {}",ioe.getLocalizedMessage());
        }
        
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpopTree = new javax.swing.JPopupMenu();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jpLayer = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jbElimina = new javax.swing.JButton();
        jbAggiungi = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Libreria Immagini");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(jpLayer);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jbElimina.setText("elimina tutto");
        jbElimina.setEnabled(false);

        jbAggiungi.setText("aggiungi");
        jbAggiungi.setEnabled(false);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbAggiungi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbElimina))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jbElimina)
                        .addComponent(jbAggiungi)))
                .addGap(5, 5, 5))
        );

        jPanel4.add(jPanel7, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        closeFrame();
    }//GEN-LAST:event_formInternalFrameClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbAggiungi;
    private javax.swing.JButton jbElimina;
    private javax.swing.JPanel jpLayer;
    private javax.swing.JPopupMenu jpopTree;
    // End of variables declaration//GEN-END:variables

    private final class DirListener extends SwingWorker<Void, Path> {

        @Override
        protected Void doInBackground() throws Exception {
            WatchKey key;
            Path dir = Paths.get(app.getAppPath(EDirectories.IMAGES_UPLOADS));
            WatchService watcher = FileSystems.getDefault().newWatchService();
            logger.debug("Registrato Watcher su: {}",dir);
            try {
                key = dir.register(watcher,
                                       ENTRY_CREATE,
                                       ENTRY_DELETE,
                                       ENTRY_MODIFY);
            } catch (IOException ioe) {
                logger.error("Error: {}",ioe);
            }    

            for (;;) {
                // wait for key to be signaled
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return null;
                }
                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (kind == OVERFLOW) {
                        continue;
                    }
                    // The filename is the
                    // context of the event.
                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    Path filename = ev.context();
                    logger.debug("Watcher: nuovo file: {}",filename);
                    // Verify that the new
                    //  file is a text file.
                    try {
                        // Resolve the filename against the directory.
                        // If the filename is "test" and the directory is "foo",
                        // the resolved name is "test/foo".
                        Path child = dir.resolve(filename);
                        logger.debug("");
                        // TODO: inserire i corretti mime type
                        if (Files.probeContentType(child).equals("text/plain")) {
                            continue;
                        }
                        publish(child);
                    } catch (IOException ioe) {
                        logger.error("DirListener error: {}",ioe);
                        continue;
                    }
                }
                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
            return  null;
        }
        
        @Override
        protected void process(List<Path> p) {
            logger.debug("Creo immagine da {} e aggiungo al pannello",p.get(0));
            MetaImage mi = miservice.getMetaImage(p.get(0).toFile());
            ThumbPanel tp = new ThumbPanel(mi);
            jpLayer.add(tp);
            jpLayer.validate();
        }        
    }
    
    private File[] getFiles() {
        File imgDir = new File(app.getAppPath(EDirectories.IMAGES_UPLOADS));
        return imgDir.listFiles();
    }

    private final class LoadFilesWorker extends SwingWorker<Void, String> {

        private File[] files = null;
        private ArrayList<JPanel> panels = new ArrayList<>();

        @Override
        protected Void doInBackground() throws Exception {
            int perc = 0;
            publish("" + perc);
            files = getFiles();
            int curr = 0;
            for (File imgFile : files) {
                curr++;
                MetaImage mi = miservice.getMetaImage(imgFile);
                ThumbPanel tp = new ThumbPanel(mi);
                panels.add(tp);
                perc = (curr * 100) / files.length;
                publish("" + perc);
            }
            return null;
        }

        @Override
        protected void process(List<String> lperc) {
            String perc = lperc.get(lperc.size() - 1);
            jProgressBar1.setString(perc + "% completato");
            jProgressBar1.getModel().setValue(new Integer(perc));
        }

        @Override
        protected void done() {
            try {
                int numFiles = 0;
                if (files != null) {
                    // aggiorna(files);
                    numFiles = files.length;
                    for (JPanel panel : panels) {
                        jpLayer.add(panel);
                    }
                }
                jProgressBar1.setString("Caricati " + numFiles + " file!");
                jbElimina.setEnabled(true);
                jbAggiungi.setEnabled(true);
                jpLayer.validate();
            } catch (Exception ex) {
                logger.error("Load error: {}",ex.getLocalizedMessage());
            }
        }
    }
    
    private final class ServerWorker extends SwingWorker<Void, String> {

        private List<String> ips = new ArrayList<String>();
        private HttpServer server = null;
        private boolean doRun = true;
       
        public ServerWorker() {
            try {
                server = HttpServer.create(new InetSocketAddress(App.HTTPPORT), 0);
                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                while (e.hasMoreElements()) {
                    NetworkInterface ni = e.nextElement();
                    Enumeration<InetAddress> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress addr = e2.nextElement();
                        if (addr.isSiteLocalAddress() && ! addr.isLoopbackAddress()) {
                            logger.debug("Trovato IP Address: {}",addr.getHostAddress());
                            addIp(addr.getHostAddress());
                        }
                    }
                }  
                WebHandler wh = new WebHandler();
                server.createContext(App.HTTPCONTEXT, wh);
                server.start();                
                logger.debug("Started Web Server: Ip Address(es): {}, port; {}",getAddresses(),App.HTTPPORT);
            } catch (IOException ex) {
                logger.error("Exception creating http server: {}",ex.getLocalizedMessage());                
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            logger.debug("Running http server...");
            while (doRun) {
                Thread.sleep(1L);
            }
            logger.debug("Stopping http server...");            
            return null;
        }

        public void stop() {
            doRun = false;
        }
        
        @Override
        protected void process(List<String> lperc) {
            //lblMessage.setText("% completato");
        }

        @Override
        protected void done() {
            logger.debug("Stopping http server...");
            server.stop(0);
        }
        
        private void addIp(String ipAddr) {
            ips.add(ipAddr);
        }

        public String getAddresses() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ips.size(); i++) {
                sb.append(ips.get(i));
                if (i < ips.size() - 1) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }        
    }    

    /**
     * FlowLayout subclass that fully supports wrapping of components.
     * credits: https://tips4java.wordpress.com/2008/11/06/wrap-layout/
     * 
     * 
     * TODO: spostare in simeolib2
     */
    public class WrapLayout extends FlowLayout {

        private Dimension preferredLayoutSize;

        /**
         * Constructs a new <code>WrapLayout</code> with a left alignment and a
         * default 5-unit horizontal and vertical gap.
         */
        public WrapLayout() {
            super();
        }

        /**
         * Constructs a new <code>FlowLayout</code> with the specified alignment
         * and a default 5-unit horizontal and vertical gap. The value of the
         * alignment argument must be one of <code>WrapLayout</code>,
         * <code>WrapLayout</code>, or <code>WrapLayout</code>.
         *
         * @param align the alignment value
         */
        public WrapLayout(int align) {
            super(align);
        }

        /**
         * Creates a new flow layout manager with the indicated alignment and
         * the indicated horizontal and vertical gaps.
         * <p>
         * The value of the alignment argument must be one of
         * <code>WrapLayout</code>, <code>WrapLayout</code>, or
         * <code>WrapLayout</code>.
         *
         * @param align the alignment value
         * @param hgap the horizontal gap between components
         * @param vgap the vertical gap between components
         */
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        /**
         * Returns the preferred dimensions for this layout given the
         * <i>visible</i> components in the specified target container.
         *
         * @param target the component which needs to be laid out
         * @return the preferred dimensions to lay out the subcomponents of the
         * specified container
         */
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        /**
         * Returns the minimum dimensions needed to layout the <i>visible</i>
         * components contained in the specified target container.
         *
         * @param target the component which needs to be laid out
         * @return the minimum dimensions to lay out the subcomponents of the
         * specified container
         */
        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        /**
         * Returns the minimum or preferred dimension needed to layout the
         * target container.
         *
         * @param target target to get layout size for
         * @param preferred should preferred size be calculated
         * @return the dimension to layout the target container
         */
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
		//  Each row must fit with the width allocated to the containter.
                //  When the container width = 0, the preferred width of the container
                //  has not yet been calculated so lets ask for the maximum.

                int targetWidth = target.getSize().width;

                if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                }

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

		//  Fit components into the allowed width
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);

                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

				//  Can't add the component to current row. Start a new row.
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }

				//  Add a horizontal gap for all components after the first
                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }

                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }

                addRow(dim, rowWidth, rowHeight);

                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;

		//	When using a scroll pane or the DecoratedLookAndFeel we need to
                //  make sure the preferred size is less than the size of the
                //  target containter so shrinking the container size works
                //  correctly. Removing the horizontal gap is an easy way to do this.
                Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);

                if (scrollPane != null && target.isValid()) {
                    dim.width -= (hgap + 1);
                }

                return dim;
            }
        }

        /*
         *  A new row has been completed. Use the dimensions of this row
         *  to update the preferred size for the container.
         *
         *  @param dim update the width and height when appropriate
         *  @param rowWidth the width of the row to add
         *  @param rowHeight the height of the row to add
         */
        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);

            if (dim.height > 0) {
                dim.height += getVgap();
            }

            dim.height += rowHeight;
        }
    }
}
