/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.service;

import org.cogchar.vision.PortableImage;

/**
 *
 * @author humankind
 */
public interface FreckleService {
	public boolean submitImage(String handle, PortableImage pimg);
	public void registerListener(FreckleResultListener frl);
}
