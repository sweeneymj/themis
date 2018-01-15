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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
import com.maxmind.db.Reader;

import java.io.File;
import java.net.InetAddress;

/****************************************************************************
 * simple class for loading and lookup up values in custom mindmax IP
 * databases.
 ****************************************************************************/

public class IPLookup {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(IPLookup.class));

    private Reader reader;

    /*
        load the databases from the file provided
     */
    public IPLookup(String filename) {
        try {
            File database = new File(filename);
            reader = new Reader(database);
        } catch (Exception e) {
            LOG.error("IPLookup error", e);
            throw new RuntimeException(e);
        }
    }

    /*
        get a value from the database using a ip as a key, returns null if error
        or not found in the database
     */
    public String get (String ipAddress) {
        InetAddress inetAddress;
        JsonNode node;
        String value;
        try {
            inetAddress = InetAddress.getByName(ipAddress);
        } catch (Exception e) {
            return null;
        }
        try {
            node = reader.get(inetAddress);
        } catch (Exception e) {
            return null;
        }
		if (node == null) {
			return null;
		} else {
			value = node.path("note").asText();
			return value;
		}
    }
}
