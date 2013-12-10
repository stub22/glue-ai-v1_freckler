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

package org.freckler.extra;

import org.cogchar.freckbase.FreckbaseSession;
import org.cogchar.freckbase.Friend;
import org.cogchar.freckbase.Manager;
import org.cogchar.freckbase.Observation;
import org.cogchar.freckbase.ProfileEntry;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.cogchar.sight.api.facerec.FreckleQuery;
import org.cogchar.sight.api.facerec.FreckleResult;
import org.cogchar.sight.api.obs.PortableImage;

import org.freckler.facerec.impl.nwrap.FaceProfileManager;
import org.freckler.facerec.impl.nwrap.FaceRecPopulationManager;
import scala.Option;

/**
 * @author Stu Baurmann
 */
public class FreckbaseFacade {
	private static Logger	theLogger = Logger.getLogger(FreckbaseFacade.class.getName());
	private FreckbaseSession	myFreckbaseSession;
	private Manager				myManager;
	private	FaceProfileManager	myProfileComputer;
	private	boolean				myAutoEnrollFlag = false;

	public static FreckbaseFacade initServerFacade(String dbFilePath, FaceProfileManager fpm) {
		FreckbaseFacade ff = new FreckbaseFacade();
		// This $.MODULE$ stuff is not necessary under Scala 2.8.
		if (dbFilePath != null) {
			FreckbaseSession.configureDatabaseFilePath(dbFilePath);
		}
		ff.myFreckbaseSession = FreckbaseSession.serverSession();
		ff.myManager = new Manager(ff.myFreckbaseSession);
		ff.myManager.initTables();
		ff.myFreckbaseSession.startTCPServer(FreckbaseSession.theTcpPort());
		ff.myProfileComputer = fpm;
		return ff;
	}
	public static FreckbaseFacade initClientFacade(String dbFilePath) {
		if (dbFilePath != null) {
			FreckbaseSession.configureDatabaseFilePath(dbFilePath);
		}
		FreckbaseFacade ff = new FreckbaseFacade();
		// This $.MODULE$ stuff is not necessary under Scala 2.8.
		ff.myFreckbaseSession = FreckbaseSession.clientSession();
		ff.myManager = new Manager(ff.myFreckbaseSession);
		return ff;
	}

	public void initTables() {
		myManager.initTables();
	}

	public Observation processFreckleResult(FreckleQuery query, FreckleResult result) {
		return myManager.processFreckleResult(null, query, result);
	}

	public void buildPopulation(FaceRecPopulationManager popMgr, Long freckbasePopID) {
		// myManager.buildPopulation(popMgr, freckbasePopID);
		myManager.makeMatchablePopulation(null, popMgr, freckbasePopID);
	}

	public Long writeFaceObs(Long frameStamp, Long hypoID, String stat, int width, int height,
				byte[] faceImageBytes)  {
		Long recordedObsID = myManager.recordObs(null, frameStamp, hypoID,
				stat, width, height, faceImageBytes);
		return recordedObsID;
	}
	public Long writeFaceHypo() {
		Long recordedHypoID = myManager.recordHypo(null);
		return recordedHypoID;
	}
	public Observation readFreckbaseObs(Long obsID) {
		return myManager.getObservation(null, obsID.longValue());
	}
	public Friend readFreckbaseFriend(Long friendID) {
		return myManager.getFriend(null, friendID.longValue());
	}
	public void setFriendName(Long friendID, String friendName) {
		myManager.setFriendName(null, friendID.longValue(), friendName);
	}
	public Image getPhotoImage(Long photoID) {
		return myManager.getPhotoImage(null, photoID.longValue());
	}
	public Long importObsFromImageFile(File imageFile) {
		Long obsID = null;
		try {
			BufferedImage img = ImageIO.read(imageFile);
			PortableImage pi = new PortableImage(img, false);
			int width = pi.getWidth();
			int height = pi.getHeight();
			theLogger.info("Read image file " + imageFile + " and got width,height=" + width + "," + height);
			Long stamp = -1L;
			Long hypoID = -1L;
			obsID = writeFaceObs(stamp, hypoID, "IMPORTED", width, height, pi.getBytesWithoutHeader());
			theLogger.info("Wrote obs with ID=" + obsID);
		} catch (Throwable t) {
			theLogger.log(Level.SEVERE, "Problem importing obs image: " + imageFile, t);
		}
		return obsID;
	}
	public Long createFriendAndProfileWithImportedFoundingObs(Long obsID) {
		return myManager.makeFriendFromImportedObs(null, obsID.longValue());
	}
	public void expandFriendProfileWithImportedObs(Long friendID, Long obsID) {
		myManager.expandFriendProfileWithImportedObs(null, friendID.longValue(), obsID.longValue());
	}
	public List<Friend> getAllFriends() {
		return myManager.getAllFriendsInJavaList(null);
	}
	public List<ProfileEntry> getProfileEntriesForFriend(Long friendID) {
		return myManager.getFriendProfileEntriesInJavaList(null, friendID);
	}
	public long getOptionalLong(Option o, long dfaultValue) {
		return myManager.getOptionalLong(o, dfaultValue);
	}
	public String getOptionalString(Option o, String dfaultValue) {
		return myManager.getOptionalString(o, dfaultValue);
	}
	public void removeFriend(Long friendID) {
		myManager.removeFriend(null, friendID.longValue());
	}
	public void removeProfileEntry(Long profileEntryID) {
		myManager.removeProfileEntry(null, profileEntryID.longValue());
	}
	public void setAutoEnrollFlag(boolean flag) {
		myAutoEnrollFlag = flag;
	}
	public boolean getAutoEnrollFlag() {
		return myAutoEnrollFlag;
	}	
}
