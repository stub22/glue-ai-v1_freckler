/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freckler.sight.impl.motion;

import org.cogchar.integroid.broker.IntegroidCueBroker;

/**
 *
 * @author Stu B22 <stub22@appstract.com>
 */
public class MotionCueBroker extends IntegroidCueBroker {
	public MotionCue getMotionCue() {
        try{
            return getSingleFactMatchingClass(MotionCue.class);
        } catch(Throwable t) {
			// theLogger.warning("Can't find MotionCue");
		}
        return null;
	}	
}
