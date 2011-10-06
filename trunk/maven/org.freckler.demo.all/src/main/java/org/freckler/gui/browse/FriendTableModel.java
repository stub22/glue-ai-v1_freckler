/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.gui.browse;


import org.cogchar.freckbase.Friend;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.cogchar.platform.util.TimeUtils;
;

/**
 *
 * @author Stu Baurmann
 */
public class FriendTableModel extends AbstractTableModel {
	private static Logger	theLogger = Logger.getLogger(FriendTableModel.class.getName());

	private	List<Friend>			myCachedFriends;
	private	long					myLastRefreshStampMsec;
	private	FriendBrowserImpl		myFBI;

	private static String theColumnNames [] =  {
		"friendID", "profileID", "freckleID", "name"
	};

	public FriendTableModel (FriendBrowserImpl fbi) {
		myFBI = fbi;
	}
	protected synchronized void refresh() {
		myCachedFriends = myFBI.getAllFriends();
		myLastRefreshStampMsec = TimeUtils.currentTimeMillis();
	}
	protected void refreshIfNeeded(long maxCacheLifeMsec) {
		if (myCachedFriends != null) {
			long	 nowMsec = TimeUtils.currentTimeMillis();
			if  (nowMsec - myLastRefreshStampMsec > maxCacheLifeMsec) {
				refresh();
			}
		} else {
			refresh();
		}
	}
	public int getColumnCount() {
		return 4;
	}
	public String getColumnName(int i) {
		return theColumnNames[i];
	}
	public int getRowCount() {
		refreshIfNeeded(100);
		return myCachedFriends.size();
	}
	public Class getColumnClass(int c) {
		// The returned class will be used to pick a renderer for the column c.
	//	Class clz = null;
	//	if ((c == 5) || (c == 6)) {
	//		return FaceObservation.class;
	//	} else {
			return String.class;
	//	}
	}
	protected Friend getFriendAtRow(int rowIndex) {
		if (myCachedFriends.size() <= rowIndex) {
			theLogger.warning("myCachedFriends is smaller than requested index:" + rowIndex);
			return null;
		}
		Friend f = myCachedFriends.get(rowIndex);
		return f;
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		refreshIfNeeded(100);
		Friend friend = getFriendAtRow(rowIndex);
		String val = "null";
		if (friend != null) {
			switch(columnIndex) {
				case 0:
					val = "" + myFBI.getOptionalLong(friend.myObjectIdent(), -9999); // .getOrElse("NONE");
				break;
				case 1:
					val = "" + friend.myProfileID();
				break;
				case 2:
					val = "x";
				break;
				case 3:
					val = "" + myFBI.getOptionalString(friend.myPersonName(), "_"); // .getOrElse("NONE");
				break;
			}
		}
		return val;
	}
	public void initListeners(final JTable table) {
		// table.setCellSelectionEnabled(true);
		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		// Not possible to prevent multiple column selection?
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				theLogger.info("friend selection change - clearing status");
				myFBI.setFriendOpStatus("status: browsing");
				myFBI.setEntryOpStatus("status: browsing");
				Object selectedData = null;

				int[] selectedRow = table.getSelectedRows();
				int[] selectedColumns = table.getSelectedColumns();

				for (int i = 0; i < selectedRow.length; i++) {
					int selRow = selectedRow[i];
					Friend fr = getFriendAtRow(selRow);
					if (myFBI != null) {
						theLogger.info("Setting detailed friend to: " + fr);
						myFBI.setDetailedFriend(fr);
					}
					/*
					for (int j = 0; j < selectedColumns.length; j++) {
						int selCol = selectedColumns[j];
						selectedData = table.getValueAt(selRow, selCol);
						theLogger.info("PersonTable selected [r=" + selRow
									+ ",c=" + selCol + "], data=" + selectedData);

						PersonCue pcue = getCueAtRow(selRow);
						if (selCol == 5) {
							FreckleFace ff = myPersonMonitorImpl.getFreckleFaceForPersonCue(pcue);
							myPersonMonitorImpl.setDetailedFreckleFace(ff);
						}
						if (selCol == 6) {
							FaceHypothesis hypo = myPersonMonitorImpl.getFaceHypoForPersonCue(pcue);
							myPersonMonitorImpl.setDetailedFaceHypothesis(hypo);
						}
					}
					 *
					 */
				}
			}
		});
	}

}
