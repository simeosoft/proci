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
package proci;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
/**
 * Nuova versione con db H2.
 * 
 * @author simeo
 */
public class DBHandler {

    static final Logger logger = LoggerFactory.getLogger(DBHandler.class);   

    private static DBHandler instance = null;
    private JdbcDataSource ds;  // implementa DataSource
    private Server server;

    private DBHandler() {
    }
    
    public static DBHandler getInstance() {
        if (instance == null) {
            instance = new DBHandler();
        }
        return instance;
    }
    
    public String getUrl() {
        return "jdbc:h2:tcp://localhost/" + App.getInstance().getAppPath() + "data/procidb";
    }
    public void open() throws Exception  {
        logger.debug("Starting db... url: {}",getUrl());
        server = Server.createTcpServer();
        server.start();
        //ds = JdbcConnectionPool.create(getUrl(), "proci", "proci");
        ds = new JdbcDataSource();
        ds.setUrl(getUrl());
        ds.setUser("proci");
        ds.setPassword("proci");
        logger.debug("Datasource created: {}",ds);
        logger.debug("Db started! running: {}",server.isRunning(false));
    }
    
    public void close()  throws Exception {
        server.stop();
    }
       
    public DataSource getDataSource() {
        return ds;
    }
            
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    
    public void reopen() {
        try {
            close();
            Thread.sleep(2000);
            open();
            //Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            logger.error("Ops1: ",e);
        }
    }
    
//    public String getProps() {
//        StringBuilder b = new StringBuilder();
//        try {
//            Properties p = server.getCurrentProperties();
//            Enumeration e = p.keys();
//            while (e.hasMoreElements()) {
//                String key = (String) e.nextElement();
//                b.append("Key: [");
//                b.append(key);
//                b.append("], value: [");
//                b.append(p.getProperty(key));
//                b.append("]\n");
//            }
//        } catch (Exception e) {
//            logger.error("Ops2: ",e);
//        }         
//        return b.toString();
//    }
    
    public static void main(String[] args) {
        try {
            System.out.println("Istanzio database per test.. (solo Linux)");
            System.out.println("user dir: " + System.getProperty("user.dir"));
            App.getInstance().setAppPath(System.getProperty("user.dir"));
            DBHandler h = DBHandler.getInstance();
            h.open();
            System.out.println("Properties:");
//            System.out.println(h.getProps());
            System.out.println("PREMERE UN TASTO!...........................");
            System.in.read();
            System.out.println("Chiudo db.");
            h.close();
        } catch (Exception e) {
            System.out.println("---> errore: " + e.getLocalizedMessage());
        }
    }
}
