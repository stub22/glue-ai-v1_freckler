/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.service;

import org.cogchar.freckler.protocol.FreckleResult;

/**
 *
 * @author humankind
 */
public interface FreckleResultListener {
	public void noticeFreckleResult(FreckleResult fres);
}
