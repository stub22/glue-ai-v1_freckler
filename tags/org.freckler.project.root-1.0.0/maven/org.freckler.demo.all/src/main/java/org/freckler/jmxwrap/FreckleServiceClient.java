/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.jmxwrap;

import org.freckler.service.FreckleResultListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeChangeNotification;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.cogchar.integroid.jmxwrap.JMXUtils;
import org.cogchar.platform.util.TimeUtils;
import org.cogchar.sight.api.facerec.FreckleQuery;
import org.cogchar.sight.api.facerec.FreckleResult;

/**
 *
 * @author Stu Baurmann
 */
public class FreckleServiceClient implements NotificationListener, FreckleServiceWrapperMXBean {
	private static Logger	theLogger = Logger.getLogger(FreckleServiceClient.class.getName());

	private		MBeanServerConnection			myMBSC;
	private		ObjectName						myFrecklerON;
	private		FreckleServiceWrapperMXBean		myProxy;
	private		List<FreckleResultListener>		myListeners;
	
	public FreckleServiceClient(MBeanServerConnection mbsc) throws Throwable {
		myMBSC = mbsc;
		myListeners = new ArrayList<FreckleResultListener>();
	}
	private void connectProxies() throws Throwable {
		myFrecklerON = new ObjectName(FreckleServiceWrapperMXBean.FRECKLER_JMX_OBJNAME);
		myProxy = JMX.newMXBeanProxy(myMBSC, myFrecklerON, FreckleServiceWrapperMXBean.class);
	}
	public static FreckleServiceClient makeClientAndConnect(String serviceURL, boolean verbose) {
		FreckleServiceClient client = null;
        try {
            theLogger.info("\nCreating a JMX-RMI connection to the Hanson FreckleServer application at URL: " + serviceURL);
            JMXServiceURL url = new JMXServiceURL(serviceURL);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            // Get an MBeanServerConnection
            theLogger.info("\nFetching an MBeanServerConnection");
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
			if (verbose) {
				JMXUtils.dumpMBeanServerInfo(mbsc);
			}
			client = new FreckleServiceClient(mbsc);
			client.connectProxies();
			client.registerHandlers();

        } catch (Throwable t) {
            t.printStackTrace();
        }
		return client;
    }
	private void registerHandlers() throws Throwable {
		NotificationFilter filter = null;
		Object handback = null;
		// All notifications are routed to my handleNotification method.
		myMBSC.addNotificationListener(myFrecklerON, this, filter, handback);
	}
	public void addListener(FreckleResultListener frl) {
		myListeners.add(frl);
	}
	public void handleNotification(Notification notification, Object handback) {
		try {
			theLogger.info("Received notification at: " + System.currentTimeMillis());
//			echo("\tClassName: " + notification.getClass().getName());
//			echo("\tSource: " + notification.getSource());
// 			echo("\tType: " + notification.getType());
			
			// theLogger.info("\tMessage: " + notification.getMessage());
			if (notification instanceof AttributeChangeNotification) {
				AttributeChangeNotification acn = (AttributeChangeNotification) notification;
				String attribName = acn.getAttributeName();
				String attribTypeName = acn.getAttributeType();
				Object newValue =  acn.getNewValue();
				Object oldValue = acn.getOldValue();

				// Dispatch based on "attribute name"
				if (attribName.equals(FreckleServiceWrapperMXBean.ATTRIB_FRECKLE_RESULT)) {
					FreckleResult fres = (FreckleResult) newValue;
					handleFreckleResult(fres);
				} else {
					theLogger.warning("####################################################################"
							+ "\nUnhandled attribute change notification.  Details are:"
							+ "\n\tAttributeName: " + attribName
							+ "\n\tAttributeType: " + attribTypeName
							+ "\n\tNewValue: " + newValue
							+ "\n\tOldValue: " + oldValue
							+ "####################################################################");
				}
			}
			theLogger.fine("Finished processing notification at:" + TimeUtils.currentTimeMillis());
		} catch (Throwable t) {
			theLogger.log(Level.SEVERE, "problem processing notification", t);
		}
	}
	private void handleFreckleResult(FreckleResult fres) {
		for (FreckleResultListener frl : myListeners) {
			frl.noticeFreckleResult(fres);
		}
	}
	public Boolean submitAsyncQuery(FreckleQuery query) {
		return myProxy.submitAsyncQuery(query);
	}
	public 	FreckleResult syncQuery(FreckleQuery query, boolean notifyListeners) {
		return myProxy.syncQuery(query, notifyListeners);
	}
	/*
	public String[] getDefaultPopulationFreckleIDs() {
		return myProxy.getDefaultPopulationFreckleIDs();
	}
	 */
}
