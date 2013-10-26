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
package org.freckler.sight.impl.obs;

import java.util.List;
import org.friendularity.sight.api.core.SightObservation;

/**
 * Tracks observations, combining observations of the same target into a single
 * Tracker.
 * 
 * For example, a face tracker would get collect face observation objects and 
 * combine observations of the same face into a Face Tracker object/
 * 
 * @param <ObsType> Type of observation to be tracked
 * @param <TrackerType> Type of tracker to track a target across multiple 
 * observations
 * @author Matthew Stevenson
 */
public interface ObservationTrackerRegistry<ObsType extends SightObservation, TrackerType> {
    /**
     * Adds an observation and returns a new or existing Tracker which the
     * observation has been added to.
     * @param obs observation to add
     * @return new or existing Tracker which the observation has been added to
     */
    public TrackerType addObservation(ObsType obs);
    /**
     * Returns all active trackers in the registry.
     * @return all active trackers in the registry
     */
    public List<TrackerType> getObservationTrackers();
}
