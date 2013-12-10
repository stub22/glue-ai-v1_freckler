/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.service;

import org.cogchar.sight.api.facerec.FreckleResult;

/**
 *
 * @author humankind
 */
public interface FreckleResultListener {
	public void noticeFreckleResult(FreckleResult fres);
}
