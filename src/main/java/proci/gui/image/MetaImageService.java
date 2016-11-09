/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proci.gui.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.apache.tika.Tika;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simeo
 */
public class MetaImageService { 

    static final Logger logger = LoggerFactory.getLogger(MetaImageService.class);    
    
    private final Tika tika = new Tika();
    private File noImageFile = null;
    private BufferedImage noImage = null;
    private final ArrayList<String> standardImageMimeTypes = new ArrayList<>();

    public MetaImageService()  {
        standardImageMimeTypes.add("image/gif");
        standardImageMimeTypes.add("image/jpeg");
        standardImageMimeTypes.add("image/bmp");
        standardImageMimeTypes.add("image/png");
        standardImageMimeTypes.add("image/x-ms-bmp");
        standardImageMimeTypes.add("image/tiff");
    }

    public void setNoImage(File noImageFile) {
        this.noImageFile = noImageFile;
        try {
            noImage = ImageIO.read(noImageFile);
        } catch (IOException ioe) {
            logger.error("Error reading no image: ",ioe);
        }        
    }
    
    public synchronized MetaImage getMetaImage(File imageFile) {
        MetaImage mi = new MetaImage();
        mi.setFile(imageFile);
        mi.setValid(false);
        mi.setImage(noImage);
        String mimeType = "<unknown>";
        mi.setMimetype(mimeType);
        try {
            mimeType = tika.detect(imageFile);
            if (! mimeValid(mimeType)) {
                logger.debug("Document is not an image; {}",mimeType);
                mi.setDescr("Mime type not valid: " + mimeType);
                return mi;
            } 
            mi.setValid(true);
            BufferedImage bi = ImageIO.read(imageFile);
            mi.setImage(bi);
            mi.setDescr(imageFile.getName());
        } catch (IOException ioe) {
            logger.error("Error reading image: ",ioe);
            mi.setDescr("Error reading image: " + ioe);
            return mi;
        }
        logger.debug("Accepted image: {}",mimeType);
        return mi;
    }
    
    private boolean mimeValid(String mimeType) {
        for (String standardImageMimeType : standardImageMimeTypes) {
            if (standardImageMimeType.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }
}
