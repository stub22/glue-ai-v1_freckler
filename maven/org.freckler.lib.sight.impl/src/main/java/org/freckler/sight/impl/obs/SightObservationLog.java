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

import org.friendularity.sight.api.core.SightObservation;
import org.freckler.sight.impl.hypo.SightHypothesis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

// import org.cogchar.animoid.calc.estimate.TargetObjectStateEstimate;
import org.friendularity.sight.api.freckle.FaceNoticeConfig;
import org.cogchar.api.animoid.protocol.EgocentricDirection;
import org.friendularity.sight.api.core.SightRelatedStateEstimate;
import org.cogchar.platform.util.CollectionFilter;
import org.cogchar.platform.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <ObsType>
 * @author Stu B. <www.texpedient.com>
 */
public abstract class SightObservationLog<ObsType extends SightObservation>  { // extends PropertyChangeNotifier {
	private static Logger theLogger = LoggerFactory.getLogger(SightObservationLog.class.getName());

	private int							myPrunedObservationCount = 0;
	private long						myFirstTimeStampMsec;

	// This collection is not strictly guarded by insert/remove methods, because
	// we want to do removal from iterators.  That's preventing us from caching
	// bestEstimate and some other values, so, might be better.
	private TreeMap<Long, ObsType> myObservations = new TreeMap<Long, ObsType>();

	public SightObservationLog(ObsType initialObs) {
		insertObservation(initialObs);		
		myFirstTimeStampMsec = initialObs.getTimeStampMsec();
	}
	public Long getEarliestObservationTimeStamp() {	
		return myFirstTimeStampMsec;
	}
	public Long getLatestObservationTimeStamp() {
		ObsType mro = getMostRecentObservation();
		if (mro != null) {
			return mro.getTimeStampMsec();
		} else {
			return null;
		}
	}

	public synchronized ObsType getMostRecentObservation() {
		ObsType mro = null;
		if (myObservations != null) {
			mro = myObservations.lastEntry().getValue();
		}
		return mro;
	}

	public ObsType getObservationForTimestamp(Long ts) {
		return myObservations.get(ts);
	}

	public int getRetainedObservationCount() {
		return myObservations.size();
	}

	public int getTotalObservationCount() {
		return myPrunedObservationCount + getRetainedObservationCount();
	}

	public synchronized boolean insertObservation(ObsType insertMe) {
		Long insertTS = insertMe.getTimeStampMsec();
		ObsType existingObs = getObservationForTimestamp(insertTS);
		if (existingObs != null) {
			theLogger.warn("Ignoring request to insert obs at timestamp " + insertTS
						+ " because we already have an obs there.");
			return false;
		}
		myObservations.put(insertTS, insertMe);
		return true;
	}
	protected synchronized void absorbInferiorLog(SightObservationLog<ObsType> inferior) {
		// All your obs are belong to me, inferior!
		// Obs at colliding timestamps are ignored, with a warning log message.
		for (ObsType infObs : inferior.myObservations.values()) {
			insertObservation(infObs);
		}
	}
	public NavigableSet<Long> getNavigableRecentTimestamps() {
		if (myObservations == null) {
			return null;
		} else {
			return myObservations.descendingKeySet();
		}
	}
	public Collection<ObsType> getRecentObservations() {
		Collection<ObsType> result = null;
		if (myObservations != null) {
			NavigableMap<Long,ObsType> recentNM = myObservations.descendingMap();
			result = recentNM.values();
		}
		return result;
	}

	protected synchronized void cleanup() {	
		// This is important so that the observation contents (the OpenCVImages) do not hang
		// around on the heap until this Hypothesis happens to get collected.  
		// If we forced periodic full-GC, then this shouldn't be necessary
		// (unless someone is holding onto the faceHypothesis somewhere unbenownst to us).
		myObservations = null;
	}
	public synchronized int pruneOldObservations(int obsToKeep, Set<Long> immuneTimestamps) {
		int pruneCount = 0;
		int keptCount = 0;

		NavigableSet<Long> countDownSet = this.getNavigableRecentTimestamps();
		Iterator<Long> countDownIt = countDownSet.iterator();
		while ((keptCount < obsToKeep) && (countDownIt.hasNext())) {
			countDownIt.next();
			keptCount++;
		}
		while (countDownIt.hasNext()) {
			Long pruneMe = countDownIt.next();
			if ((immuneTimestamps == null) || (!immuneTimestamps.contains(pruneMe))) {
				ObsType prunedObs = getObservationForTimestamp(pruneMe);
				adjustStatisticsForPrunedObs(prunedObs);
				countDownIt.remove();
				pruneCount++;
			}
		}
		return pruneCount;
	}
	protected void adjustStatisticsForPrunedObs(ObsType pruned) {
		myPrunedObservationCount++;
	}

	public void  collectRetainedObservationsMatchingPredicate(Collection<ObsType> tgt,
				CollectionFilter.Predicate<ObsType> pred) {
		Collection<ObsType> recentObs = getRecentObservations();
		CollectionFilter.filter(recentObs, tgt, pred);
	}
	public synchronized void  collectRetainedTimestampsWhereObsMatchesPredicate(Collection<Long> tgt,
				final CollectionFilter.Predicate<ObsType> pred) {
		NavigableSet<Long>	recentStamps = getNavigableRecentTimestamps();
		CollectionFilter.filter(recentStamps, tgt, new CollectionFilter.Predicate<Long>() {
			public boolean test(Long stamp) {
				ObsType obs = getObservationForTimestamp(stamp);
				return pred.test(obs);
			}
		});
	}
	public synchronized int  countRetainedTimestampsWhereObsMatchesPredicate(
				final CollectionFilter.Predicate<ObsType> pred) {
		Set<Long> matchingStamps = new HashSet<Long>();
		collectRetainedTimestampsWhereObsMatchesPredicate(matchingStamps, pred);
		return matchingStamps.size();
	}
	public synchronized ObsType getMostRecentObsMatchingPredicate(final CollectionFilter.Predicate<ObsType> pred) {
		if (myObservations != null) {
			NavigableMap<Long, ObsType> recentObsMap = myObservations.descendingMap();
			for (ObsType obs : recentObsMap.values()) {
				if (pred.test(obs)) {
					return obs;
				}
			}
		}
		return null;
	}
	public synchronized int pruneOldObservations(int obsToKeep,
				CollectionFilter.Predicate<ObsType> immunityPred) {
		Set<Long> immuneStamps = new HashSet<Long>();
		collectRetainedTimestampsWhereObsMatchesPredicate(immuneStamps, immunityPred);
		return pruneOldObservations(obsToKeep, immuneStamps);
	}
	protected double secondsSinceUpdate() {
		long nowMillis = TimeUtils.currentTimeMillis();
		long elapsedMillis = nowMillis - this.getLatestObservationTimeStamp();
		double elapsedSec = ((double) elapsedMillis) / 1000.0;
		return elapsedSec;
	}
	/*
	public SimpleGazeTarget getSimpleGazeTarget(){
		return new SimpleGazeTarget(getEstimatedDirection());
	}
	 */

	public int countOverlappingTimestamps(SightObservationLog<ObsType> other) {
		int count = 0;
		if ((myObservations != null) && (other != null) && (other.myObservations != null)) {
			for (ObsType fobs: myObservations.values()) {
				long ts = fobs.getTimeStampMsec();
				if (other.myObservations.get(ts) != null) {
					count++;
				}
			}
		}
		return count;
	}		

	public EgocentricDirection getEstimatedDirection() {
		// Could add some uncerainty measures to the EgoDir, proportional to time since obs.
		EgocentricDirection estDir = null;
		SightRelatedStateEstimate tose = getMostCertainTOSE();
		if (tose != null) {
			estDir = tose.myTargetDirectionAtObs;
		}
		return estDir;
		// Old way:
		// SightObservation sob = getMostRecentObservation();
		// if (sob != null) {
		//	estDir = sob.getCenterDirection();
	}
	/*
	public JointPositionSnapshot getEstimatedGazeSnapshot() {
		JointPositionSnapshot estJps = null;
		TargetObjectStateEstimate tose = getMostCertainTOSE();
		if (tose != null && tose.jointPosAtObs != null) {
			// Note - this uses hardcoded muscle joint IDs!
			estJps = JointPositionSnapshot.getGazeSnapshot(tose.jointPosAtObs);
		}
		return estJps;
	}
	 */
	public synchronized SightRelatedStateEstimate getMostCertainTOSE() {
		// TODO:  This can be optimized, so that we don't check old
		// obs that are obviously less good.  To do this, we can figure
		// the oldest an est could be that would overcome bestEst, and
		// stop searching when we get that far back.  This approach will
		// be cleaner than caching and updating the cache, especially
		// when we absorb inferiors.
		SightRelatedStateEstimate	bestEst = null;
		NavigableSet<Long> recentStampNavSet = getNavigableRecentTimestamps();
		if (recentStampNavSet != null) {
			Iterator<Long> recentStampIt = recentStampNavSet.iterator();
			while (recentStampIt.hasNext()) {
				Long	stamp = recentStampIt.next();
				ObsType sob = getObservationForTimestamp(stamp);
				SightRelatedStateEstimate tose = sob.myTOSE;
				if (bestEst == null) {
					bestEst = tose;
				} else {
					FaceNoticeConfig sightModelConfig = SightHypothesis.getFaceNoticeConfig();
					if (tose.isBetterThan(bestEst, sightModelConfig)) {
						bestEst = tose;
					}
				}
			}
		}
		return bestEst;
	}
	public ObsType getMostAccurateObservation() {
		ObsType resultObs = null;
		SightRelatedStateEstimate tose = getMostCertainTOSE();
		if (tose != null) {
			resultObs = (ObsType) tose.myTempHackedSightObservation;
		}
		// Doublechecking some weirdness
		ObsType mostRecent = this.getMostRecentObservation();
		if (resultObs != mostRecent) {
			theLogger.trace("Most recent obs is not most accurate.  mostRecentTOSE="
					+ mostRecent.myTOSE + ", mostAccurateTOSE=" + tose +
					", fnconfig=" + SightHypothesis.getFaceNoticeConfig());
		}
		// End doublecheck
		return resultObs;
	}
	protected NavigableMap<Long, ObsType> getObsNewerThanStamp(Long ts, boolean inclusive) {
		if (myObservations != null) {
			return myObservations.tailMap(ts, inclusive);
		} else {
			return null;
		}
	}
}
