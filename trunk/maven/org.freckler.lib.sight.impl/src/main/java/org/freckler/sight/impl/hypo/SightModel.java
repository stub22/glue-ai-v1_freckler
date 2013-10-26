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
import org.friendularity.sight.api.core.SightObservation;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.cogchar.animoid.broker.Animator;
import org.cogchar.animoid.broker.AnimoidCueSpaceStub;

// import org.cogchar.animoid.calc.estimate.GazeDirectionComputer;
import org.cogchar.animoid.calc.estimate.PositionEstimator;
// import org.cogchar.api.animoid.config.bonus.AnimoidConfig;
// import org.cogchar.api.animoid.gaze.GazeJoint;
import org.friendularity.sight.api.core.SightPort;
import org.cogchar.api.animoid.protocol.EgocentricDirection;
import org.cogchar.api.animoid.protocol.Frame;
import org.friendularity.sight.api.core.SightDirectionComputer;
import org.cogchar.zzz.platform.stub.CueSpaceStub;
import org.cogchar.platform.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <HypoType>
 * @author Stu B. <www.texpedient.com>
 */
public abstract class SightModel<HypoType extends SightHypothesis> {

	private static Logger theLogger = LoggerFactory.getLogger(SightModel.class.getName());
		
	// protected		GazeDirectionComputer		myGazeDirectionComputer;
	protected		SightDirectionComputer		mySightDirectionComputer;
	protected		PositionEstimator			myPositionEstimator;
	private			CueSpaceStub					myAnimoidCueSpace;
	
	private		SightHypoRegistry<HypoType>			myRegistry;
	
	private		HypoChangeNotifier			myNotifier;

	private			Long						myPositionEstimateTimestamp;
	private			Frame						myJointPosSnapshot;

	public SightModel() {
		myRegistry = new SightHypoRegistry<HypoType>();
		myNotifier = new HypoChangeNotifier(); 
	}

	protected abstract void trimAndCollapseHypos();
	/*
	protected AnimoidCueSpaceStub getAnimoidCueSpace() {
		return myAnimoidCueSpace;
	}
	*/
	public void setAnimoidCueSpace(AnimoidCueSpaceStub acs) {
		myAnimoidCueSpace = acs;
	}	

	public SightDirectionComputer getGazeDirectionComputer() {
		return mySightDirectionComputer;
	}
	public void setGazeDirectionComputer(SightDirectionComputer gdc) {
		mySightDirectionComputer = gdc;
	}
	static int thePEsetCount = 0;
	public void setPositionEstimator(PositionEstimator pe) {
		theLogger.info("Got positionEstimator: " + pe);
		myPositionEstimator = pe;
		thePEsetCount++;
		if (thePEsetCount > 1) {
			// This double-setting was a result of a nasty thread-context bug
			// (C++ callbacks into java, classloader, ugh...)  that is now fixed.
			// The fix is in the form of the "blessThread()" methods.
			throw new RuntimeException("INSANITY DETECTED!!! Where is this being set from?");
		}
	}
	protected void recordPositionEstimate() {
		myPositionEstimateTimestamp = TimeUtils.currentTimeMillis();
		// theLogger.info("Recorded video frame timestamp: " + myPositionEstimateTimestamp);
		myJointPosSnapshot = getJointPosSnapForTimestamp(myPositionEstimateTimestamp, true);
	}
	private Frame getJointPosSnapForTimestamp(long stamp, boolean enhancedAccuracy) {
		Frame jointPosSnap = null;
		if (myPositionEstimator != null) {
			if (enhancedAccuracy) {			
				long correctionMsec =(long) (SightHypothesis.getFaceNoticeConfig().visionToMotionOffsetSec * 1000.0);
				long correctedStamp = stamp - correctionMsec;
				jointPosSnap = myPositionEstimator.estimatePositionAtMoment(correctedStamp);
			} else {
				jointPosSnap = myPositionEstimator.estimatePositionRoughly();
			}
		}
		return jointPosSnap;
	}
	public Point getObsCenterAdjustedScreenPoint(SightObservation fobs, boolean enhancedAccuracy) {
		Frame jointPosSnap = getJointPositionEstimateForCurrentVideoFrame(); //    getJointPosSnapNow(enhancedAccuracy);
		Point obsCenterNow = null; // getGazeDirectionComputer().computeTargetScreenPoint(jointPosSnap, fobs.getCenterDirection());
		return obsCenterNow;
	}
	public Long getTimestampForCurrentVideoFrame() {
		return myPositionEstimateTimestamp;
	}
	public Frame getJointPositionEstimateForCurrentVideoFrame() {
		return myJointPosSnapshot;
	}

	public void setAnimator(Animator a) {
		/*
		AnimoidConfig ac = a.getAnimoidConfig();
		SightPort vp = ac.getViewPort();
		List<GazeJoint> gjl = ac.getGazeJoints();
		GazeDirectionComputer gdc = new GazeDirectionComputer(vp, gjl);
		setGazeDirectionComputer(gdc);
		PositionEstimator pe = a.getPositionEstimator();
		setPositionEstimator(pe);		
		*/
	}	
	protected synchronized void collapseHyposUntilDone(double maxDistance) {
		if ((myRegistry != null) && (mySightDirectionComputer != null)) {
			while (myRegistry.attemptToCollapseOneHypo(maxDistance, mySightDirectionComputer.getViewPort())) {
				theLogger.trace("Collapsed one hypothesis, looking for more!");
			}
		}
	}

	protected synchronized void updateAllHyposAndPruneDead(EgocentricDirection currentCenterDir) {
		myRegistry.updateAllHyposAndPruneDead(currentCenterDir);
	}
	public HypoType getHypoForNumber(Integer num) {
		return myRegistry.getHypoForNumber(num);
	}
	public List<HypoType> getHypoSnapshotOrderedByNum() {
		return myRegistry.getHypoSnapshotOrderedByNum();
	}
	public void registerHypo(HypoType sh) {
		myRegistry.registerHypo(sh);
	}

	public Rectangle getFieldBoundsRect() {
		SightPort vp = getGazeDirectionComputer().getViewPort();
		Rectangle fieldBoundsRect = new Rectangle(1, 1, vp.getWidthPixels() - 2, vp.getHeightPixels() - 2);
		return fieldBoundsRect;
	}

	public Rectangle getObjectImageGrabRect(Rectangle detectedRect, boolean invertedVertical, double expansionRatio) {
		// Compute a "good" rectangle to use for a (face) image grab, based on the
		// detectedRect (which came from OpenCV).
		SightPort vp = getGazeDirectionComputer().getViewPort();
		Rectangle fieldBoundsRect = getFieldBoundsRect();
		Rectangle fixedRect = detectedRect;
		if (invertedVertical) {
			int detBottom = detectedRect.y + detectedRect.height;
			int invertedTop = vp.getHeightPixels() - detBottom;
			fixedRect = new Rectangle(detectedRect.x, invertedTop, detectedRect.width, detectedRect.height);
		}
		Rectangle expandedRect = new Rectangle(fixedRect);
		int expansionMarginH = (int) (expansionRatio * ((double) fixedRect.width));
		int expansionMarginV = (int) (expansionRatio * ((double) fixedRect.height));
		// Add expansionMargin pixels on each edge.
		expandedRect.grow(expansionMarginH, expansionMarginV);
		// Crop the expanded rectangle so that it doesn't exceed our viewport.
		Rectangle croppedRect = expandedRect.intersection(fieldBoundsRect);
		// theLogger.info("detectedRect=" + detectedRect + ", expandedAndCroppedRect=" + croppedRect);
		return croppedRect;
	}
	/*
	public abstract OldSightCue postSightCue(double strength);
	protected void clearSightCue(OldSightCue sc) {
		myAnimoidCueSpace.clearCue(sc);
	}
	 */
	public static class HypoChangeNotifier extends Observable {
		private long	myLastBroadcastTS = 0;
		public void maybeBroadcast() {
			long now = TimeUtils.currentTimeMillis();
			// TODO:  Make broadcast interval configurable.
			if ((now - myLastBroadcastTS) >= 300) {
				broadcast();
			}
		}
		private void broadcast() {
			setChanged();
			notifyObservers();
			myLastBroadcastTS = TimeUtils.currentTimeMillis();
		}
	}
	protected void maybeBroadcastNotifications() {
		myNotifier.maybeBroadcast();
	}
	public void registerObserver(Observer o) {
		myNotifier.addObserver(o);
	}
	public List<SightHypoComparison<HypoType>> getCachedComparisons() {
		return myRegistry.getCachedComparisons();
	}
	public synchronized void killHypothesis(HypoType h) {
		myRegistry.killHypothesis(h);
	}
	public SightExposureStatus exposureStatusForObs(SightObservation obs) {
		Point currentObsScreenPoint = getObsCenterAdjustedScreenPoint(obs, false);
		SightPort vp = getGazeDirectionComputer().getViewPort();
		if (vp.inBounds(currentObsScreenPoint)) {
			return SightExposureStatus.EXPOSED;
		} else {
			return SightExposureStatus.HIDDEN;
		}
	}
	public static double computeDecayedStrength(double origStrength, double elapsedSeconds,
				SightExposureStatus exposureStatus) {
		double decayConstant = 0.0;
		switch (exposureStatus) {
			case EXPOSED:	decayConstant = SightHypothesis.getFaceNoticeConfig().exposedDecayConstant;
			break;
			case HIDDEN:	decayConstant = SightHypothesis.getFaceNoticeConfig().hiddenDecayConstant;
			break;
		}
		return origStrength * Math.exp(elapsedSeconds * decayConstant);
	}
}
