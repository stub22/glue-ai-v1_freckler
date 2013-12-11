/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.gui.photo;

import java.awt.BorderLayout;

/**
 *
 * @author Stu Baurmann
 */
public class FrecklerMonitorFrame extends javax.swing.JFrame {
	public		FrecklerMonitorTabPanel		myFMTP;
	public FrecklerMonitorFrame() {
		myFMTP = new FrecklerMonitorTabPanel();
		/*
		myFIP.setBackground(new java.awt.Color(0, 153, 51));
        myFIP.setBorder(javax.swing.BorderFactory.createMatteBorder(5, 5, 5, 5, new java.awt.Color(0, 102, 0)));
        myFIP.setMaximumSize(new java.awt.Dimension(320, 240));
        myFIP.setMinimumSize(new java.awt.Dimension(320, 240));
        myFIP.setPreferredSize(new java.awt.Dimension(320, 240));
		 */
		getContentPane().add(myFMTP, BorderLayout.CENTER);
	}
}
