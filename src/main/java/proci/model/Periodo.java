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

public class Periodo {
    private int PERID;
    private String PERANNOCOMP;
    private String PERDESCR;
    private String PERNOTE;
    private BigDecimal PERQUOTA;
    private BigDecimal PERQUOTARID;
    
    public Periodo() {
        PERID = 0;
    }
    
    public Periodo(int PERID,
            String PERANNOCOMP,
            String PERDESCR,
            String PERNOTE,
            BigDecimal PERQUOTA,
            BigDecimal PERQUOTARID) {
        this.PERID = PERID;
        this.PERANNOCOMP = PERANNOCOMP;
        this.PERDESCR = PERDESCR;
        this.PERNOTE = PERNOTE;
        this.PERQUOTA = PERQUOTA;
        this.PERQUOTARID = PERQUOTARID;
    }
    public String toString() {
        return "(" + PERID + ") " + PERDESCR;
    }

    /**
     * @return the PERID
     */
    public int getPERID() {
        return PERID;
    }

    /**
     * @param PERID the PERID to set
     */
    public void setPERID(int PERID) {
        this.PERID = PERID;
    }

    /**
     * @return the PERANNOCOMP
     */
    public String getPERANNOCOMP() {
        return PERANNOCOMP;
    }

    /**
     * @param PERANNOCOMP the PERANNOCOMP to set
     */
    public void setPERANNOCOMP(String PERANNOCOMP) {
        this.PERANNOCOMP = PERANNOCOMP;
    }

    /**
     * @return the PERDESCR
     */
    public String getPERDESCR() {
        return PERDESCR;
    }

    /**
     * @param PERDESCR the PERDESCR to set
     */
    public void setPERDESCR(String PERDESCR) {
        this.PERDESCR = PERDESCR;
    }

    /**
     * @return the PERNOTE
     */
    public String getPERNOTE() {
        return PERNOTE;
    }

    /**
     * @param PERNOTE the PERNOTE to set
     */
    public void setPERNOTE(String PERNOTE) {
        this.PERNOTE = PERNOTE;
    }

    /**
     * @return the PERQUOTA
     */
    public BigDecimal getPERQUOTA() {
        return PERQUOTA;
    }

    /**
     * @param PERQUOTA the PERQUOTA to set
     */
    public void setPERQUOTA(BigDecimal PERQUOTA) {
        this.PERQUOTA = PERQUOTA;
    }

    /**
     * @return the PERQUOTARID
     */
    public BigDecimal getPERQUOTARID() {
        return PERQUOTARID;
    }

    /**
     * @param PERQUOTARID the PERQUOTARID to set
     */
    public void setPERQUOTARID(BigDecimal PERQUOTARID) {
        this.PERQUOTARID = PERQUOTARID;
    }
}
