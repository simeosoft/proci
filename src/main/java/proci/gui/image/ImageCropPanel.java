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

package proci.gui.image;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proci.gui.image.ImageCropEvent.ICEvent;

/**
 *
 * @author simeo
 */
public final class ImageCropPanel extends javax.swing.JPanel {
    static final Logger logger = LoggerFactory.getLogger(ImageCropPanel.class);
    private static final long serialVersionUID = -54626534406288808L;
    // original image
    private BufferedImage originalImage;
    // current image
    private BufferedImage currentImage;
    // current size (from currentImage) 
    private Dimension currentSize;
    // mouse event area 
    private Rectangle currentImageBounds;
    // true if paintComponent method should draw the selection rectangle
    private boolean drawCropArea;
    // resize clip ratio (fixed)
    private final double RATIO = 1.2121;
    // selection rectangle data
    private Rectangle clip;
    private final int clipWidth = 0;
    private final int clipHeight = 0;
    // zoom %
    private int zoomPerc = 100;
    // minimum % zoom step
    private final int zoomPercStep = 10;
    // store the pressed point
    private Point pressedPoint = null;
    // old point coordinates
    private Point oldClipXY = new Point();
    // image listeners
    private final ArrayList<ImageCropListener> listeners = new ArrayList<>();
    // current operation
    private enum EOp {
        NOOP,
        DRAW,
        RESIZE,
        MOVE;
    }
    private EOp currentOp = EOp.NOOP;
    // current mouse region
    private enum ERegion {
        IN_CLIP,
        IN_HANDLE,
        IN_IMAGE,
        OUT_IMAGE;
    }
    private ERegion currentRegion = ERegion.OUT_IMAGE;
    /**
     * External command
     */
        public enum ECommand {
        /**
         * Crop
         */
        CROP,
        /**
         * Undo image crop
         */
        UNDO,
        /**
         * Zoom in 
         */
        ZOOM_IN,
        /**
         * Zoom out
         */
        ZOOM_OUT,
        /**
         * Fit image to container
         */
        ZOOM_FIT,
        /**
         * restore zoom 1:1
         */
        ZOOM_RESTORE;
    }

    // useful for debug
    //JLabel debuglabel = null;
        
    /**
     * Creates new form ImageCropPanel
     */
    public ImageCropPanel() {
        initComponents();
        drawCropArea = false;
        addMouseListener(toggler);
        addComponentListener(resizer);
        addMouseMotionListener(motionlistener);
        addMouseWheelListener(wheellistener);
        // trap
        currentImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Creates new form ImageCropPanel
     * @param mi
     */
    public ImageCropPanel(MetaImage mi) {
        this();
        try {
            loadImageFile(mi);
        } catch (IOException ioe) {
            logger.error("ERRORE: ", ioe);
        }
    }    
    
    /**
     * Clip can't be null.
     * 
     * @return 
     */
    private Rectangle getClip() {
        if (clip == null) {
            clip = new Rectangle(clipWidth, clipHeight);
        }
        return clip;
    }

    /**
     * Load image into panel.
     * 
     * @param mi
     * @throws IOException
     */
    public void loadImageFile(MetaImage mi) throws IOException {
        originalImage = copyImage(mi.getImage());
        init();
    }    
    /**
     * Load image into panel.
     * 
     * @param file file image to load
     * @throws IOException
     */
    public void loadImageFile(File file) throws IOException {
        originalImage = ImageIO.read(file);
        init();
    }
    
    private void init() {
        currentImage =  new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        Graphics g = currentImage.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        currentSize = new Dimension(currentImage.getWidth(), currentImage.getHeight());  
        currentImageBounds = new Rectangle(0, 0, currentSize.width, currentSize.height);
        // 
        if (listeners.size() > 0) {
            ImageCropEvent ice = new ImageCropEvent(ICEvent.LOAD);
            propagateEvent(ice);
        }
    }
    /**
     * Buffered image clone.
     * 
     * @param source source image
     * @return new image
     */
    private BufferedImage copyImage(BufferedImage source){
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }    
    /**
     * Propagate an evento to listeners.
     * 
     * @param ice event
     */
    private void propagateEvent(ImageCropEvent ice) {
        for (ImageCropListener l : listeners) {
            l.onEvent(ice);
        }
    }
    
    /**
     * Override paintComponent.
     * 
     * Renders the image and draws the selection lines.
     * 
     * @param g Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        currentSize = new Dimension(currentImage.getWidth(), currentImage.getHeight());
        int x = (w - currentSize.width) / 2;
        int y = (h - currentSize.height) / 2;
        currentImageBounds = new Rectangle(x, y, currentSize.width, currentSize.height);
        g2.drawImage(currentImage, x, y, this);
        if (drawCropArea && clip != null) {
            logger.trace("drawing CLIP: {}", clip);
            g2.setPaint(Color.red);
            g2.draw(clip);
            g2.fillOval(clip.x + clip.width - 5, clip.y + clip.height - 5, 10, 10);
        }
        // border
        Rectangle border = new Rectangle(currentImageBounds);
        border.width += 2;
        border.height += 2;
        border.x -= 1;
        border.y -= 1;
        g2.setPaint(Color.BLUE);
        g2.draw(border);
    }

    /**
     * Returns the image size.
     * 
     * @return the image size.
     */
    @Override
    public Dimension getPreferredSize() {
        return currentSize;
    }

    /**
     * Zoom function.
     * 
     * @param ec zoom type
     */
    public void zoom(ECommand ec) {
        int resizeWidth;
        switch (ec) {
            case ZOOM_IN:
                if (zoomPerc < 300) {
                    zoomPerc += zoomPercStep;
                    resizeWidth = currentImage.getWidth() + (currentImage.getWidth() / 100 * zoomPercStep);
                    logger.trace("zoomlev: {}, res: {}",zoomPerc,resizeWidth);
                    currentImage = Scalr.resize(originalImage, resizeWidth);
                    drawCropArea = false;
                    currentOp = EOp.NOOP;
                    clip = null;
                    repaint();
                }
                break;
            case ZOOM_OUT:
                if (currentImage.getWidth() > 200) {
                    zoomPerc -= zoomPercStep;
                    resizeWidth = currentImage.getWidth() - (currentImage.getWidth() / 100 * zoomPercStep);
                    currentImage = Scalr.resize(originalImage, resizeWidth);
                    drawCropArea = false;
                    currentOp = EOp.NOOP;
                    clip = null;
                    repaint();
                }
                break;
            case ZOOM_RESTORE:
                currentImage = copyImage(originalImage);
                drawCropArea = false;
                currentOp = EOp.NOOP;
                clip = null;
                repaint();
                break;
            case ZOOM_FIT:
                currentImage = Scalr.resize(originalImage, Mode.FIT_TO_WIDTH, this.getWidth(),this.getHeight());
                drawCropArea = false;
                currentOp = EOp.NOOP;
                clip = null;
                repaint();
                break;
        }
    }

    /**
     * Crops the image to the current clip.
     * 
     */
    public void crop() {
        if (clip == null || clip.isEmpty()) {
            return;
        }
        if (clip.x < 0 || clip.y < 0 || clip.width < 0 || clip.height < 0) {
            return;
        }
        currentImage = Scalr.crop(currentImage, clip.x - currentImageBounds.x, clip.y - currentImageBounds.y, clip.width,clip.height);
        currentImageBounds = new Rectangle(clip);        
        drawCropArea = false;
        currentOp = EOp.NOOP;
        repaint();
        // 
        if (listeners.size() > 0) {
            ImageCropEvent ice = new ImageCropEvent(ICEvent.CROP);
            propagateEvent(ice);
        }        
    }

    /**
     * Returns the current image.
     * 
     * @return the current image
     */
    public BufferedImage getImage() {
        logEvent(null);
        if (currentImageBounds.width != originalImage.getWidth() &&
                currentImageBounds.height != originalImage.getHeight()) {
            if (zoomPerc != 100) {
                logger.debug("returning cropped image..");
                int realWidth = currentImage.getWidth() * 100 / zoomPerc;
                int realHeight = (int) (realWidth / RATIO);
                int zx = (clip.x * 100 / zoomPerc);
                int zy = (int) (zx / RATIO);
                logger.trace("getImage: currentimage: {}", currentImageBounds);
                logger.trace("getImage: clip: {}", clip);
                logger.trace("getImage: perc: {}, percstep: {}, x: {}, y: {}, scaledWidth: {}, scaledheight: {}",zoomPerc,zoomPercStep,zx,zy,realWidth,realHeight);
                return Scalr.crop(originalImage, zx,zy,realWidth,realHeight);
            }
            logger.debug("current image is now the cropped image..");
        }
        logger.debug("returning current image..");
        return currentImage;
    }
    
    /**
     * Restores the original image.
     * 
     */
    public void undo() {
        currentImage = copyImage(originalImage);
        drawCropArea = false;
        repaint();
        // 
        if (listeners.size() > 0) {
            ImageCropEvent ice = new ImageCropEvent(ICEvent.UNDO);
            propagateEvent(ice);
        }        
    }
    /**
     * Override the isFocusTraversable() method to accept
     * keys.
     * 
     * @return true
     */
    @Override
    public boolean isFocusTraversable() {
        return true;
    }

    /**
     * Computes the rectangle clip by coordinates
     * and mouse event type.
     * 
     * Returns the clip rectangle if drawable, null
     * otherwise
     * 
     * @param e mouse event
     * @return the clip or null
     */
    private Rectangle computeClip(MouseEvent e) {
        Rectangle computedClip = null;
        int height = 0;
        int width = 0;
//        // disegno solo se sono all'interno dell'immagine
//        if (currentOp == EOp.NOOP) { 
//            if (e.getX() < currentImageBounds.x || e.getY() < currentImageBounds.y) {
//                return computedClip;
//            }
//            if (e.getX() > (currentImageBounds.width - clipWidth)) {
//                return computedClip;
//            }
//            if (e.getY() > (currentImageBounds.height - clipHeight)) {
//                return computedClip;
//            }
//        }
        //
        switch (currentOp) {
            case DRAW:
                computedClip = new Rectangle(pressedPoint);
                height = e.getY() - pressedPoint.y;
                if (height < 0) {
                    height = 0;
                }        
                width = (int) (height / RATIO);
                computedClip.height = height;
                computedClip.width = width;
                logger.trace("isClipDrawable: computedClip: {}",computedClip);
                if (! currentImageBounds.contains(computedClip)) {
                    logger.trace("isClipDrawable DRAW: false");
                    return null;
                }
                break;
            case RESIZE:
                computedClip = new Rectangle(clip);
                height = e.getY() - clip.y;
                if (height < 0) {
                    height = 0;
                }        
                width = (int) (height / RATIO);
                computedClip.height = height;
                computedClip.width = width;
                logger.trace("isClipDrawable: computedClip: {}",computedClip);
                if (! currentImageBounds.contains(computedClip)) {
                    logger.trace("isClipDrawable RESIZE: false");
                    return null;
                }
                break;
            case MOVE:
                computedClip = new Rectangle(clip);
                int x = (int) e.getX() - pressedPoint.x;
                int y = (int) e.getY() - pressedPoint.y;
                computedClip.x = oldClipXY.x + x;
                computedClip.y = oldClipXY.y + y;
                logger.trace("isClipDrawable: computedClip: {}",computedClip);
                if (! currentImageBounds.contains(computedClip)) {
                    logger.trace("isClipDrawable MOVE: false");
                    return null;
                }
                break;
        }
        // 
        if (listeners.size() > 0) {
            ImageCropEvent ice = new ImageCropEvent(ICEvent.CLIP,"Clip: " + clip);
            propagateEvent(ice);
        }          
        return computedClip;
    }
    
    /**
     * Mouse motion listener
     */
    private final MouseMotionListener motionlistener = new MouseMotionListener() {
        @Override
        public void mouseMoved(MouseEvent e) {
            clip = getClip();
            Point point = e.getPoint();
            // uso un'area rettangolare per comodità
            Rectangle handle = new Rectangle(clip.x + clip.width - 5, clip.y + clip.height - 5, 10, 10);
            if (handle.contains(point)) {
                ImageCropPanel.this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                currentRegion = ERegion.IN_HANDLE;
            } else if (clip.contains(point)) {
                ImageCropPanel.this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                currentRegion = ERegion.IN_CLIP;
            } else if (currentImageBounds.contains(point)) {
                ImageCropPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                currentRegion = ERegion.IN_IMAGE;
            } else {
                ImageCropPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                currentRegion = ERegion.OUT_IMAGE;
                currentOp = EOp.NOOP;
            }
            // 
            if (listeners.size() > 0) {
                ImageCropEvent ice = new ImageCropEvent(ICEvent.MOUSE,"Mouse moved: " + point);
                propagateEvent(ice);
            }            
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            //logEvent(e);
            Rectangle computedClip = computeClip(e);
            if (computedClip != null) {
                clip = computedClip;
                drawCropArea = true;
                repaint();
                // 
                if (listeners.size() > 0) {
                    ImageCropEvent ice = new ImageCropEvent(ICEvent.MOUSE,"Mouse dragged: " + e.getPoint());
                    propagateEvent(ice);
                }            
            }
        }
    };

    /**
     * Mouse listener
     */
    private final MouseListener toggler = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            pressedPoint = e.getPoint();
            //logEvent(e);
            switch (currentRegion) {
                case IN_IMAGE:
                    currentOp = EOp.DRAW;
                    if (computeClip(e) != null) {
                        clip.x = pressedPoint.x;
                        clip.y = pressedPoint.y;
                        clip.width = 0;
                        clip.height = 0;
                        drawCropArea = true;
                    }
                    break;
                case IN_HANDLE:
                    currentOp = EOp.RESIZE;
                    break;
                case IN_CLIP:
                    oldClipXY.x = clip.x;
                    oldClipXY.y = clip.y;
                    currentOp = EOp.MOVE;
                    break;
                case OUT_IMAGE:
                    currentOp = EOp.NOOP;
                    break;
            }
            // 
            if (listeners.size() > 0) {
                ImageCropEvent ice = new ImageCropEvent(ICEvent.MOUSE,"Mouse pressed: " + e.getPoint());
                propagateEvent(ice);
            }            
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mouseDragged(MouseEvent e) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            //logEvent(e);
            currentOp = EOp.NOOP;
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
        }
        @Override
        public void mouseMoved(MouseEvent e) {
        }
    };

    /**
     * Mouse wheel listener
     */
    private final MouseWheelListener wheellistener = new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.getWheelRotation() > 0) {
                zoom(ECommand.ZOOM_IN);
            } else {
                zoom(ECommand.ZOOM_OUT);
            }
        }
    };
    /**
     * Repaint the image with a null clip if resize.
     * 
     */
    private final ComponentListener resizer = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            clip = null;
            repaint();
        }
    };

    /**
     *
     * @param icl
     */
    public void addImageCropListener(ImageCropListener icl) {
        listeners.add(icl);
    }

    /**
     *
     * @param icl
     */
    public void removeImageCropListener(ImageCropListener icl) {
        listeners.remove(icl);
    }

//    /**
//     *
//     * @param label
//     */
//    public void setDebuglabel(JLabel label) {
//        this.debuglabel = label;
//    }    
//    
    private void logEvent(MouseEvent e) {
//        if (debuglabel != null) {
//            debuglabel.setText("<html>Method: <b>" + e.paramString() +  "</b><br>Region: <b>" + currentRegion + "</b><br>Operation:  <b>" + currentOp +
//                            "</b><br>Clip: <b>" + clip +
//                            "</b><br>current image size: <b>" + currentSize +
//                            "</b><br>current image bounds: <b>" + currentImageBounds +
//                            "</b><br>eventPoint: <b>" + e.getPoint() +
//                            "</b><br>pressedPoint: <b>" + pressedPoint +
//                            "</b><br>oldClip: <b>" + oldClipXY);
////                            "</b><br>oldClipPoint: <b>" + oldClickPointX + "</b><br>oldClipY: <b>" + oldClickPointY);
//        }
        logger.trace("------------------------------------------------------------");
        logger.trace("event: {}", e != null ? e.paramString() : "no event");
        logger.trace("pressedPoint: {}", pressedPoint);
        logger.trace("current image size: (Dimension) {}", currentSize);
        logger.trace("current image bounds: (Rectangle) {}", currentImageBounds);
        logger.trace("clip: (Rectangle) {}", clip);
        logger.trace("Function: {}, Region: {}, operation: {}", e != null ? e.paramString() : "no function", currentRegion, currentOp);
        logger.trace("oldClip: {}",oldClipXY);
        logger.trace("------------------------------------------------------------");
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Test.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // main frame
//        JFrame f = new JFrame("");
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.setSize(400, 400);
//        f.setLocation(200, 200);
//        Dimension screenSize = f.getToolkit().getScreenSize();
//        //
//        JFrame fdebug = new JFrame("Debug frame");
//        fdebug.setSize(800, 400);
//        fdebug.setLocation(screenSize.width - f.getWidth(), 0);
//        fdebug.getContentPane().setLayout(new FlowLayout());
//        JLabel debugLabel = new JLabel();
//        debugLabel.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
//        fdebug.getContentPane().add(debugLabel);
//        fdebug.setVisible(true);
//        //
//        ImageCropPanel imageCropPanel = new ImageCropPanel();
//        imageCropPanel.loadImageFile(new File("immagini/upload/artico11.jpg"));
//        imageCropPanel.setDebuglabel(debugLabel);
//        //
//        f.getContentPane().setLayout(new BorderLayout());
//        f.getContentPane().add(new JScrollPane(imageCropPanel));
//        f.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}