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

package org.freckler.sight.impl.motion;

import org.freckler.sight.impl.hypo.SightModel;
import org.freckler.sight.track.SightTrackingTarget;
import java.awt.Point;
// import org.cogchar.animoid.calc.estimate.GazeDirectionComputer;

import org.cogchar.api.animoid.protocol.EgocentricDirection;
import org.cogchar.api.animoid.protocol.Frame;
import org.cogchar.api.integroid.cue.SightCue;

import org.cogchar.sight.api.core.SightPort;
import org.cogchar.sight.api.core.SightDirectionComputer;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class PeakTracker extends SightTrackingTarget<SightCue> {

	private		SightModel			myModelForEstimates;
	private		Point				myPeakScreenPoint;
	private		EgocentricDirection	myPeakDirection;

	public PeakTracker(SightModel modelForEstimates) {
		myModelForEstimates = modelForEstimates;
	}
	public Point getCameraCenterPixel() {
		SightDirectionComputer gdc = myModelForEstimates.getGazeDirectionComputer();
		SightPort vp = gdc.getViewPort();
		return vp.getCameraCenterPixel();
	}
	public int getCameraPixelArea() {
		SightDirectionComputer gdc = myModelForEstimates.getGazeDirectionComputer();
		SightPort vp = gdc.getViewPort();
		int width = vp.getWidthPixels();
		int height = vp.getHeightPixels();
		return width * height;
	}
	public void noticeCameraPeakPixel(Point cameraPixel) {
		Frame cameraCenterPosEstimate = myModelForEstimates.getJointPositionEstimateForCurrentVideoFrame(); // getJointPosSnapNow(true);
		SightDirectionComputer gdc = myModelForEstimates.getGazeDirectionComputer();
		if ((cameraCenterPosEstimate != null) && (gdc != null)) {
			// myPeakDirection = gdc.computeGazeDirection(cameraCenterPosEstimate, cameraPixel);
		}
		// Could do something like this to record estimated gaze speed at time of obs,
		// to use in downplaying the value of motion.
		// Could also try to do some kind of vergence calc.
		// TargetObjectStateEstimate tose = new TargetObjectStateEstimate(myPositionEstimator, gdc, r, timestamp);
	}

	@Override public Flavor getCurrentFlavor() {
		return Flavor.EGOCENTRIC_DIRECTION;
	}
	@Override  public EgocentricDirection getEstimatedDirection() {
		return myPeakDirection;
	}

	@Override  public Double getVergenceAngle(Double defaultWidth, Double slope) {
		return new Double(3.0);
	}


}
