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

package proci.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import proci.App;
import proci.EDirectories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simeo
 */
public abstract class ProciUtils {

    static final Logger logger = LoggerFactory.getLogger(ProciUtils.class);    

    public static String[] socio_properties = {
        "SOID",
        "SOCOGNOME",
        "SONOME",
        "SOCONAS",
        "SOSESSO",
        "SODATANAS",
        "SOINDI",
        "SORESI",
        "SOCAP",
        "SOTELABI",
        "SOCODFISC",
        "SOTELCELL",
        "SOEMAIL",
        "SOANNOTESS",
        "SONUMTESS",
        "SOPATENTE",
        "SOPROFESSIONE",
        "SOFOTO",
        "SOTIPO",
        "SOGRUPPO",
        "SOSERVIZIO",
        "SOALTRE",
        "SONOTE" };
        
    public static String[] socio_intest = {        
        "IDSocio",
        "Cognome",
        "Nome",
        "Comune nascita",
        "Sesso",
        "Data",
        "Indirizzo",
        "Residenza",
        "CAP",
        "Telefono Abitazione",
        "Codice Fiscale",
        "Telefono Cellulare",
        "Indirizzo Posta Elettronica",
        "Anno tessera",
        "Numero Tessera",                
        "Patente",
        "Professione",
        "Fotografia",
        "Tipo Socio",
        "Gruppo Sanguigno",
        "Servizio Militare",
        "Altre Associazioni",        
        "Note"};
        
    public static void copiaFotoSoci(File imgSrc) {
        try {
            FileChannel src = new FileInputStream(imgSrc).getChannel();
            FileChannel dst = new FileOutputStream(App.getInstance().getAppPath(EDirectories.IMAGES_PHOTOS) + imgSrc.getName()).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (IOException e) {
            logger.error("Errore copia immagine: {}",e);    
        }            
   }

    public static void creaMiniatura(File imgSrc) {
        try {
            File imgMini = new File(App.getInstance().getAppPath(EDirectories.IMAGES_THUMBS) + imgSrc.getName());
            BufferedImage bi = ImageIO.read(imgSrc);
            BufferedImage scaledImg = new BufferedImage(20, 20,BufferedImage.TYPE_INT_RGB);
            Graphics2D gScaledImg = scaledImg.createGraphics();
            gScaledImg.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
            gScaledImg.drawImage(bi, 0, 0, 20,20, null);
            ImageIO.write(scaledImg, "jpeg", imgMini);
        } catch (IOException e) {
            logger.error("Errore creazione miniatura: {}",e);
        }     
    }    
}
