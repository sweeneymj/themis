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
package com.verifydynamics.netflowscoring.factory;

import com.verifydynamics.netflowscoring.enums.ServiceType;
import com.verifydynamics.netflowscoring.lookup.*;

public class ServiceLookupFactory {
    public static GenericServiceLookup getServiceLookup (ServiceType serviceType) {
        GenericServiceLookup serviceLookup = null;
        switch (serviceType) {
            case TCP_HOSTED:
                serviceLookup = TCPHostedServiceLookup.getInstance();
                break;

            case TCP_REMOTE:
                serviceLookup = TCPRemoteServiceLookup.getInstance();
                break;

            case UDP_HOSTED:
                serviceLookup = UDPHostedServiceLookup.getInstance();
                break;

            case UDP_REMOTE:
                serviceLookup = UDPRemoteServiceLookup.getInstance();
                break;
        }
        return serviceLookup;
    }
}
