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
package com.verifydynamics.netflowscoring.lookup;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.verifydynamics.netflowscoring.utils.PostgresContext;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class GenericServiceLookup {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(GenericServiceLookup.class));

    Table<String, Integer, Boolean> ipPortTable = HashBasedTable.create();
    HashMap<String, Boolean> ipHash =  new HashMap<String, Boolean>();

    // singleton
    public GenericServiceLookup(int protocol, boolean is_hosted) {

        PostgresContext postgresContext = PostgresContext.getInstance();
        Connection connection = postgresContext.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select ip_address, port from public.list_known_host, public.list_known_host_service where list_known_host.id = list_known_host_service.list_known_host_id and protocol = " + protocol + " and is_hosted = " + is_hosted + ";");

            while ( rs.next() ) {
                LOG.info("row = " + rs.getString("ip_address") + " : " + rs.getInt("port"));
                ipPortTable.put(rs.getString("ip_address"), rs.getInt("port"), true);
                ipHash.put(rs.getString("ip_address"),true);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage());
        }

    }

    public boolean exists (String ip_address, Integer port) {
        if (ipPortTable.get(ip_address, port) != null) {
            return true;
        }
        return false;
    }

    public boolean exists (String ip_address) {
        if (ipHash.get(ip_address) != null) {
            return true;
        }
        return false;
    }
}

