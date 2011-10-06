/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.gui.browse;


import org.freckler.gui.photo.ImageDisplayPanel;
import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.cogchar.freckbase.Observation;
import org.cogchar.freckbase.ProfileEntry;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Stu Baurmann
 */
public class EntryTableModel  extends AbstractTableModel {
	private static Logger	theLogger = Logger.getLogger(EntryTableModel.class.getName());

	private	List<ProfileEntry>		myCachedEntries;
	private	long					myLastRefreshStampMsec;
	private	Long					myDetailedFriendID;
	private	FriendBrowserImpl		myFBI;
	private static String theColumnNames [] =  {
		"entryID", "obsID", "photoID", "image"
	};

	public EntryTableModel(FriendBrowserImpl fbi) {
		myFBI = fbi;
	}
	public void	setDetailedFriendID(Long friendID) {
		myDetailedFriendID = friendID;
		refresh();
		fireTableDataChanged();
	}
	protected synchronized void refresh() {
		// null friendID yields empty list
		myCachedEntries = myFBI.getEntriesForFriend(myDetailedFriendID);
		myLastRefreshStampMsec = TimeUtils.currentTimeMillis();
	}
	protected void refreshIfNeeded(long maxCacheLifeMsec) {
		if (myCachedEntries != null) {
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
		return myCachedEntries.size();
	}
	public Class getColumnClass(int c) {
		// The returned class will be used to pick a renderer for the column c.
		if (c == 3) {
			return Image.class;
		} else {
			return String.class;
		}
	}
	protected ProfileEntry getEntryAtRow(int rowIndex) {
		if (myCachedEntries.size() <= rowIndex) {
			theLogger.warning("myCachedFriends is smaller than requested index:" + rowIndex);
			return null;
		}
		ProfileEntry e = myCachedEntries.get(rowIndex);
		return e;
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		refreshIfNeeded(100);
		ProfileEntry entry = getEntryAtRow(rowIndex);
		long obsID = entry.myObsID();
		Observation obs = myFBI.getObsForID(obsID);
		long photoID = obs.myFacePhotoID();
		String val = "null";
		if (entry != null) {
			switch(columnIndex) {
				case 0:
					val = "" + myFBI.getOptionalLong(entry.myObjectIdent(), -9999); // .getOrElse("NONE");
				break;
				case 1:
					val = "" + obsID;
				break;
				case 2:
					val = "" + photoID;
				break;
				case 3:
					Image photoImg = myFBI.getPhotoImageForID(photoID);
					return photoImg;
			}
		}
		return val;
	}
	public void initRenderers(JTable table) {
		TableColumnModel tcm = table.getColumnModel();

		TableColumn photoCol = tcm.getColumn(3);
		photoCol.setCellRenderer(new EntryPhotoColumnRenderer());
	}
	static public class EntryPhotoColumnRenderer implements TableCellRenderer {
		private ArrayList<ImageDisplayPanel> rowPhotoPanels = new ArrayList<ImageDisplayPanel>();
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			ImageDisplayPanel idp = null;
			// theLogger.info("fetchingRenderer for row=" + row + ", col=" + column);
			// theLogger.info("value=" + value);
			while (rowPhotoPanels.size() < row + 1) {
				rowPhotoPanels.add(null);
			}
			idp = rowPhotoPanels.get(row);
			if (idp == null) {
				idp = new ImageDisplayPanel();
				rowPhotoPanels.add(null);
			}
			Image pimg = (Image) value;
			idp.setImage(pimg);
			return idp;
		}
	}
	public void initListeners(final JTable table) {	
		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				theLogger.info("entry selection change - clearing status");
				myFBI.setFriendOpStatus("status: browsing");
				myFBI.setEntryOpStatus("status: browsing");
			}
		});
	}
}

