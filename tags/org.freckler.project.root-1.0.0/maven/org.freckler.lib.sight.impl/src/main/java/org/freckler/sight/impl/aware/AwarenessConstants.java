/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.sight.impl.aware;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class AwarenessConstants {
	public static final String T_INHIBIT_ATTENTION_CHANGE = "T_INHIBIT_ATTENTION_CHANGE";

	public static final String T_ATTENTION_CHANGE_NEEDED = "T_ATTENTION_CHANGE_NEEDED";
	public static final String T_ATTENTION_CHANGED = "T_ATTENTION_CHANGED";

	public static final String T_SHOULD_LOOK_FOR_FACES = "T_SHOULD_LOOK_FOR_FACES";
	public static final String T_LOOKING_FOR_FACES = "T_LOOKING_FOR_FACES";

	public static final String T_LOCAL_SEARCH_EXPECTED_FACE = "T_LOCAL_SEARCH_EXPECTED_FACE";

	public static final String T_PAUSED_GAZE = "T_PAUSED_GAZE";

	public static final String T_SHOULD_SAY_A_NAME = "T_SHOULD_SAY_A_NAME";
	public static final String T_PARTNER_GAZE_LOCKED = "T_PARTNER_GAZE_LOCKED";
	public static final String T_WAITING_TO_UNLOCK_PARTNER_GAZE = "T_WAITING_TO_UNLOCK_PARTNER_GAZE";

	public static final String T_GLANCE_ENDED = "T_GLANCE_ENDED";
	public static final String T_NAME_INTERACTION_ACTIVE = "T_NAME_INTERACTION_ACTIVE";
	public static final String T_ATTENTION_DISABLED = "T_ATTENTION_DISABLED";
	public static final String T_NO_ANIMATION = "T_NO_ANIMATION";

	public static final String T_ALL_ANIMS_DISABLED = "T_ALL_ANIMS_DISABLED";
	public static final String theAttentionThoughtStateNames[] = {
		T_INHIBIT_ATTENTION_CHANGE,
		T_ATTENTION_CHANGE_NEEDED,
		T_ATTENTION_CHANGED,
		T_SHOULD_LOOK_FOR_FACES,
		T_LOOKING_FOR_FACES,
		T_LOCAL_SEARCH_EXPECTED_FACE,
		T_PAUSED_GAZE,
		T_SHOULD_SAY_A_NAME,
		T_PARTNER_GAZE_LOCKED,
		T_WAITING_TO_UNLOCK_PARTNER_GAZE
	};


	public static final String VAR_PARTNER = "PARTNER";
	public static final String VAR_PERSON_COUNT = "PERSON_COUNT";
	public static final String VAR_NORMAL_GAZE_STRATEGY = "NORMAL_GAZE_STRATEGY";

	public static final String VAR_GAZE_PERSON_DESC = "GAZE_PERSON_DESC";
	public static final String DESC_BOGEY = "someone I dont recognize";
	public static final String DESC_NOBODY = "noone in particular";

	public static final String GS_NO_GAZE = "noGaze";
	public static final String GS_RAMPY_ALL_JOINTS = "rampyAllJoints";
	public static final String GS_RAMPY_RECENTER = "rampyRecenter";

	public static final String ANIM_REGEXP_GAZE_UNFIXED = ".*_gu_.*";
	public static final String ANIM_REGEXP_LOOK_FACES_WIDE = "look_faces_(left|right).*";
	public static final String ANIM_PREFIX_LOOK_FACES_LOCAL = "look_faces_locally";

	public static final String GESTURE_LOOK_FACES_WIDE = "LookForFaces_WIDE";
	public static final String GESTURE_LOOK_FACES_LOCAL = "LookForFaces_LOCAL";
}
