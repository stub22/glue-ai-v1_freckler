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


package org.freckler.sight.impl.aware;

import org.cogchar.api.integroid.cue.PersonCue;
import java.util.List;
import org.cogchar.integroid.broker.IntegroidFacade;
import org.cogchar.integroid.broker.IntegroidHelpFuncs;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class AwarenessHelpFuncsUnused extends AwarenessHelpFuncs {
	
	public static PersonCue randomAttentionEligiblePersonWithExclusions(IntegroidFacade igf, PersonCue... exclusions) {
		// Not currently used (as of 19a - 2010-3-25)
		List<PersonCue> filteredPCs = getPersonCuesMinusExclusions(igf, exclusions);
		int eligiblePersonCount = filteredPCs.size();
		if (eligiblePersonCount >= 1) {
			int selectionIdx = IntegroidHelpFuncs.randomWholeNumber(igf, eligiblePersonCount);
			return filteredPCs.get(selectionIdx);
		} else {
			return null;
		}
	}
}
