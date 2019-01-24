package com.ibm.cognos.auth.jdbc.cache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.ibm.cognos.auth.adapters.Account;
import com.ibm.cognos.auth.util.DBAuthLogger;

public class AccountCache {

	private final Map< String, Account > accounts = new HashMap< String, Account >();
	private final ConnectionManager connectionManager;

	public AccountCache(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	private Account createAccount(final String userID) throws UnrecoverableException {
		DBAuthLogger.debug("AccountCache createAccount start.");
		final Account account = new Account(userID);
		this.setAccountProperties(account);
		this.accounts.put(userID, account);

		DBAuthLogger.debug("AccountCache createAccount end.");
		return account;
	}

	public synchronized Account findAccount(final String userID) throws UnrecoverableException {
		DBAuthLogger.debug("AccountCache findAccount(userID) start.");
		Account account = this.accounts.get(userID);
		if (null == account)
			account = this.createAccount(userID);

		DBAuthLogger.debug("AccountCache findAccount(userID) end.");
		return account;
	}
	
	public synchronized Account findAccount(final String userID, final String method) throws UnrecoverableException {
		DBAuthLogger.debug("AccountCache findAccount(userID,method) start.");
		Account account = this.accounts.get(userID);
		if (null == account) {
			account = this.createAccount(userID);
		} else {
			accounts.remove(userID);
			account = this.createAccount(userID);
		}

		DBAuthLogger.debug("AccountCache findAccount(userID,method) end.");
		return account;
	}

	private void setAccountProperties(final Account account) throws UnrecoverableException {
		DBAuthLogger.debug("AccountCache setAccountProperties start.");

		final String userIDStr = account.getObjectID();
		final String userID = userIDStr.substring(2);

		final Object[][] data = QueryUtil.query(this.connectionManager, "SELECT USER_ID, USER_NM, USER_EMAIL, USER_LOCALE FROM " + this.connectionManager.USER_TABLE + " WHERE USER_ID = ?", userID);
		if (1 > data.length)
			return;

		final Object[] row = data[0];
		
		String userId = (String)row[0];
		String userNm = (String)row[1];
		String userEmail = (String)row[2];
		String userLocale = (String)row[3];
		
		account.setUserName(userId);
		account.setEmail(userEmail);
		account.setGivenName(userNm);

		final Locale locale = QueryUtil.getLocale(userLocale);
		account.setContentLocale(locale);
		account.setProductLocale(locale);

		account.addName(locale, userNm);
		
		DBAuthLogger.debug("AccountCache setAccountProperties end.");
	}
}
