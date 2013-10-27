/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.sight.impl.motion;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.logging.Logger;
import org.freckler.sight.impl.motion.PeakTracker;
import org.cogchar.sight.api.obs.ROIVisionObserver;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MotionFilterObserver extends ROIVisionObserver {
	private static Logger theLogger = Logger.getLogger(MotionFilterObserver.class.getName());

	private		PeakTracker		myPeakTracker;
	private		Point			myPeakCameraPixel;
	private		Color			myPeakAnnotColor;

	public MotionFilterObserver(Color instantColor, Color peakColor) {
		super(instantColor);
		myPeakAnnotColor = peakColor;
	}
	public void setPeakTracker(PeakTracker pt) {
		myPeakTracker = pt;
	}
	@Override public void Annotate(Graphics g) {
		// theLogger.info("MFO: Annotating!");
		super.Annotate(g);
		if (myPeakCameraPixel != null) {
			g.setColor(myPeakAnnotColor);
			if (myPeakTracker.getAttentionFlag()) {
				g.fillOval(myPeakCameraPixel.x - 8, myPeakCameraPixel.y - 8, 16, 16);
			} else {
				g.drawOval(myPeakCameraPixel.x - 5, myPeakCameraPixel.y - 5, 10, 10);
			}
		}
	}

	@Override public void ProcessFrame(int[] rectData) {
		super.ProcessFrame(rectData);

		if (myPeakTracker != null) {
			Point	cameraCenterPix = myPeakTracker.getCameraCenterPixel();
			int cameraArea = myPeakTracker.getCameraPixelArea();

			double sumVectorX = 0.0;
			double sumVectorY = 0.0;
			List<Rectangle> currentRects = getRectanglesOfInterest();
			for (Rectangle r : currentRects) {
				double rectCenterX = r.getCenterX();
				double rectCenterY = r.getCenterY();
				double rectArea = r.getWidth() * r.getHeight();
				double areaRatio = rectArea / cameraArea;
				double vectorX = rectCenterX - cameraCenterPix.x;
				double vectorY = rectCenterY - cameraCenterPix.y;
				// Overlapping rects will lead to "overweighting", but maybe that's OK...
				sumVectorX += vectorX * areaRatio;
				sumVectorY += vectorY * areaRatio;
			}
			// The peak may be "out of frame" due to overweighting...
			int peakCameraX = cameraCenterPix.x + (int) Math.round(sumVectorX);
			int peakCameraY = cameraCenterPix.y + (int) Math.round(sumVectorY);
			myPeakCameraPixel = new Point(peakCameraX, peakCameraY);
			myPeakTracker.noticeCameraPeakPixel(myPeakCameraPixel);
		}
		// theLogger.info("MFO: processFrame");
	}

}