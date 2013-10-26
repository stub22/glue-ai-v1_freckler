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

import org.cogchar.api.integroid.cue.SightAttentionStatus;
import org.cogchar.api.integroid.cue.SightCue;
import org.friendularity.sight.api.core.SightExposureStatus;
import org.freckler.sight.impl.hypo.SightHypothesis;
import org.freckler.sight.impl.hypo.SightModel;
import org.friendularity.sight.api.core.SightObservation;
import org.cogchar.api.animoid.config.bonus.AnimoidConfig;
import org.cogchar.api.animoid.protocol.EgocentricDirection;
import org.cogchar.zzz.platform.stub.CueBrokerStub;
import org.cogchar.platform.util.TimeUtils;

/**
 * @param <SO>
 * @param <SH>
 * @param <SC>
 * @author Stu B. <www.texpedient.com>
 */
public abstract class SightTracker<SO extends SightObservation, SH extends SightHypothesis<SO>, SC extends SightCue> extends SightTrackingTarget<SC> {

	private		SH						myHypo;
	private		EgocentricDirection		myStoredDirection;
	private		SO						myStoredAccurateObs;

	private		boolean					myLockedFlag = false;

	// Exposure stamp is null if we are not currently exposed.
	private		Long					myExposureStampMsec;


	public SH getHypothesis() {
		return myHypo;
	}
	public void setHypothesis(SH sh) {
		if (sh == null) {
			if (myHypo != null) {
				// Save direction for use until we get a fresh hypo.
				myHypo.setActivationStatus(SightHypothesis.ActivationStatus.SUBLIMINAL);
			}
		}
		myHypo = sh;
	}
	protected void die(CueBrokerStub cb) {

		if (myCue != null) {
			cb.clearCue(myCue);
			myCue = null;
		}
		setHypothesis(null);
	}
	public boolean isDead() {
		return ((myCue == null) && (myHypo == null));
	}
	public void saveStuff() {
		if (myHypo != null) {
			EgocentricDirection hed = myHypo.getEstimatedDirection();
			SO obs = myHypo.getMostAccurateObservation();
			// TODO:  Confirm that hypo is not from a conflicting freckle?
			if (hed != null) {
				myStoredDirection = hed;
			}
			if (obs != null) {
				myStoredAccurateObs = obs;
			}
			// 	theLogger.info("Direction and Obs stored for: " + this);
		}
	}
	/* Could possibly derive this lockedFlag from the attentionStatus */
	public synchronized void setLockedFlag(boolean val) {
		myLockedFlag = val;
	}
	public boolean getLockedFlag() {
		return myLockedFlag;
	}
	public Integer getHypoNumber() {
		Integer fhn = null;
		if (myHypo != null) {
			fhn = myHypo.getHypothesisNumber();
		}
		return fhn;
	}

	@Override public EgocentricDirection getEstimatedDirection() {
		EgocentricDirection ed = null;
		if (myHypo != null) {
			ed = myHypo.getEstimatedDirection();
		} else {
			ed = myStoredDirection;
			// theLogger.info("Returning stored direction: " + ed);
		}
		return ed;
	}
	public SO getMostAccurateObservation() {
		SO obs = null;
		if (myHypo != null) {
			obs = myHypo.getMostAccurateObservation();
		} else {
			obs = myStoredAccurateObs;
		}
		return obs;
	}
	public SightExposureStatus getExposureStatus(SightModel sm) {
		if (myHypo != null) {
			return myHypo.getExposureStatus();
		} else if (myStoredAccurateObs != null) {
			return sm.exposureStatusForObs(myStoredAccurateObs);
		}
		return null;
	}
	public void propagateStrengthAndStatus(SightModel sm, AnimoidConfig aconf) {
		SightExposureStatus expoStat = getExposureStatus(sm);
		if ((expoStat == null) || (expoStat == SightExposureStatus.HIDDEN)) {
			myExposureStampMsec = null;
		} else if ((expoStat == SightExposureStatus.EXPOSED) && (myExposureStampMsec == null)) {
			myExposureStampMsec = TimeUtils.currentTimeMillis();
		}
		SH sh = getHypothesis();
		SC sc = getCue();
		if ((sc != null) && (sh != null)) {
			double hypoStrength = sh.getStrength();
			sc.setStrength(hypoStrength);
			sh.setActivationStatus(SightHypothesis.ActivationStatus.POSTED);
		} else if (sc != null) {
			// Need to decay cue (usually FriendCue) strength to adjust the eligibility
			double prevStrength = sc.getStrength();
			double elapsedSec = aconf.getSecondsPerFrame();
			double nextStrength = SightModel.computeDecayedStrength(prevStrength, elapsedSec, expoStat);
			sc.setStrength(nextStrength);
		}
	}
	public void clearAttentionStatus() {
		myExposureStampMsec = null;
		SightCue meCue = getCue();
		if (meCue != null) {
			meCue.setAttentionStatus(SightAttentionStatus.IGNORED);
		}
	}
	public String shortDesc() {
		return "cueID=" + getCueSID() + ", hypoNumber=" + getHypoNumber();
	}
	@Override public String toString() {
		return "SightTracker[cue=" + myCue + ", hypo=" + myHypo
			+ ", storedDirection=" + myStoredDirection + "]";
/*			if(ed.getAzimuth() != null){
				az = ed.getAzimuth().getDegreesText();}
			if(ed.getElevation() != null){
				el = ed.getElevation().getDegreesText();}
			lastSnapshot = String.valueOf(myLastGazeSnapshot.getSnapshotTimestamp());

 */
	}

	public Double getExposureAgeSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myExposureStampMsec);
	}
}
