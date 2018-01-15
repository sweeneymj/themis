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

import org.apache.log4j.Logger;

public class ValidProtocolLookup {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(ValidProtocolLookup.class));

    private static ValidProtocolLookup instance = new ValidProtocolLookup();

    NumberLookup protocolsList;

    // singleton
    private ValidProtocolLookup() {
        protocolsList = new NumberLookup("/opt/flow_score/data/data_sources/valid_protocols.txt");
    }

    public static ValidProtocolLookup getInstance() {
        return instance;
    }

    public boolean exists (Integer value) {
        return protocolsList.exists(value);
    }
}

