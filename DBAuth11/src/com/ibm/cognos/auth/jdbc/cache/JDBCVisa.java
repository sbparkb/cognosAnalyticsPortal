/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2005, 2012
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.ibm.cognos.auth.jdbc.cache;

import java.sql.SQLException;

import com.cognos.CAM_AAA.authentication.IBiBusHeader;
import com.cognos.CAM_AAA.authentication.ICredential;
import com.cognos.CAM_AAA.authentication.ITrustedCredential;
import com.cognos.CAM_AAA.authentication.ReadOnlyDisplayObject;
import com.cognos.CAM_AAA.authentication.SystemRecoverableException;
import com.cognos.CAM_AAA.authentication.TextNoEchoDisplayObject;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.cognos.CAM_AAA.authentication.UserRecoverableException;
import com.ibm.cognos.auth.adapters.Account;
import com.ibm.cognos.auth.adapters.Visa;
import com.ibm.cognos.auth.adapters.Credential;
import com.ibm.cognos.auth.adapters.TrustedCredential;
import com.ibm.cognos.auth.util.DBAuthLogger;
import com.ibm.cognos.auth.util.EncryptUtil;

public class JDBCVisa extends Visa {
	private ConnectionManager connectionManager;
	private String password;
	private String username;

	public JDBCVisa() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.provider.IVisa#generateCredential(com.cognos.CAM_AAA .objectModel.IAuthenticateRequest)
	 */
	@Override
	public ICredential generateCredential(final IBiBusHeader theAuthRequest) throws UserRecoverableException,
			SystemRecoverableException, UnrecoverableException {
		DBAuthLogger.debug("JDBCVisa generateCredential start.");
		if (!this.validateConnection(this.username, this.password)) {
			final UnrecoverableException e =
					new UnrecoverableException("Could not generate credentials for the user.", "Visa contains invalid credentials.");
			throw e;
		}
		final Credential credentials = new Credential();
		credentials.addCredentialValue("username", this.username);
		credentials.addCredentialValue("password", EncryptUtil.getCredentialPassword());
		DBAuthLogger.debug("JDBCVisa generateCredential end.");
		return credentials;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.authentication.IVisa#generateTrustedCredential(com .cognos.CAM_AAA.authentication.IBiBusHeader)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cognos.CAM_AAA.provider.IVisa#generateTrustedCredential(com.cognos .CAM_AAA.objectModel.IAuthenticateRequest)
	 */
	@Override
	public ITrustedCredential generateTrustedCredential(final IBiBusHeader theAuthRequest) throws UserRecoverableException,
			SystemRecoverableException, UnrecoverableException {
		DBAuthLogger.debug("JDBCVisa generateTrustedCredential start.");
		// 1 - Look for credentials coming from SDK request
		JDBCAuth.Credential credential = JDBCAuth.getCredentialValues(theAuthRequest);
		if (credential.isEmpty()) {
			// 2 - Look for credentials in formfield
			credential = JDBCAuth.getFormFieldValues(theAuthRequest);
		}

		if (credential.isEmpty() || !credential.getUsername().equals(username)) {
			credential.setUsername(username);
			credential.setPassword(password);
		}

		if (!validateConnection(credential)) {
			final UserRecoverableException e =
					new UserRecoverableException("Please type your credentials for authentication.", "The provided credentials are invalid.");
			e.addDisplayObject(new ReadOnlyDisplayObject("User ID:", "CAMUsername", this.username));
			e.addDisplayObject(new TextNoEchoDisplayObject("Password:", "CAMPassword"));
			throw e;
		}
		final TrustedCredential tc = new TrustedCredential();
		tc.addCredentialValue("username", this.username);
		tc.addCredentialValue("password", EncryptUtil.getCredentialPassword());
		DBAuthLogger.debug("JDBCVisa generateTrustedCredential end.");
		return tc;
	}

	public void init(final JDBCAuth theNamespace, final ConnectionManager theConnectionManager, final String theUsername, final String thePassword) throws UnrecoverableException {
		try {
			connectionManager = theConnectionManager;
			// Create account object for the user.
			final Account account = QueryUtil.createAccount(connectionManager, theUsername, thePassword);
			super.init(account);
			QueryUtil.updateMembership(connectionManager, this);
			this.username = theUsername;
			this.password = thePassword;
		} catch (final SQLException e) {
			throw new UnrecoverableException("Connection Error", "Database connection failure. Reason: " + ConnectionManager.getSqlExceptionDetails(e));
		}
	}

	private boolean validateConnection(final JDBCAuth.Credential credential) {
		return validateConnection(credential.getUsername(), credential.getPassword());
	}

	private boolean validateConnection(final String theUsername, final String thePassword) {
		try {
			//QueryUtil.createAccount(connectionManager, theUsername, thePassword);
			username = theUsername;
			password = thePassword;
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
}
