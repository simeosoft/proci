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

/**
 *
 * @author simeo
 */
public class ImageCropEvent {

    public enum ICEvent {
        LOAD,
        MOUSE,
        CLIP,
        CROP,
        UNDO;
    }
    private ICEvent eventType = null;
    private String paramString = null;

    public ImageCropEvent(ICEvent eventType) {
        this.eventType = eventType;
    }
    /**
     * Buils a new ImageCropEvent.
     * 
     * If event is MOUSE or CLIP, param string is mandatory but not reinforced
     * in code.
     * 
     * @param eventType event type
     * @param paramString event description
     */
    public ImageCropEvent(ICEvent eventType,String paramString) {
        this.eventType = eventType;
        this.paramString = paramString;
    }
    /**
     * @return the eventType
     */
    public ICEvent getEventType() {
        return eventType;
    }

    /**
     * @return the paramString
     */
    public String getParamString() {
        if (paramString != null) {
            return paramString;
        }
        switch (eventType) {
            case LOAD:
                return "Image loaded";
            case CROP:
                return "Image cropped!";
            case UNDO:
                return "Undo!";
            case MOUSE: // for mouse event paramString is mandatory - should not be here
            case CLIP: // for clip event paramString is mandatory - should not be here
            default:
                return "Event unknown!?";
        }
    }    
    
    @Override
    public String toString() {
        return "Event: " + eventType.name() + (paramString == null ? "" : " " + paramString);
    }
}
