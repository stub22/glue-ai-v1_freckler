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
import org.cogchar.api.animoid.protocol.EgocentricDirection;
import org.cogchar.api.animoid.protocol.Frame;

/**
 *
 * @author Stu B.  <www.texpedient.com>
 */
public interface IGazeTarget {
	public enum Flavor {
		EGOCENTRIC_DIRECTION,
		SERVO_SNAPSHOT
	}
	public Flavor getCurrentFlavor();
	public EgocentricDirection getEstimatedDirection();
	public Frame getEstimatedServoSnapshot();
	// 0 = parallel, +x = converged, -x = diverged
	public Double getVergenceAngle(Double defaultWidth, Double slope);
	public void notifyAttentionStarted();
	public void notifyAttentionConfirmed();
	public void notifyAttentionStopped();
}
