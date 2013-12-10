/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.cogchar.sight.api.facerec.FreckleMatchConfig;
import org.cogchar.sight.api.facerec.FreckleQuery;
import org.cogchar.sight.api.facerec.FreckleResult;
import org.cogchar.sight.api.obs.OpenCVImage;
import org.cogchar.sight.api.obs.PortableImage;

import org.freckler.facerec.impl.nwrap.FaceRecPopulationManager;

/**
 *
 * @author Stu Baurmann
 */
public class FreckleServiceImpl {
	private static Logger theLogger = Logger.getLogger(FreckleServiceImpl.class.getName());

	private		String							myFV_ConfigPath;
	private		String							myFV_RepositoryPath;
	private		FaceRecPopulationManager		myPopMgr;

	public FreckleServiceImpl(String fvConfigPath, String fvRepoPath) {
		myFV_ConfigPath = fvConfigPath;
		myFV_RepositoryPath = fvRepoPath;
	}
	/*
	public synchronized void connectToFreckleNativeImpl() {
		if (myPopMgr == null) {
			// String fvConfigPath = "C:\\_hanson\\_wapp\\facevacs_6_1_0\\etc\\frsdk.cfg";
			String serverPort = "56099";
			String config = "FaceVacsConfig:" + myFV_ConfigPath + "\n"
							+ "ServerPort:" + serverPort + "\n";
			myPopMgr = new FaceRecServer(config);
			((FaceRecServer)myPopMgr).start();
			// On exit, should do:   myFreckleService.shutdown();
		}
	}
	public synchronized void disconnectFromFreckleService() {
		((FaceRecServer)myPopMgr).shutdown();
		myPopMgr = null;
	}
 */
	
	/*
	public FreckleResult attemptMatchOrEnrollInDefaultPop(FreckleQuery query) {
		long popID = myPopMgr.getDefaultPopulationID();
		return attemptMatchOrEnroll(popID, query);
	}
	 */
	public FreckleResult attemptMatchOrEnroll(long popID, FreckleQuery query) {
		
		String queryHandle = query.getHandle();
		PortableImage pimg = query.getPortableImage();
		OpenCVImage ocvi = pimg.fetchOpenCVImage();
		
		FreckleResult	result = new FreckleResult();
		result.setSubmittedHandle(queryHandle);
		String encodedMatchResult = myPopMgr.matchPerson(ocvi, popID);

		result.parseMatchingResult(encodedMatchResult);

		FreckleMatchConfig conf = query.getFreckleMatchConfig();
		Double enrollPreventThresh = conf.getMatchScorePreventEnrollThresh();
		if (result.checkEnrollmentWorthy(enrollPreventThresh)) {
			String enrollFreckleID = query.getEnrollmentFreckleID();
			List<OpenCVImage> enrollImageBatch = new ArrayList<OpenCVImage>();
			enrollImageBatch.add(ocvi);

			String bitmapFilename = "db/bitmap/" + enrollFreckleID + ".bmp";
			theLogger.info("Writing enrollment image to" + bitmapFilename);
			ocvi.SaveFile(bitmapFilename);
			theLogger.info("Attempting to enroll new freckle-face with ID: " + enrollFreckleID);

			boolean enrollSuccess = myPopMgr.addNamedPerson(enrollImageBatch, enrollFreckleID, popID);
			if (enrollSuccess) {
				result.setEnrollmentAction(FreckleResult.EnrollmentAction.SUCCESS);
				result.setEnrolledFreckleID(enrollFreckleID);
			} else {
				result.setEnrollmentAction(FreckleResult.EnrollmentAction.ATTEMPT_FAILED);
			}
		}
		return result;
	}

	public synchronized void loadDefaultPopulation() {
			/*****
		try {
			theLogger.info("Calling FreckleService.loadPopulation(" + myFV_RepositoryPath + ")");
			myPopMgr.loadPopulationAndReplaceDefault(myFV_RepositoryPath);
		} catch (Throwable t) {
			theLogger.log(Level.SEVERE, "problem loading population from repo " + myFV_RepositoryPath, t);
		}
			 ***/
	}
	public synchronized void saveDefaultPopulation() {
			/*****
		try {
			long defPopID = myPopMgr.getDefaultPopulationID();
			myPopMgr.savePopulation(defPopID, myFV_RepositoryPath);
		} catch (Throwable t) {
			theLogger.log(Level.SEVERE, "problem saving population to " + myFV_RepositoryPath, t);
		}
			 ****/
	}
	/***
	public synchronized String[] getDefaultPopulationFreckleIDs() {
		long defPopID = myPopMgr.getDefaultPopulationID();
		String[] freckleIDs = myPopMgr.listPopulation(defPopID);
		return freckleIDs;
	}
	****/
	public FaceRecPopulationManager getPopulationManager() {
		return myPopMgr;
	}

}
