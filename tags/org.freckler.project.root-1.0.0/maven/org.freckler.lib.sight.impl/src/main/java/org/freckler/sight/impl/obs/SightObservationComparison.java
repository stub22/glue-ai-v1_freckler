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

import org.cogchar.api.animoid.protocol.EgocentricDirection;

import org.cogchar.sight.api.core.SightObservation;
import org.cogchar.sight.api.facerec.FaceNoticeConfig;
import org.cogchar.sight.api.core.SightPort;
import org.freckler.sight.impl.hypo.SightHypothesis;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class SightObservationComparison {
	public double distance;
	public double timeDiffSec;
	public EgocentricDirection diffDir;
	public double azDiffDiams;
	public double elDiffDiams;

	public SightObservationComparison(SightObservation one, SightObservation two, SightPort vp) {

		timeDiffSec = ((double) Math.abs(one.getTimeStampMsec() - two.getTimeStampMsec())) / 1000.0;
		EgocentricDirection dir1 = one.getCenterDirection();
		EgocentricDirection dir2 = two.getCenterDirection();
		EgocentricDirection diffDir = dir1.subtract(dir2);
		double azDiffDeg = Math.abs(diffDir.getAzimuth().getDegrees());
		double elDiffDeg = Math.abs(diffDir.getElevation().getDegrees());

		double dd1 = one.getDiameterDeg(vp);
		double dd2 = two.getDiameterDeg(vp);
		double gmdd = Math.sqrt(dd1 * dd2); // geometric mean diameter degrees

		azDiffDiams = azDiffDeg / gmdd;
		elDiffDiams = elDiffDeg / gmdd;

		FaceNoticeConfig fnc = SightHypothesis.getFaceNoticeConfig();

		double termAzSquared = fnc.cogDistCoeffAzDiamSquared * azDiffDiams * azDiffDiams;
		double termElSquared = fnc.cogDistCoeffElDiamSquared * elDiffDiams * elDiffDiams;
		double termSeconds =   fnc.cogDistCoeffSeconds * timeDiffSec;
		// double termProduct = fnc.cogDistCoeffProduct * placeDiffFactor * timeDiffSec;
		distance = termAzSquared + termElSquared + termSeconds;
	}
}
