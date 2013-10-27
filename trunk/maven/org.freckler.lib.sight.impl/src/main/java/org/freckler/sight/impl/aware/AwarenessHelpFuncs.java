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

import org.cogchar.zzz.api.platform.cues.ThoughtCue;
import org.cogchar.integroid.broker.IntegroidFacade;
import org.cogchar.animoid.broker.AnimoidFacade;
import org.freckler.sight.impl.hypo.SightModel;
import org.freckler.sight.impl.motion.PeakTracker;
// import org.cogchar.api.sight.SightCue;
import org.cogchar.api.integroid.cue.AwarenessCue;
import org.freckler.sight.impl.motion.MotionCue;
import org.cogchar.api.integroid.cue.PersonCue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.cogchar.api.integroid.cue.SightCue;
import org.freckler.sight.impl.hypo.AnimoidSightFacade;
import static org.freckler.sight.impl.aware.AwarenessConstants.*;
import org.freckler.sight.impl.motion.MotionCueBroker;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class AwarenessHelpFuncs {
	private static Logger	theLogger = Logger.getLogger(AwarenessHelpFuncs.class.getName());
	public static String STAR_LINE=	"******************************************************";
	public static String BAR_LINE=	"======================================================";
	public static PersonCue mostAttentionEligiblePerson(IntegroidFacade igf, double minEligibility, PersonCue... exclusions) {
		// Should this be done through the resolver instead?
		List<PersonCue> filteredPCs = getPersonCuesMinusExclusions(igf, exclusions);
		int eligiblePersonCount = filteredPCs.size();

		if (eligiblePersonCount >= 1) {
			PersonCue mostEligiblePC = null;
			double maxEligibScore = 0.0;
			for(PersonCue pc : filteredPCs) {
				double eScore = getPersonAttentionEligibilityScore(pc);
				if (eScore > maxEligibScore) {
					mostEligiblePC = pc;
					maxEligibScore = eScore;
				}
			}
			if (maxEligibScore >= minEligibility) {
				return mostEligiblePC;
			}
		} 
		return null;
	}
	public static double getPersonAttentionEligibilityScore(PersonCue pc) {
		// TODO:  Make configurable+tweakable
		double peakEligibilityStrength = 0.1;
		return pc.getAttentionEligibilityScore(peakEligibilityStrength);
	}
	public static String formattedAttentionEligibility(PersonCue pc) {
		if (pc != null) {
			return String.format("%8.6f", getPersonAttentionEligibilityScore(pc));
		} else {
			return "INELIG";
		}
	}
	public static List<PersonCue> getPersonCuesMinusExclusions(IntegroidFacade igf, PersonCue... exclusions) {
		// allPersonCues is a copy which it's OK to modify
		List<PersonCue> allPersonCues = igf.getCueBroker().getAllPersonCues();

		List<PersonCue> filteredPCs = new ArrayList<PersonCue>();
		for (PersonCue pc: allPersonCues) {
			filteredPCs.add(pc);
		}
		for (PersonCue epc: exclusions) {
			if (epc != null) {
				filteredPCs.remove(epc);
			}
		}
		return filteredPCs;
	}
	public static PersonCue findPersonForFreckleID(IntegroidFacade igf, String freckleID) {
		if (freckleID != null) {
			List<PersonCue> allPersonCues = igf.getCueBroker().getAllPersonCues();
			for (PersonCue pc: allPersonCues) {
				String fid = pc.getPermPersonID();
				if ((fid != null) && fid.equals(freckleID)) {
					return pc;
				}
			}
		}
		return null;
	}
	public static void logThoughtStates(IntegroidFacade igf, String thoughtNames[]) {
		StringBuffer sbuf = new StringBuffer();
		for (String tn : thoughtNames) {
			sbuf.append("[" + tn + "]=" + igf.getThoughtCue(tn) + "\n");
		}
		theLogger.finest("ThoughtStateDump:\n" + sbuf.toString());
	}
	public static void logVariableState(IntegroidFacade igf, String variableName) {
		theLogger.finest("[" + variableName + "]=" + igf.getVariableCue(variableName));
	}

	public static void logAttentionState(IntegroidFacade igf) {
		logThoughtStates(igf, theAttentionThoughtStateNames);
		logVariableState(igf, VAR_PARTNER);
		logVariableState(igf, VAR_NORMAL_GAZE_STRATEGY);
	}
	public static void setFixationPerson(IntegroidFacade igf, AwarenessCue ac, PersonCue pc) {
		// Propagation of IGazeTarget to AttentionJob is now handled by PersonResolver.
		PersonCue oldFP = ac.getFixationPerson();
		ac.setFixationPerson(pc);
		ThoughtCue pgl = igf.getThoughtCue(T_PARTNER_GAZE_LOCKED);
		if (pgl != null) {
			theLogger.finest(STAR_LINE + "\n" + STAR_LINE + "\n" + STAR_LINE
						+ "setFixationPerson called but pgl=" + pgl + "\n"
						+ STAR_LINE + "\n" + STAR_LINE + "\n" + STAR_LINE);
		} else {
			ThoughtCue inhibitAC = igf.getThoughtCue(T_INHIBIT_ATTENTION_CHANGE);
			theLogger.finest(BAR_LINE + "\n"
				+ "setFixationPerson called but inhibitAC=" + inhibitAC  + "\n"	+ BAR_LINE);
		}
		theLogger.finest("setFixationPerson[old=" + oldFP + ", new=" + pc + "]");
		// RuntimeException re = new RuntimeException("FixationPersonChange");
		// re.fillInStackTrace();
		// theLogger.log(Level.INFO, "Stacktrace: ", re);
		// logAttentionState(igf);

		/* This is done by the Resolver if/when the target is updated.
		if (oldFP != null) {
			Integer sessionCueID = oldFP.fetchSessionCueID();
			theLogger.info("Deactivating old fixation cue with sessionID=" + sessionCueID);
			oldFP.setAttentionStatus(SightAttentionStatus.IGNORED);
		}
		*/
	}
	public static void setPrimaryPerson(IntegroidFacade igf, AwarenessCue ac, PersonCue pc) {
		ac.setPrimaryPerson(pc);
	}
	public static void setGlanceSight(IntegroidFacade igf, AwarenessCue ac, SightCue sc) {
		ac.setGlanceSight(sc);
	}
	public static boolean isAwarenessFocusedOnPerson(IntegroidFacade igf, PersonCue pc) {
		AwarenessCue ac = igf.getCueBroker().getAwarenessCue();
		return ac.isFocusOnPerson(pc);
	}
	public static void transferFocus(IntegroidFacade igf, PersonCue prevFocusCue, PersonCue nextFocusCue) {
		AwarenessCue ac = igf.getCueBroker().getAwarenessCue();
		ac.transferFocus(prevFocusCue, nextFocusCue);
	}
	public static void clearFocus(IntegroidFacade igf, PersonCue prevFocusCue) {
		AwarenessCue ac = igf.getCueBroker().getAwarenessCue();
		ac.transferFocus(prevFocusCue, null);
	}
	public static SightModel getAnySightModel(IntegroidFacade igf) {
		AnimoidSightFacade asf = (AnimoidSightFacade) igf.getAnimoidFacade();
		if (asf != null) {
			return asf.getSightModel();
		} else {
			return null;
		}
	}
	public static MotionCue makeTheMotionCue(IntegroidFacade igf) {
		SightModel sm = getAnySightModel(igf);
		if (sm != null) {
			PeakTracker pt = new PeakTracker(sm);
			MotionCue mc = new MotionCue(pt);
			mc.setStrength(1.0);
			igf.getCueBroker().addCue(mc);
			return mc;
		} else {
			theLogger.warning("Can't make motionCue because sightModel is null");
			return null;
		}
	}
	public static MotionCue getTheMotionCue(IntegroidFacade igf) {
		return ((MotionCueBroker) igf.getCueBroker()).getMotionCue();
	}

	public static void logAware(String text) {
		theLogger.fine(text);
	}
}
