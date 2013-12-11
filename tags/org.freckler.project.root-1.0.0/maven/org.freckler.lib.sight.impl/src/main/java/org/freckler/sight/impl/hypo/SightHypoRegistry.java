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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.cogchar.sight.api.core.SightPort;
import org.cogchar.api.animoid.protocol.EgocentricDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <HypoType>
 * @author Stu B. <www.texpedient.com>
 */
public class SightHypoRegistry<HypoType extends SightHypothesis> {
	private static Logger theLogger = LoggerFactory.getLogger(SightHypoRegistry.class.getName());

	private TreeMap<Integer, HypoType> myHypothesesByNumber = new TreeMap<Integer, HypoType>();

	private	List<HypoType>				myCachedHyposByNum = null;

	private List<SightHypoComparison<HypoType>>	myCachedComparisons;

	public synchronized HypoType getHypoForNumber(Integer number) {
		return myHypothesesByNumber.get(number);
	}
	
	protected synchronized void registerHypo(HypoType hypo) {
		Integer hypoNumber = hypo.getHypothesisNumber();
		myHypothesesByNumber.put(hypoNumber, hypo);
		myCachedHyposByNum = null;
	}	
	protected synchronized void removeKnownDeadHypo(HypoType sh) {
		myHypothesesByNumber.remove(sh.getHypothesisNumber());
		myCachedHyposByNum = null;		
	}	
	
	public synchronized List<HypoType> getHypoSnapshotOrderedByNum() {
		if (myCachedHyposByNum == null) {
			Collection sharedValues = myHypothesesByNumber.values();
			myCachedHyposByNum = new ArrayList<HypoType>(sharedValues);
		}
		return myCachedHyposByNum;
	}
	public synchronized Collection<HypoType> getAllHypotheses() {
		return getHypoSnapshotOrderedByNum();
	}

	protected synchronized void killHypothesis(HypoType sh) {
		removeKnownDeadHypo(sh);
		sh.dieWithDignity();
	}
	protected synchronized void removeKnownDeadHypos(Collection<HypoType> deadHypos) {
		for (HypoType dsh: deadHypos) {
			removeKnownDeadHypo(dsh);
		}
	}	
	protected synchronized void findAndPruneDeadHypos() {
		List<HypoType> deadHyposToRemove = new ArrayList<HypoType>();
		for (HypoType dsh: myHypothesesByNumber.values()) {
			if (dsh.getActivationStatus() == SightHypothesis.ActivationStatus.DEAD) {
				deadHyposToRemove.add(dsh);
			}
		}
		removeKnownDeadHypos(deadHyposToRemove);
	}		
	protected synchronized boolean attemptToCollapseOneHypo(double maxDistance, SightPort vp) {
		Set<Integer> hypoNumbers = new HashSet<Integer>(myHypothesesByNumber.keySet());
		List<Integer> sortedHypoNumbers = new ArrayList<Integer>(hypoNumbers);
		Collections.sort(sortedHypoNumbers);
		SightHypoComparison<HypoType> nearestHC = null;
		myCachedComparisons = new ArrayList<SightHypoComparison<HypoType>>();
		// Find the pair of hypos with the shortest cognitive distance.
		// TODO: We probably need to go further, and consider the size of this hypo
		// distance distance relative to other current distances (and other metrics,
		// such as average faces-per-frame on recent frames), to determine a likelihood
		// that the hypos should be collapsed.
		for (Integer hn: sortedHypoNumbers) {
			hypoNumbers.remove(hn);
			for (Integer ohn: hypoNumbers) {
				SightHypoComparison<HypoType> hc = new SightHypoComparison<HypoType>(this, hn, ohn, vp);
				myCachedComparisons.add(hc);
				if ((nearestHC == null) || (hc.getCognitiveDistance() < nearestHC.getCognitiveDistance())) {
					// Only hypos which do not share any timestamps are eligible for collapse.
						nearestHC = hc;
				}
			}
		}
		theLogger.debug("Nearest comparison is: " + nearestHC);
		if (nearestHC != null) {
			if (nearestHC.getCognitiveDistance() < maxDistance) {
				// The older hypothesis (lowerHypo) always absorbs the newer one.
				nearestHC.lowerHypo.absorb(nearestHC.upperHypo);
				killHypothesis(nearestHC.upperHypo);
				// beansBinding is not robust enough to handle this
				// myObservableHypotheses.remove(nearestHC.upperHypo);
				return true;
			}
		} 
		return false;
	}
	public synchronized List<SightHypoComparison<HypoType>> getCachedComparisons() {
		return myCachedComparisons;
	}
	protected void updateAllHyposAndPruneDead(EgocentricDirection currentCenterDir) {
		// HaHaHa - "shit"!  Should we call it Shiterator?
		Iterator<HypoType> shit =  myHypothesesByNumber.values().iterator();
		while (shit.hasNext()) {
			HypoType sh = shit.next();
			// If this update() tries to delete the hypothesis, we get a concurrent-mod exception.
			// So we allow deletion to happen in findAndPrune below.
			sh.update(currentCenterDir);
		}
		findAndPruneDeadHypos();
	}
}
