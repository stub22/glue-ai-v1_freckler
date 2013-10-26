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

import org.friendularity.sight.api.core.SightExposureStatus;
import org.freckler.sight.impl.obs.SightObservationLog;
import org.friendularity.sight.api.core.SightObservation;
import java.util.List;
import org.friendularity.sight.api.freckle.FaceNoticeConfig;
import org.cogchar.api.animoid.protocol.EgocentricDirection;
import org.cogchar.platform.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <ObsType>
 * @author Stu B. <www.texpedient.com>
 */
public abstract class SightHypothesis<ObsType extends SightObservation>
			extends SightObservationLog<ObsType> {
	private static Logger theLogger = LoggerFactory.getLogger(SightHypothesis.class.getName());
	
	
	public enum ActivationStatus {
		DEAD,
		SUBLIMINAL,
		POSTED,
		RETIRED
	}
	private static List<ActivationStatus> myActiveStatusList;

	// TODO:  This static singleton pattern is kludgy.  Do not replicate!
	private		 static		FaceNoticeConfig	theFaceNoticeConfig;

	private static int			theNextHypothesisNumber = 1000;
	private double				myStrength;

	private Integer					myHypothesisNumber;
	private SightExposureStatus		myExposureStatus;
	private	ActivationStatus		myActivationStatus;
	
	private	SightModel			mySightModel;

	private Long				myFreckbaseHypoID;

	public abstract double computeAdditionalDistance(SightHypothesis sh);
	
	// Need to rename this SightModelConfig or somesuch
	public static void loadConfig(FaceNoticeConfig fnc) {
		theFaceNoticeConfig = fnc;
	}
	public static FaceNoticeConfig getFaceNoticeConfig() {
		return theFaceNoticeConfig;
	}
	public SightHypothesis(SightModel smod, ObsType initialObs) {
		super(initialObs);
		mySightModel = smod;
		myHypothesisNumber = theNextHypothesisNumber++;
		myActivationStatus = ActivationStatus.SUBLIMINAL;
		myExposureStatus = SightExposureStatus.EXPOSED;
		updateStrength(getFaceNoticeConfig().initialStrength);
	}
	protected SightModel getSightModel() {
		return mySightModel;
	}	
	public Integer getHypothesisNumber() {
		return myHypothesisNumber;
	}
	public String getUniqueID() {
		return "FH-" + this.getEarliestObservationTimeStamp() + "-" + myHypothesisNumber;
	}	
	public ActivationStatus getActivationStatus() {
		return myActivationStatus;
	}	
	public void setActivationStatus(ActivationStatus astat) {
		myActivationStatus = astat;
	}
	public static List<ActivationStatus> getActiveStatusList(){
		if(myActiveStatusList == null){
			myActiveStatusList = CollectionUtils.list(
				ActivationStatus.POSTED,
				ActivationStatus.SUBLIMINAL);
		}
		return myActiveStatusList;
	}
	protected void updateStrength(double upval) {
		if (upval > 1.0) {
			upval = 1.0;
		} else if (upval < 0.0) {
			upval = 0.0;
		}
		myStrength = upval;

		if ((myStrength <= getFaceNoticeConfig().survivalThreshold) && this.isActive()) {
			//retire();
			dieWithDignity();
		}

	}
	protected synchronized void retire(){
		myActivationStatus = ActivationStatus.RETIRED;
	}
	protected synchronized void dieWithDignity() {
		theLogger.debug("Hypo(" + getHypothesisNumber() + ") is dead");
		myActivationStatus = ActivationStatus.DEAD;
		cleanup();
	} 
	@Override protected synchronized void cleanup() {	
			// This is important so that the observation contents (the OpenCVImages) do not hang
		// around on the heap until this Hypothesis happens to get collected.  
		// If we forced periodic full-GC, then this shouldn't be necessary
		// (unless someone is holding onto the faceHypothesis somewhere unbenownst to us).

		// clearOldSightCue();
		mySightModel = null;
		super.cleanup();
	}

	public double getStrength() {
		return myStrength;
	}
	private void decayStrength(double elapsedSeconds) {
		double decayedStrength = SightModel.computeDecayedStrength(myStrength,
				elapsedSeconds, myExposureStatus);
		updateStrength(decayedStrength);
	}
/*
	// Point may be out of field of view, with negative or large coordinates.
	public Point getEstimatedCameraViewPoint(boolean enhancedAccuracy) {
		Point estCameraPoint = null;
		SightObservation fobs = getMostRecentObservation();
		if (fobs != null) {
			estCameraPoint = mySightModel.getObsCenterAdjustedScreenPoint(fobs, enhancedAccuracy);
		}
		return estCameraPoint;
	}	
*/
	protected synchronized void absorb(SightHypothesis inferior) {
		theLogger.debug("Hypo(" + getHypothesisNumber() + ") is absorbing Hypo(" +
				inferior.getHypothesisNumber() + ")");
		// inferior.clearOldSightCue();
		absorbInferiorLog(inferior);
		updateStrength (myStrength + inferior.getStrength());
	}	
	public synchronized void update(EgocentricDirection currentCenterDir) {
		if (!isActive()) {
			theLogger.debug("Ignoring update(), because I am inactive");
			return;
		}
		ObsType fobs = getMostAccurateObservation();
		myExposureStatus = mySightModel.exposureStatusForObs(fobs);
		double elapsedSec = secondsSinceUpdate();
		decayStrength(elapsedSec);
	}
	public SightExposureStatus getExposureStatus() {
		return myExposureStatus;
	}
	public boolean isActive(){
		return getActiveStatusList().contains(myActivationStatus);
	}

	@Override public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		final SightHypothesis<ObsType> other = (SightHypothesis<ObsType>) obj;
		return myHypothesisNumber.equals(other.myHypothesisNumber);
	}
	@Override public int hashCode() {
		return myHypothesisNumber.hashCode();
	}
	public void setFreckbaseHypoID(Long id) {
		myFreckbaseHypoID = id;
	}
	public Long getFreckbaseHypoID() {
		return myFreckbaseHypoID;
	}

}
