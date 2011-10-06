/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.jmxwrap;

import org.cogchar.freckler.protocol.FreckleQuery;
import org.cogchar.freckler.protocol.FreckleResult;

/**
 *
 * @author Stu Baurmann
 */
public interface FreckleServiceWrapperMXBean  {
	public static String	FRECKLER_JMX_OBJNAME = "com.hansonrobotics:freckler=jovial";

	public static final String	ATTRIB_FRECKLE_RESULT = "queryResult";

	public Boolean submitAsyncQuery(FreckleQuery query);
	public FreckleResult syncQuery(FreckleQuery query, boolean notifyListeners);
	// public String[] getDefaultPopulationFreckleIDs();
}
