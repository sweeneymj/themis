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
package com.verifydynamics.netflowscoring;

import com.verifydynamics.netflowscoring.enums.ServiceType;
import com.verifydynamics.netflowscoring.factory.ServiceLookupFactory;
import com.verifydynamics.netflowscoring.lookup.GenericServiceLookup;
import com.verifydynamics.netflowscoring.lookup.TCPHostedServiceLookup;
import com.verifydynamics.netflowscoring.lookup.UDPHostedServiceLookup;
import org.apache.log4j.Logger;
import org.junit.Test;

public class PostgresLookup {

    private static final Logger LOG = Logger.getLogger(PostgresLookup.class);

    @Test
    public void testGenericServiceLookup () {
        GenericServiceLookup tcpServiceLookup = new GenericServiceLookup(6, false);

        System.out.println(tcpServiceLookup.exists("196.21.242.189", 443));
        System.out.println(tcpServiceLookup.exists("196.21.242.189", 10));
        System.out.println(tcpServiceLookup.exists("196.21.242.1", 443));

        GenericServiceLookup udpServiceLookup = new GenericServiceLookup(17, true);

        System.out.println(udpServiceLookup.exists("196.21.242.1", 443));
        System.out.println(udpServiceLookup.exists("196.21.242.130", 53));
    }

    @Test
    public void testTCPServiceLookup () {
        TCPHostedServiceLookup serviceLookup = TCPHostedServiceLookup.getInstance();

        System.out.println(serviceLookup.exists("196.21.242.189", 443));
        System.out.println(serviceLookup.exists("196.21.242.189", 10));
        System.out.println(serviceLookup.exists("196.21.242.1", 443));
    }

    @Test
         public void testUDPServiceLookup () {
        UDPHostedServiceLookup serviceLookup = UDPHostedServiceLookup.getInstance();

        System.out.println(serviceLookup.exists("196.21.242.189", 443));
        System.out.println(serviceLookup.exists("196.21.242.189", 10));
        System.out.println(serviceLookup.exists("196.21.242.130", 53));
    }

    @Test
    public void testClassServiceLookup () {
        GenericServiceLookup serviceLookup;

        serviceLookup = UDPHostedServiceLookup.getInstance();

        System.out.println(serviceLookup.exists("196.21.242.189", 443));
        System.out.println(serviceLookup.exists("196.21.242.189", 10));
        System.out.println(serviceLookup.exists("196.21.242.130", 53));
    }

    @Test
    public void testClassServiceFactory () {

        GenericServiceLookup serviceLookup = ServiceLookupFactory.getServiceLookup(ServiceType.UDP_HOSTED);
        System.out.println(serviceLookup.exists("196.21.242.189", 443));
        System.out.println(serviceLookup.exists("196.21.242.76", 53));
        System.out.println(serviceLookup.exists("196.21.242.130", 53));
    }

}
