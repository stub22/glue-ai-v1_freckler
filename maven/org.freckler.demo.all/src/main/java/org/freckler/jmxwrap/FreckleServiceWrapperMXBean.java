/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.jmxwrap;

import org.cogchar.sight.api.facerec.FreckleQuery;
import org.cogchar.sight.api.facerec.FreckleResult;

/**
 *
 * @author Stu Baurmann
 */
public interface FreckleServiceWrapperMXBean  {
	public static String	FRECKLER_JMX_OBJNAME = "org.freckler:server=europa";

	public static final String	ATTRIB_FRECKLE_RESULT = "queryResult";

	public Boolean submitAsyncQuery(FreckleQuery query);
	public FreckleResult syncQuery(FreckleQuery query, boolean notifyListeners);
	// public String[] getDefaultPopulationFreckleIDs();
}
