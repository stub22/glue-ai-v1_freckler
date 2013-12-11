/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PhotoMonitorPanel.java
 *
 * Created on Mar 13, 2010, 9:17:25 PM
 */

package org.freckler.gui.photo;

import java.awt.Image;
import java.util.logging.Logger;
import org.freckler.extra.FreckbaseFacade;

/**
 *
 * @author humankind
 */
public class PhotoMonitorPanel extends javax.swing.JPanel {
	private static Logger	theLogger = Logger.getLogger(PhotoMonitorPanel.class.getName());

	private FreckbaseFacade		myFreckbaseFacade;
    /** Creates new form PhotoMonitorPanel */
    public PhotoMonitorPanel() {
        initComponents();
    }
	public void setFreckbaseFacade(FreckbaseFacade ff) {
		myFreckbaseFacade = ff;
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imageDisplayPanel = new org.freckler.gui.photo.ImageDisplayPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txt_photoIdent = new javax.swing.JTextField();
        but_display = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        imageDisplayPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout imageDisplayPanelLayout = new javax.swing.GroupLayout(imageDisplayPanel);
        imageDisplayPanel.setLayout(imageDisplayPanelLayout);
        imageDisplayPanelLayout.setHorizontalGroup(
            imageDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 462, Short.MAX_VALUE)
        );
        imageDisplayPanelLayout.setVerticalGroup(
            imageDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        add(imageDisplayPanel, java.awt.BorderLayout.CENTER);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setText("Photo ID");

        txt_photoIdent.setText("1");

        but_display.setText("display");
        but_display.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_displayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txt_photoIdent, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(but_display)
                .addContainerGap(247, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txt_photoIdent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(but_display))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

	private void but_displayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_displayActionPerformed
		// TODO add your handling code here:
		String photoIdentText = txt_photoIdent.getText();
		Long photoIdent = Long.parseLong(photoIdentText);
		Image photoImage = myFreckbaseFacade.getPhotoImage(photoIdent);
		imageDisplayPanel.setImage(photoImage);
		imageDisplayPanel.repaint();
	}//GEN-LAST:event_but_displayActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_display;
    private org.freckler.gui.photo.ImageDisplayPanel imageDisplayPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txt_photoIdent;
    // End of variables declaration//GEN-END:variables

}