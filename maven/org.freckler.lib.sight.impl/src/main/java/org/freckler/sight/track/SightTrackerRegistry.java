/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.freckler.sight.track;

import org.cogchar.api.integroid.cue.SightCue;
import org.freckler.sight.impl.hypo.SightHypothesis;
import org.cogchar.sight.api.core.SightObservation;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @param <ST>
 * @param <SO>
 * @param <SH>
 * @param <SC> 
 * @author Stu B. <www.texpedient.com>
 */
public class SightTrackerRegistry<ST extends SightTracker<SO, SH, SC>, SO extends SightObservation, SH extends SightHypothesis<SO>, SC extends SightCue>  {
	private static Logger	theLogger = LoggerFactory.getLogger(SightTrackerRegistry.class.getName());
	private	Set<ST>	myTrackers = new HashSet<ST>();
	protected void registerTracker(ST pt) {
		myTrackers.add(pt);
	}
	protected void unregisterTracker(ST pt) {
		myTrackers.remove(pt);
	}
	public Set<ST> getAllTrackers() {
		return myTrackers;
	}
}
