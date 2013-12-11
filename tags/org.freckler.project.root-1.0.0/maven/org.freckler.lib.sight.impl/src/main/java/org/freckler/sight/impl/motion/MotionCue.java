/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.sight.impl.motion;

import org.cogchar.api.integroid.cue.SightCue;
import org.freckler.sight.impl.motion.PeakTracker;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class MotionCue extends SightCue {
	transient	PeakTracker		myTracker;
	public MotionCue(PeakTracker pt) {
		myTracker = pt;
		pt.setCue(this);
	}
	public PeakTracker fetchPeakTracker() {
		return myTracker;
	}
}
