package com.ibm.cognos.auth.jdbc.cache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cognos.CAM_AAA.authentication.IBaseClass;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.ibm.cognos.auth.adapters.Group;
import com.ibm.cognos.auth.util.DBAuthLogger;

public class GroupCache {

	private final AccountCache accountCache;

	private final ConnectionManager connectionManager;

	private final Map< String, Group > groups = new HashMap< String, Group >();

	public GroupCache(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		this.accountCache = connectionManager.getAccountCache();
	}

	protected Group createGroup(final String groupID) throws UnrecoverableException {
		DBAuthLogger.debug("GroupCache createGroup start.");

		final Group group = new Group(groupID);
		this.setGroupProperties(group);

		this.groups.put(groupID, group);

		DBAuthLogger.debug("GroupCache createGroup end.");
		return group;
	}

	public synchronized Group findGroup(final String groupID) throws UnrecoverableException {
		DBAuthLogger.debug("GroupCache findGroup(groupID) start.");
		
		Group group = this.groups.get(groupID);

		if (null == group)
			group = this.createGroup(groupID);

		DBAuthLogger.debug("GroupCache findGroup(groupID) end.");
		return group;
	}
	
	public synchronized Group findGroup(final String groupID, final String userID) throws UnrecoverableException {
		DBAuthLogger.debug("GroupCache findGroup(groupID,userID) start.");
		
		Group group = this.groups.get(groupID);
		
		if (null == group)
			group = this.createGroup(groupID);
		else {
			boolean isExists = false;
			for(IBaseClass account : group.getMembers()) {
				if(userID.equals(account.getObjectID())) {
					isExists = true;
					break;
				}
			}
			if(!isExists) {
				group.addMember(this.accountCache.findAccount(userID));
			}
		}

		DBAuthLogger.debug("GroupCache findGroup(groupID,userID) end.");
		return group;
	}

	protected void setGroupProperties(final Group group) throws UnrecoverableException {
		DBAuthLogger.debug("GroupCache setGroupProperties start.");

		final String groupIDStr = group.getObjectID();
		final String groupID = groupIDStr.substring(2);

		//Select GROUPNAME & USERID and exclude any users with a tenantId not public or available in the group.
		final Object[][] data = QueryUtil.query(this.connectionManager, "SELECT GROUP_NM FROM " + this.connectionManager.GROUP_TABLE + " WHERE GROUP_ID = ?", groupID);
		
		if (0 < data.length) {
			group.addName(Locale.getDefault(), (String) data[0][0]);
			DBAuthLogger.debug("#### Group Name : " + (String) data[0][0]);
		}

		final Object[][] data2 = QueryUtil.query(this.connectionManager, "SELECT USER_ID FROM " + this.connectionManager.USER_GROUP_MAPP + " WHERE GROUP_ID = ?", groupID);
		for (int i = 0; i < data2.length; ++i) {
			final Object[] row = data2[i];
			group.addMember(this.accountCache.findAccount("u:" + (String)row[0]));
			DBAuthLogger.debug("#### Group UserID : " + (String)row[0]);
		}
		
		DBAuthLogger.debug("GroupCache setGroupProperties end.");
	}
}
