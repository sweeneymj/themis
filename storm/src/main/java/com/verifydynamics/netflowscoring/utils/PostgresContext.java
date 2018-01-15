/*******************************************************************
 Themis - NetFlow scoring and tagging framework.
 Version: 0.5
 Release date: 2017/12/31
 Author: MJ Sweeney
 Rhodes University
 Computer Science Masters Project - 2017
 Supervisor: Barry Irwin
 Copyright (C) 2017, MJ Sweeney
 *******************************************************************/
package com.verifydynamics.netflowscoring.utils;

import org.apache.log4j.Logger;

import java.sql.*;
import java.sql.ResultSet;
import java.sql.Statement;

public class PostgresContext {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PostgresContext.class);

    // TODO : should come from config or params
    private final String URL = "jdbc:postgresql://127.0.0.1/NetflowScore";
    // private final String URL = "jdbc:postgresql://10.8.0.1/NetflowScore";
    private final String USER = "nfs_user";
    private final String PASSWORD = "nfs_user";

    private static PostgresContext instance = new PostgresContext();

    private Connection db_connection;

    // singleton
    private PostgresContext() {
        connectDB(URL, USER, PASSWORD);
    }

    private int connectDB(String url, String user, String password) {
        try {
            db_connection = DriverManager.getConnection(url, user, password);

        } catch (SQLException ex) {
            LOG.error(ex.getMessage());
            LOG.error("PostgresContext : " + ex.getMessage());
            return 0;
        }
        return 1;
    }

    public static PostgresContext getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return db_connection;
    }

}
