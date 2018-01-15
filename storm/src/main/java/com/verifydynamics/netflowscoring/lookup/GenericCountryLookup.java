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

public class GenericCountryLookup { // TODO make generic.....

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(GenericCountryLookup.class));

    private static GenericCountryLookup instance = new GenericCountryLookup();

    HashMap<String, Double> countryHash =  new HashMap<String, Double>();

    // singleton
    private GenericCountryLookup() {

        PostgresContext postgresContext = PostgresContext.getInstance();
        Connection connection = postgresContext.getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select country_code, weighting from public.suspect_country;");

            while ( rs.next() ) {
                LOG.info("row = " + rs.getString("country_code") + " : " + rs.getInt("weighting"));
                countryHash.put(rs.getString("country_code"),rs.getDouble("weighting"));
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage());
        }

    }

    public static GenericCountryLookup getInstance() {
        return instance;
    }

    public double exists (String country_code) {
        if (countryHash.get(country_code) != null) {
            return countryHash.get(country_code);
        }
        return 0.0;
    }
}

