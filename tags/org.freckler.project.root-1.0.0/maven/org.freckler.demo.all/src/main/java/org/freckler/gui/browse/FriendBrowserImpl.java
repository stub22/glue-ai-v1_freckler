/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.gui.browse;

import org.cogchar.freckbase.Friend;
import org.cogchar.freckbase.Observation;
import org.cogchar.freckbase.ProfileEntry;
import org.freckler.jmxwrap.FreckleServiceWrapper;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.freckler.extra.FreckbaseFacade;
import scala.Option;

/**
 *
 * @author Stu Baurmann
 */
public class FriendBrowserImpl {
	public class FriendStat {
		public	Friend	obj;
		public	Long	ident;
		public  String	name;
	}
	private static Logger	theLogger = Logger.getLogger(FriendBrowserImpl.class.getName());

	private		FriendTableModel		myFTM;
	private		EntryTableModel			myETM;
	private		FreckbaseFacade			myFreckbaseFacade;
	private		FreckleServiceWrapper	myFSW;

	JLabel		myFriendOpStat, myEntryOpStat;

	public void initTables(JTable friendTable, JTable entryTable) {
		myFTM = new FriendTableModel(this);
		friendTable.setModel(myFTM);
		myETM = new EntryTableModel(this);
		entryTable.setModel(myETM);
		myFTM.initListeners(friendTable);
		myETM.initRenderers(entryTable);
		myETM.initListeners(entryTable);
	}
	public void registerStatusLabels(JLabel friendOpStat, JLabel entryOpStat) {
		myFriendOpStat = friendOpStat;
		myEntryOpStat = entryOpStat;
	}
	public void setFreckbaseFacade(FreckbaseFacade ff) {
		myFreckbaseFacade = ff;
		myFTM.refresh();
		myFTM.fireTableDataChanged();
	}
	public void setFreckleServiceWrapper(FreckleServiceWrapper fsw) {
		myFSW = fsw;
	}	
	public List<Friend> getAllFriends() {
		if (myFreckbaseFacade != null) {
			return myFreckbaseFacade.getAllFriends();
		} else {
			return new ArrayList<Friend>();
		}
	}
	public List<ProfileEntry> getEntriesForFriend(Long friendID) {
		if ((myFreckbaseFacade != null) && (friendID != null)) {
			return myFreckbaseFacade.getProfileEntriesForFriend(friendID);
		} else {
			return new ArrayList<ProfileEntry>();
		}
	}
	public Observation getObsForID(Long obsID) {
		Observation obs = myFreckbaseFacade.readFreckbaseObs(obsID);
		return obs;
	}
	public Image getPhotoImageForID(Long photoID) {
		Image img = myFreckbaseFacade.getPhotoImage(photoID);
		return img;
	}
	public void setDetailedFriend(Friend f) {
		if (myETM != null) {
			Long friendID = null;
			if (f != null) {
				friendID = myFreckbaseFacade.getOptionalLong(f.myObjectIdent(), -1);
				if (friendID == -1) {
					// TODO: print warning
					friendID = null;
				}
			}
			myETM.setDetailedFriendID(friendID);
		}
	}
	public Long makeNewFriend(File imageFile) {
		setFriendOpStatus("Reading image file into new DB observation");
		Long obsID = myFreckbaseFacade.importObsFromImageFile(imageFile);
		setFriendOpStatus("Creating DB records for friend and profile");
		Long friendID = myFreckbaseFacade.createFriendAndProfileWithImportedFoundingObs(obsID);
		setFriendOpStatus("Rebuilding population, please wait");
		myFSW.updateFreckbasePopulations();
		setFriendOpStatus("Created friend " + friendID + " with founding obs " + obsID);
		myFTM.fireTableDataChanged();
		return friendID;
	}
	public void addPhotoToFriendProfile(Long friendID, File imageFile) {
		setEntryOpStatus("Reading image file into new DB observation");
		Long obsID = myFreckbaseFacade.importObsFromImageFile(imageFile);
		setEntryOpStatus("Creating DB records for new friend and profile");
		myFreckbaseFacade.expandFriendProfileWithImportedObs(friendID, obsID);
		setEntryOpStatus("Rebuilding population, please wait");
		myFSW.updateFreckbasePopulations();
		setEntryOpStatus("Expanded friend " + friendID + " with obs " + obsID);
		myETM.fireTableDataChanged();
	}
	public FriendStat getFriendStatForTableRow(int friendRowNum) {
		FriendStat stat = new FriendStat();
		Friend friend = null;
		if (friendRowNum >= 0) {
			stat.obj = myFTM.getFriendAtRow(friendRowNum);
			stat.ident = getOptionalLong(stat.obj.myObjectIdent(), -1);
			stat.name = getOptionalString(stat.obj.myPersonName(), null);
		}
		return stat;
	}
	public long getOptionalLong(Option olong, long defval) {
		return myFreckbaseFacade.getOptionalLong(olong, defval);
	}
	public String getOptionalString(Option ostr, String defval) {
		return myFreckbaseFacade.getOptionalString(ostr, defval);
	}
	public void setFriendName(int friendRowNum, String friendNewName) {
		FriendStat		stat = getFriendStatForTableRow(friendRowNum);
		if (stat.obj != null) {
			theLogger.info("Setting name for friend# " + stat.ident + " to " + friendNewName);
			myFreckbaseFacade.setFriendName(stat.ident, friendNewName);
			myFTM.fireTableDataChanged();
		} else {
			theLogger.warning("Can't find friend for row# " + friendRowNum);
		}
	}
	public void removeFriend(Long friendID) {
		theLogger.info("Removing friend with ID=" + friendID);
		myETM.setDetailedFriendID(null);
		myFreckbaseFacade.removeFriend(friendID);
		myFTM.fireTableDataChanged();
	}
	public void removeProfileEntry(Long entryID) {
		theLogger.info("Removing profile entry with ID=" + entryID);
		myFreckbaseFacade.removeProfileEntry(entryID);
		myETM.fireTableDataChanged();
	}
	public Long getProfileEntryID_forRow(int rownum) {
		ProfileEntry entry = myETM.getEntryAtRow(rownum);
		return myFreckbaseFacade.getOptionalLong(entry.myObjectIdent(), -1);
	}
	public void setFriendOpStatus(String status) {
		if (myFriendOpStat != null) {
			myFriendOpStat.setText(status);
		}
	}
	public void setEntryOpStatus(String status) {
		if (myEntryOpStat != null) {
			myEntryOpStat.setText(status);
		}
	}

	public void setAutoEnrollFlag(boolean flag) {
		myFreckbaseFacade.setAutoEnrollFlag(flag);
	}
	public void refreshTables() {
		myETM.fireTableDataChanged();
		myFTM.fireTableDataChanged();
	}
}
