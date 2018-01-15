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
import org.apache.log4j.Logger;

public class TCPRemoteServiceLookup extends GenericServiceLookup {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(IPLookup.class));

    Table<String, Integer, Boolean> table = HashBasedTable.create();

    private static TCPRemoteServiceLookup instance = new TCPRemoteServiceLookup();

    // singleton
    private TCPRemoteServiceLookup() {
        super(6, false);
    }

    public static TCPRemoteServiceLookup getInstance() {
        return instance;
    }

}

