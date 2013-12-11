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

import org.cogchar.api.animoid.gaze.IGazeTarget;
import org.cogchar.api.animoid.gaze.IGazeTarget.Flavor;
import org.cogchar.api.integroid.cue.SightCue;
import org.cogchar.api.animoid.protocol.Frame;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class SightTrackingTarget<SC extends SightCue> implements IGazeTarget {
	protected		SC				myCue;
	private			boolean			myAttentionFlag = false;

	@Override public Flavor getCurrentFlavor() {
		return Flavor.EGOCENTRIC_DIRECTION;
	}
	@Override  public Frame getEstimatedServoSnapshot() {
		throw new UnsupportedOperationException("Not supported.");
	}
	@Override public void notifyAttentionStarted() {
		myAttentionFlag = true;
		if (myCue != null) {
			myCue.notifyAttentionStarted();
		}
	}
	@Override  public void notifyAttentionConfirmed() {
		if (myCue != null) {
			myCue.notifyAttentionConfirmed();
		}
	}
	@Override public void notifyAttentionStopped() {
		myAttentionFlag = false;
		if (myCue != null) {
			myCue.notifyAttentionStopped();
		}
	}
	public SC getCue() {
		return myCue;
	}
	public void setCue(SC sc) {
		myCue = sc;
	}
	public Integer getCueSID() {
		SC meCue = getCue();
		Integer csid = null;
		if (meCue != null) {
			csid = meCue.fetchSessionCueID();
		}
		return csid;
	}
	public boolean getAttentionFlag() {
		return myAttentionFlag;
	}
}
