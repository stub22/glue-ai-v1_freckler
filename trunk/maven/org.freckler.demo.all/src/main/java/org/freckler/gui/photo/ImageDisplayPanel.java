/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.gui.photo;


import java.awt.Graphics;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cogchar.sight.vision.PortableImage;

/**
 *
 * @author Stu Baurmann
 */
public class ImageDisplayPanel extends javax.swing.JPanel {
private static Logger	theLogger = Logger.getLogger(ImageDisplayPanel.class.getName());
	private Image myImage;
	public void setImage(Image i) {
		myImage = i;
	}
	public void setPortableImage (PortableImage pimg) {
		Image i = pimg.fetchJavaImage();
		setImage(i);
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Image img = myImage;
		if (img != null) {
			int compWidth = getWidth();
			int compHeight = getHeight();
			int imgWidth = img.getWidth(null);
			int imgHeight = img.getHeight(null);
			double widthScale = (double) compWidth  / (double) imgWidth;
			double heightScale = (double) compHeight / (double) imgHeight;
			double minScale = Math.min(widthScale, heightScale);
			double scale = (minScale < 1.0)  ? minScale : 1.0;
			int targetWidth = (int) Math.floor(scale * imgWidth);
			int targetHeight = (int) Math.floor(scale * imgHeight);
            try {
				// theLogger.info("Drawing image: " + img);
                boolean drawingComplete = g.drawImage(img,  0, 0, targetWidth, targetHeight, null);
				if (!drawingComplete) {
					theLogger.warning("drawImage returned false - source  image is incomplete!");
				}
				/*
				for (IAnnotatingObserver iao : m_annotaters) {
					iao.Annotate(g);
				}
				*/
            } catch (Exception e) {
				theLogger.log(Level.SEVERE, "ImageDisplayPanel caught exception", e);
				e.printStackTrace();
            }
		}
	}
}
