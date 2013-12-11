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


package org.freckler.sight.impl.hypo;

import org.cogchar.sight.api.core.SightPort;
import org.cogchar.sight.api.core.SightObservation;

import org.freckler.sight.impl.obs.SightObservationComparison;



/**
 *
 * @param <HypoType> 
 * @author Stu B. <www.texpedient.com>
 */
public class SightHypoComparison<HypoType extends SightHypothesis> {
	public HypoType	 lowerHypo;
	public HypoType upperHypo;
	private	 SightObservationComparison		obsComp;
	public double	myAdditionalDistance = 0.0;
	public int		myOverlapCount = -99;

	public SightHypoComparison(SightHypoRegistry<HypoType> registry, Integer lowerHypoNumber, Integer upperHypoNumber, SightPort vp) {
		lowerHypo = registry.getHypoForNumber(lowerHypoNumber);
		upperHypo = registry.getHypoForNumber(upperHypoNumber);
		// Should we use MostAccurateObs instead?
		SightObservation lowerObs = lowerHypo.getMostAccurateObservation();
		SightObservation upperObs = upperHypo.getMostAccurateObservation();
		obsComp = new SightObservationComparison(lowerObs, upperObs, vp);
		myOverlapCount = upperHypo.countOverlappingTimestamps(lowerHypo);
		// Add type-specific distance factors (e.g. freckle-matches, significance of timestamp overlaps, etc).
		myAdditionalDistance = lowerHypo.computeAdditionalDistance(upperHypo);
	}


	public String toString() {
		return "SightHypoComp[lower=" + lowerHypo.getHypothesisNumber() + ", upper=" + upperHypo.getHypothesisNumber() + ", cogDistance=" + getCognitiveDistance() + "]";
	}
	public double getCognitiveDistance() {
		return obsComp.distance + myAdditionalDistance 
				+ myOverlapCount * SightHypothesis.getFaceNoticeConfig().cogDistCoeffTimestampOverlap;
	}
	public SightObservationComparison getObservationComparison() {
		return obsComp;
	}
}
