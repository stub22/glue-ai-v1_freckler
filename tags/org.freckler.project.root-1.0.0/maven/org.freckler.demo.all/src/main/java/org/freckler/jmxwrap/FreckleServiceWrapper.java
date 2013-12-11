/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.jmxwrap;

import org.freckler.gui.photo.ImageDisplayPanel;
import org.freckler.gui.photo.FrecklerMonitorFrame;
import org.freckler.service.FreckleResultListener;
import org.freckler.service.FreckleServiceImpl;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectName;
import javax.swing.JFrame;


import org.freckler.gui.browse.FriendBrowserImpl;
import org.freckler.gui.browse.FriendBrowserPanel;
import org.freckler.gui.photo.MultiPhotoPanel;
import org.cogchar.integroid.jmxwrap.NotifyingBeanImpl;

import org.cogchar.platform.util.StringUtils;
import org.cogchar.platform.util.TimeUtils;
import org.cogchar.sight.api.facerec.FreckleQuery;
import org.cogchar.sight.api.facerec.FreckleResult;
import org.cogchar.sight.api.obs.PortableImage;

import org.freckler.extra.FreckbaseFacade;
import org.freckler.facerec.impl.nwrap.FaceProfileManager;
import org.freckler.facerec.impl.nwrap.FaceRecPopulationManager;


/**
 * @author Stu Baurmann
 */
public class FreckleServiceWrapper extends NotifyingBeanImpl implements FreckleServiceWrapperMXBean  {
	private static Logger	theLogger = Logger.getLogger(FreckleServiceWrapper.class.getName());
	private BlockingQueue<FreckleQuery>	myQueryQueue;

	private	FreckleServiceImpl			myServiceImpl;
	private	FreckleResultListener		myLoopbackListener;

	private ImageDisplayPanel			myMonitorPanel;
	private	FriendBrowserImpl			myFBI;

	private	FreckbaseFacade				myFreckbaseFacade;
	private Long						myFreckbasePopID;

	public FreckleServiceWrapper(ObjectName on) {
		super(on);
		myQueryQueue = new LinkedBlockingQueue<FreckleQuery>(20);
	}
	public void setServiceImpl(FreckleServiceImpl impl) {
		myServiceImpl = impl;
	}

	public Boolean submitAsyncQuery(FreckleQuery query) {
		boolean successFlag = false;
		try {
			theLogger.info("Received query: " + query);
			myQueryQueue.put(query);
			successFlag = true;
		} catch (Throwable t) {
			theLogger.log(Level.SEVERE, "Exception during write to query queue", t);
		}
		return successFlag;
	}
	public FreckleResult syncQuery(FreckleQuery query, boolean notifyListeners) {
		// return processOneQueryUsingDefaultPop(query, notifyListeners);
		return processOneQueryUsingFreckbasePop(query, notifyListeners);
	}
	public void handleOneQueuedQuery() {
		try {
			FreckleQuery query = myQueryQueue.take();
			FreckleResult result = processOneQueryUsingDefaultPop(query, true);
			notifyListeners(result);
		} catch (Throwable t) {
			theLogger.log(Level.SEVERE, "Exception during query processing", t);
		}
	}
	private synchronized FreckleResult processOneQueryUsingDefaultPop(FreckleQuery query,  boolean notifyListeners) {
		FaceRecPopulationManager popMgr = myServiceImpl.getPopulationManager();
		long defPopID = popMgr.getDefaultPopulationID();
		return processOneQuery(query, notifyListeners, defPopID);
	}
	private synchronized FreckleResult processOneQueryUsingFreckbasePop(FreckleQuery query,  boolean notifyListeners) {
		return processOneQuery(query, notifyListeners, myFreckbasePopID);
	}
	private void updateGUI(FreckleQuery query) {
		PortableImage pimg = query.getPortableImage();
		if (myMonitorPanel != null) {
			theLogger.info("Showing " + pimg + " on " + myMonitorPanel);
			myMonitorPanel.setPortableImage(pimg);
			myMonitorPanel.repaint();
		}
	}
	private synchronized FreckleResult processOneQuery(FreckleQuery query,  boolean notifyListeners, long popID) {
		theLogger.info("Matching query against pop: " + popID);
		if (!myFreckbaseFacade.getAutoEnrollFlag()) {
			theLogger.info("Disabling auto enrollment for query");
			query.disableAutoEnrollment();
		}
		updateGUI(query);
		FreckleResult result = myServiceImpl.attemptMatchOrEnroll(popID, query);
		org.cogchar.freckbase.Observation updatedObs = myFreckbaseFacade.processFreckleResult(query, result);
		String recogStatus = updatedObs.myRecogStatus();
		if (recogStatus.equals("ENROLLED") || recogStatus.equals("MATCHED_AND_PROFILE_EXPANDED")) {
			updateFreckbasePopulations();
		}
		// updateLegacyPopulations(query, result, popID);
		if (notifyListeners) {
			notifyListeners(result);
		}
		return result;
	}
	/****
	private synchronized void updateLegacyPopulations(FreckleQuery query, FreckleResult result, long popID) {
		FaceRecPopulationManager popMgr = myServiceImpl.getPopulationManager();
		long defaultPopID = popMgr.getDefaultPopulationID();
		theLogger.info("Default popID=" + defaultPopID);
		if (result.getEnrollmentAction() == FreckleResult.EnrollmentAction.SUCCESS) {
			if (defaultPopID == popID) {
				theLogger.info("Saving DEFAULT population to preserve new enrollment.");
				// TODO: This needs to be generalized so we can save a particular population.
				myServiceImpl.saveDefaultPopulation();
			}
		} else {
			theLogger.info("No new enrollments, so population not saved.");
		}
		String[] allFreckleIDs = popMgr.listPopulation(popID);
		result.setPopulationFreckleIDs(allFreckleIDs);
	}
	****/
	private void notifyListeners(FreckleResult result) {
		theLogger.fine("Publishing result: " + result);
		if (myLoopbackListener != null) {
			myLoopbackListener.noticeFreckleResult(result);
		}
		// Notify JMX clients
		sendAttributeChangeNotification(
					    "Result=[" + result.toString() + "]", ATTRIB_FRECKLE_RESULT, null, result);

	}
	/*
	public String[] getDefaultPopulationFreckleIDs() {
		return myServiceImpl.getDefaultPopulationFreckleIDs();
	}
	 */
	public void setLoopbackListener(FreckleResultListener listener) {
		myLoopbackListener = listener;
	}
	public static FreckleServiceWrapper createAndRegister() throws Throwable {
		ObjectName on = new ObjectName(FreckleServiceWrapperMXBean.FRECKLER_JMX_OBJNAME);
		FreckleServiceWrapper w = new FreckleServiceWrapper(on);
		w.register();
		return w;
	}
	public void setupMonitorGUI() {
		FrecklerMonitorFrame fmf = new FrecklerMonitorFrame();
		fmf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fmf.pack();
		fmf.setVisible(true);
		myMonitorPanel = fmf.myFMTP.getStatusPanel();
		MultiPhotoPanel mpp = fmf.myFMTP.getMultiPhotoPanel();
		mpp.setFreckbaseFacade(myFreckbaseFacade);
		FriendBrowserPanel fbp = fmf.myFMTP.getBrowserPanel();
		FriendBrowserImpl fbi = fbp.getBrowserImpl();
		fbi.setFreckbaseFacade(myFreckbaseFacade);
		fbi.setFreckleServiceWrapper(this);
		myFBI = fbi;
	}
	public static void main(String args[]) {
		try {
			System.out.println("herro?");

			if (args.length != 3) {
				System.err.println("Expected 3 args: fvConfigPath fvRepoPath h2DbPath");
				System.exit(-1);
			}
			System.out.println("Arg[0]=" + args[0]);
			System.out.println("Arg[1]=" + args[1]);
			System.out.println("Arg[2]=" + args[2]);

			// Need this for the OpenCV image code.
			System.loadLibrary("RobotControlJNIWrapper");
			String fvConfigPath = args[0]; // "C:\\_hanson\\_wapp\\facevacs_6_1_0\\etc\\frsdk.cfg";
			String fvRepoPath = args[1]; // "C:\\_hanson\\_deploy\\distro_18a\\freckleRepo.fvp";
			String h2DbPath = args[2]; // "C:\\_hanson\\_deploy\\fbtest\\fb01";

			theLogger.info("fvConfigPath=" + fvConfigPath);
			theLogger.info("fvRepoPath=" + fvRepoPath);
			theLogger.info("h2DbPath=" + h2DbPath);


			FreckleServiceImpl fsi = new FreckleServiceImpl(fvConfigPath, fvRepoPath);
//			theLogger.info("Initializing FaceVACS");
//			fsi.connectToFreckleNativeImpl();
			/****
			theLogger.info("Loading population");
			fsi.loadDefaultPopulation();
			****/
			theLogger.info("Initializing JMX freckle server");
			FreckleServiceWrapper fsw = createAndRegister();
			fsw.setServiceImpl(fsi);
			
			fsw.initFreckbaseServer(h2DbPath);
			// MonitorGUI doesn't work yet, because we can't easily rebuild a
			// java AWT image from the bytes[] serialization format.
			fsw.setupMonitorGUI();

			while (true) {
				theLogger.info("Handler loop is calling processOneQuery");
				fsw.handleOneQueuedQuery();
			}
		} catch (Throwable t) {
			theLogger.log(Level.SEVERE, "Exception caught in main", t);
		}
		theLogger.info("Main thread exiting");
	}
	public void initFreckbaseServer(String dbFilePath) {
		theLogger.info("Initializing Freckbase Server");
		FaceRecPopulationManager frpm = myServiceImpl.getPopulationManager();
		FaceProfileManager fpm = (FaceProfileManager) frpm;
		myFreckbaseFacade = FreckbaseFacade.initServerFacade(dbFilePath, fpm);
		
		theLogger.info("Initializing Freckbase Population");
		updateFreckbasePopulations();
	}
	public void updateFreckbasePopulations() {
		long startStamp = TimeUtils.currentTimeMillis();
		FaceRecPopulationManager popMgr = myServiceImpl.getPopulationManager();
		myFreckbasePopID = popMgr.createPopulation();
		myFreckbaseFacade.buildPopulation(popMgr, myFreckbasePopID);
		String entryIDs[] = popMgr.listPopulation(myFreckbasePopID);
		String entryDump = StringUtils.joinArray(entryIDs, ", ");
		theLogger.info("Filled new population[" + myFreckbasePopID + "] in " + TimeUtils.getStampAgeSec(startStamp)
				+ " sec with IDs: " + entryDump);
		if (myFBI != null) {
			myFBI.refreshTables();
		}
		//TODO : Destroy the old population.
	}
	public FreckbaseFacade getFreckbaseFacade() {
		return myFreckbaseFacade;
	}
}
