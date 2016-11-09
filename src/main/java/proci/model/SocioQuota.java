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

package proci.model;

import java.math.BigDecimal;
import java.util.Date;

public class SocioQuota {
    private int SOID;
    private String SONOME;
    private String SOCOGNOME;
    private int QUOID;
    private int QUOPERID;
    private int QUOSOID;
    private String QUOFLAG;
    private String QUONOTE;
    private BigDecimal QUOPAG;
    private Date QUODATAGADGET;
    
    public SocioQuota() {
        QUOID = 0;
    }
    
    @Override
    public String toString() {
        return "socio: " + SOCOGNOME + " " + SONOME + ", id periodo: " + QUOPERID + ", id socio: " + SOID + ", id quota: " + QUOID;
    }

    /**
     * @return the QUOID
     */
    public int getQUOID() {
        return QUOID;
    }

    /**
     * @param QUOID the QUOID to set
     */
    public void setQUOID(int QUOID) {
        this.QUOID = QUOID;
    }

    /**
     * @return the QUOPERID
     */
    public int getQUOPERID() {
        return QUOPERID;
    }

    /**
     * @param QUOPERID the QUOPERID to set
     */
    public void setQUOPERID(int QUOPERID) {
        this.QUOPERID = QUOPERID;
    }

    /**
     * @return the QUOSOID
     */
    public int getQUOSOID() {
        return QUOSOID;
    }

    /**
     * @param QUOSOID the QUOSOID to set
     */
    public void setQUOSOID(int QUOSOID) {
        this.QUOSOID = QUOSOID;
    }

    /**
     * @return the QUOFLAG
     */
    public String getQUOFLAG() {
        return QUOFLAG;
    }

    /**
     * @param QUOFLAG the QUOFLAG to set
     */
    public void setQUOFLAG(String QUOFLAG) {
        this.QUOFLAG = QUOFLAG;
    }

    /**
     * @return the QUONOTE
     */
    public String getQUONOTE() {
        return QUONOTE;
    }

    /**
     * @param QUONOTE the QUONOTE to set
     */
    public void setQUONOTE(String QUONOTE) {
        this.QUONOTE = QUONOTE;
    }

    /**
     * @return the QUOPAG
     */
    public BigDecimal getQUOPAG() {
        return QUOPAG;
    }

    /**
     * @param QUOPAG the QUOPAG to set
     */
    public void setQUOPAG(BigDecimal QUOPAG) {
        this.QUOPAG = QUOPAG;
    }

    /**
     * @return the QUODATAGADGET
     */
    public Date getQUODATAGADGET() {
        return QUODATAGADGET;
    }

    /**
     * @param QUODATAGADGET the QUODATAGADGET to set
     */
    public void setQUODATAGADGET(Date QUODATAGADGET) {
        this.QUODATAGADGET = QUODATAGADGET;
    }

    /**
     * @return the SOID
     */
    public int getSOID() {
        return SOID;
    }

    /**
     * @param SOID the SOID to set
     */
    public void setSOID(int SOID) {
        this.SOID = SOID;
    }

    /**
     * @return the SONOME
     */
    public String getSONOME() {
        return SONOME;
    }

    /**
     * @param SONOME the SONOME to set
     */
    public void setSONOME(String SONOME) {
        this.SONOME = SONOME;
    }

    /**
     * @return the SOCOGNOME
     */
    public String getSOCOGNOME() {
        return SOCOGNOME;
    }

    /**
     * @param SOCOGNOME the SOCOGNOME to set
     */
    public void setSOCOGNOME(String SOCOGNOME) {
        this.SOCOGNOME = SOCOGNOME;
    }
}
