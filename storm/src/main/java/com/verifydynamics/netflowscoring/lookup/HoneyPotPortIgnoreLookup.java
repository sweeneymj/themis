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

public class HoneyPotPortIgnoreLookup {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(HoneyPotPortIgnoreLookup.class));

    private static HoneyPotPortIgnoreLookup instance = new HoneyPotPortIgnoreLookup();

    NumberLookup honeyPortList;

    // singleton
    private HoneyPotPortIgnoreLookup() {
        // TODO : use config or params to genericise
        honeyPortList = new NumberLookup("/opt/flow_score/data/data_sources/ignore_honeyport_ports.txt");
    }

    public static HoneyPotPortIgnoreLookup getInstance() {
        return instance;
    }

    public boolean exists (Integer value) {
        return honeyPortList.exists(value);
    }
}

