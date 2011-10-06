/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.freckler.extra;

import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 *
 * @author Stu Baurmann
 */
public class JMXUtils {
	// Useful for debugging what's going on with 
	public  static void dumpMBeanServerInfo(MBeanServerConnection mbsc) throws Throwable {
		// Get domains from MBeanServer
		echo("\nDomains:");
		String domains[] = mbsc.getDomains();
		for (int i = 0; i < domains.length; i++) {
			echo("\tDomain[" + i + "] = " + domains[i]);
		}
		echo("\nMBean count = " + mbsc.getMBeanCount());

		echo("\nQuery MBeanServer MBeans:");
		Set names = mbsc.queryNames(null, null);
		for (Iterator i = names.iterator(); i.hasNext(); ) {
			echo("\tObjectName = " + (ObjectName) i.next());
		}		
	}
    private static void echo(String msg) {
		System.out.println(msg);
    }	
}
