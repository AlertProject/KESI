/*
 * Copyright (C) 2011-2013 GSyC/LibreSoft, Universidad Rey Juan Carlos
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Authors: Santiago Dueñas <sduenas@libresoft.es>
 *          Luis Cañas Díaz <lcanas@bitergia.com>
 *
 */

package eu.alertproject.kesi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Database {
    private Connection conn;
    protected Logger logger;

    public Database(String driver, String userName, String password,
            String host, String port, String database)
            throws DriverNotSupportedError, DatabaseConnectionError {
        Properties connectionProps;
        String url = null;

        logger = Logger.getLogger(Database.class);
        connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        try {
            if (driver.equals("mysql")) {
                url = "jdbc:" + driver + "://" + host + ":" + port + "/"
                        + database;
                logger.debug("Connecting to database. URL:" + url);
                conn = DriverManager.getConnection(url, connectionProps);
                logger.debug("Connection established");
            } else {
                throw new DriverNotSupportedError(driver);
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionError(e.getMessage(), url);
        }
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        return conn.prepareStatement(query);
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt;
        ResultSet rs;

        stmt = conn.createStatement();
        logger.debug("Executing query. QUERY: " + query);
        rs = stmt.executeQuery(query);
        logger.debug("Query executed");

        return rs;
    }

    public ResultSet executeQuery(PreparedStatement stmt) throws SQLException {
        ResultSet rs;

        logger.debug("Executing query. QUERY: " + stmt.toString());
        rs = stmt.executeQuery();
        logger.debug("Query executed");

        return rs;
    }

    /*
     * FIXME: move these to functions to other site change name of
     * dateToString
     */
    public static String dateToString(Date d) {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        return new String(date.format(d));
    }

    public static String dateToString(Timestamp d) {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        return new String(date.format(d));
    }

    public static Date stringToDate(String d) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd H:m:s").parse(d);
    }

}
