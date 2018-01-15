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

import com.verifydynamics.netflowscoring.utils.PostgresContext;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class ASNLookup {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(ASNLookup.class));

    private static ASNLookup instance = new ASNLookup();

    NumberLookup asnList;

    // singleton
    private ASNLookup() {
        asnList = new NumberLookup("/opt/flow_score/data/data_sources/asn.txt");
    }

    public static ASNLookup getInstance() {
        return instance;
    }

    public boolean exists (Integer value) {
        return asnList.exists(value);
    }
}

