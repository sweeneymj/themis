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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NumberLookup { // TODO : rename

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(String.valueOf(IPLookup.class));

    List<Integer> list = new ArrayList<Integer>();

    /*
        load the databases from the file provided
     */
    public NumberLookup(String filename) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filename));
            String text = null;

            while ((text = reader.readLine()) != null) {
                //list.add(Integer.parseInt(text.substring(0,text.indexOf(',')+1)));
                list.add(Integer.parseInt(text));
            }
        } catch (FileNotFoundException e) {
            LOG.error("NumberLookup error", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOG.error("NumberLookup error", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

    }

    /*
        get a value from the database using a ip as a key, returns null if error
        or not found in the database
     */
    public boolean exists (Integer value) {
        return list.contains(value);
    }
}

