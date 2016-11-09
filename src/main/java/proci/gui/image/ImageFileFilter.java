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

import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileFilter;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simeo
 */
public class ImageFileFilter extends FileFilter {
    static final Logger logger = LoggerFactory.getLogger(ImageFileFilter.class);    
    Tika tika = null;

    public ImageFileFilter() {
        super();
        tika = new Tika();
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        logger.debug("File: {}",file);
        String mimeType;
        try {
            mimeType = tika.detect(file);
            switch (mimeType) {
                // all this formats should be supported by
                // java and Scalr libraries
                case "image/gif":
                case "image/jpeg":
                case "image/bmp":
                case "image/png":
                case "image/x-ms-bmp":
                case "image/tiff":
                    logger.debug("Accepting mimetype: {}",mimeType);
                    return true;
                default:
                    logger.debug("Rejecting mimetype: {}",mimeType);
                    return false;
            }
        } catch (IOException ex) {
            logger.error("Error: ",ex);            
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Image files";
    }
}
