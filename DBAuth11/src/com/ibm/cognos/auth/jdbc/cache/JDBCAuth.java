/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: CAMAAA
 * 
 * (C) Copyright IBM Corp. 2005, 2015
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.ibm.cognos.auth.jdbc.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Locale;

import com.cognos.CAM_AAA.authentication.IBiBusHeader;
import com.cognos.CAM_AAA.authentication.IBiBusHeader2;
import com.cognos.CAM_AAA.authentication.INamespaceAuthenticationProvider2;
import com.cognos.CAM_AAA.authentication.INamespaceConfiguration;
import com.cognos.CAM_AAA.authentication.IQuery;
import com.cognos.CAM_AAA.authentication.IQueryResult;
import com.cognos.CAM_AAA.authentication.ISearchExpression;
import com.cognos.CAM_AAA.authentication.ISearchFilter;
import com.cognos.CAM_AAA.authentication.ISearchStep;
import com.cognos.CAM_AAA.authentication.ISearchStep.SearchAxis;
import com.cognos.CAM_AAA.authentication.IVisa;
import com.cognos.CAM_AAA.authentication.QueryResult;
import com.cognos.CAM_AAA.authentication.ReadOnlyDisplayObject;
import com.cognos.CAM_AAA.authentication.SystemRecoverableException;
import com.cognos.CAM_AAA.authentication.TextDisplayObject;
import com.cognos.CAM_AAA.authentication.TextNoEchoDisplayObject;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.cognos.CAM_AAA.authentication.UserRecoverableException;
import com.ibm.cognos.auth.adapters.Namespace;
import com.ibm.cognos.auth.util.DBAuthLogger;

/**
 * A Namespace implementation that uses JDBC to connect to a database that contains users and groups.
 */
public class JDBCAuth extends Namespace implements INamespaceAuthenticationProvider2 {

	/**
	 * A utility class for handling username and password pairs.
	 */
	static class Credential {
		private String password;
		private String username;

		public String getPassword() {
			return this.password;
		}

		public String getUsername() {
			return this.username;
		}

		public boolean isEmpty() {
			return null == this.getUsername() && null == this.getPassword();
		}

		public void setPassword(final String thePassword) {
			this.password = thePassword;
		}

		public void setUsername(final String theUsername) {
			this.username = theUsername;
		}
	}

	protected static Credential getCredentialValues(final IBiBusHeader authRequest) {
		final Credential credential = new Credential();
		final String[] usernames = authRequest.getCredentialValue("username");
		final String[] passwords = authRequest.getCredentialValue("password");

		if (null != usernames && 0 < usernames.length)
			credential.setUsername(usernames[0]);

		if (null != passwords && 0 < passwords.length)
			credential.setPassword(passwords[0]);

		return credential;
	}

	private static String getErrorDetails(final UnrecoverableException e) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(out);

		ps.println(e.getClass().getName() + " : ");
		final String[] messages = e.getMessages();
		for (int i = 0; i < messages.length; i++)
			ps.println(messages[i]);
		ps.close();
		return out.toString();
	}

	protected static Credential getFormFieldValues(final IBiBusHeader authRequest) {
		final Credential credential = new Credential();
		final String[] usernames = authRequest.getFormFieldValue("CAMUsername");
		final String[] passwords = authRequest.getFormFieldValue("CAMPassword");

		if (null != usernames && 0 < usernames.length)
			credential.setUsername(usernames[0]);

		if (null != passwords && 0 < passwords.length)
			credential.setPassword(passwords[0]);

		return credential;
	}

	protected static Credential getTrustedCredentialValues(final IBiBusHeader2 authRequest) {
		final Credential credential = new Credential();
		final String[] usernames = authRequest.getTrustedCredentialValue("username");
		final String[] passwords = authRequest.getTrustedCredentialValue("password");

		if (null != usernames && 0 < usernames.length)
			credential.setUsername(usernames[0]);

		if (null != passwords && 0 < passwords.length)
			credential.setPassword(passwords[0]);

		return credential;
	}

	protected static Credential getTrustedEnvironmentVaribleValue(final IBiBusHeader2 authRequest) {
		final Credential credential = new Credential();
		final String[] userNames;
		userNames = authRequest.getTrustedEnvVarValue("REMOTE_USER");

		if (userNames != null && userNames.length > 0) {
			credential.setUsername(userNames[0]);
			// To simplified the case, we set up the password the same as userName
			credential.setPassword(userNames[0]);
		}

		return credential;
	}

	ConnectionManager connectionManager = new ConnectionManager();

//	public Connection getConnection() {
//		return this.connectionManager.getConnection();
//	}

	/*
	 * @see com.cognos.CAM_AAA.provider.INamespace#init(com.cognos.CAM_AAA.provider .INamespaceConfiguration)
	 */
	@Override
	public void init(final INamespaceConfiguration theNamespaceConfiguration) throws UnrecoverableException {
		DBAuthLogger.debug("JDBCAuth init start.");
		
		super.init(theNamespaceConfiguration);

		this.addName(Locale.getDefault(), theNamespaceConfiguration.getDisplayName());

		try {
			this.connectionManager.init(theNamespaceConfiguration);
		} catch (final IOException e) {
			DBAuthLogger.error("JDBCAuth init Error1 : " + e);
			throw new UnrecoverableException("Configuration Error", "Provider initialization failure. Reason: " + e.toString());
		}
		
		DBAuthLogger.debug("JDBCAuth init end.");
	}

	/*
	 * @see com.cognos.CAM_AAA.provider.INamespaceAuthenticationProvider#logoff(com .cognos.CAM_AAA.provider.IVisa,
	 * com.cognos.CAM_AAA.provider.IBiBusHeader)
	 */
	public void logoff(final IVisa theVisa, final IBiBusHeader theAuthRequest) {
		DBAuthLogger.debug("JDBCAuth logoff start.");
		try {
			// We can safely assume that we'll get back the same Visa that we
			// issued.
			final JDBCVisa visa = (JDBCVisa) theVisa;
			visa.destroy();
		} catch (final UnrecoverableException e) {
			e.printStackTrace();
		}
		DBAuthLogger.debug("JDBCAuth logoff end.");
	}

	/*
	 * @see com.cognos.CAM_AAA.provider.INamespaceAuthenticationProvider2#logon(com .cognos.CAM_AAA.provider.IBiBusHeader2)
	 */
	public IVisa logon(final IBiBusHeader2 theAuthRequest) throws UserRecoverableException, SystemRecoverableException, UnrecoverableException {
		DBAuthLogger.debug("JDBCAuth logon start.");
		JDBCVisa visa = null;

		// 1 - Look for trusted credentials
		Credential credential = JDBCAuth.getTrustedCredentialValues(theAuthRequest);
		if (credential.isEmpty()) {
			DBAuthLogger.debug("#### getTrustedCredentialValues Empty");
			credential = JDBCAuth.getCredentialValues(theAuthRequest);
		}

		if (credential.isEmpty()) {
			DBAuthLogger.debug("#### getCredentialValues Empty");
			credential = JDBCAuth.getFormFieldValues(theAuthRequest);
		}

		// here, should be something for the single signon case
		if (credential.isEmpty() && this.connectionManager.singleSignOnEnabled()) {
			DBAuthLogger.debug("#### getFormFieldValues Empty & SingleSignOn");
			credential = JDBCAuth.getTrustedEnvironmentVaribleValue(theAuthRequest);
		}

		if (credential.isEmpty() && this.connectionManager.singleSignOnEnabled()) {
			DBAuthLogger.debug("#### getTrustedEnvironmentVaribleValue Empty & SingleSignOn");
			// null implies the provider has to start the dance so throw a SysRecov.
			// the SysRecov needs to have the name of the variable we look for in
			// the second parameter
			SystemRecoverableException e = new SystemRecoverableException("Challenge for REMOTE_USER", "REMOTE_USER");
			throw e;
		}

		if (credential.isEmpty()) {
			DBAuthLogger.debug("#### Finally Credential Empty !!!");
			// Assume this is the initial logon and pass null for errorDetails
			generateAndThrowExceptionForLogonPrompt(null);
		}

		try {
			//
			// Create a Visa for the new user.
			//
			visa = new JDBCVisa();
			visa.init(this, this.connectionManager, credential.getUsername(), credential.getPassword());
		} catch (final UnrecoverableException ex) {
			DBAuthLogger.error("JDBCAuth logon Error : " + ex.getMessage());
			DBAuthLogger.error("JDBCAuth logon Username : " + credential.getUsername());
			DBAuthLogger.error("JDBCAuth logon Password : " + credential.getPassword());
			final String errorDetails = JDBCAuth.getErrorDetails(ex);

			// Something went wrong, probably because the user's credentials
			// are invalid.
			generateAndThrowExceptionForLogonPrompt(errorDetails);
		}
		
		DBAuthLogger.debug("JDBCAuth logon end.");
		return visa;
	}

	/*
	 * @see com.cognos.CAM_AAA.provider.INamespaceAuthenticationProvider#search(com .cognos.CAM_AAA.provider.IVisa,
	 * com.cognos.CAM_AAA.provider.IQuery)
	 */
	public IQueryResult search(final IVisa theVisa, final IQuery theQuery) throws UnrecoverableException {
		DBAuthLogger.debug("JDBCAuth search start.");
		
		// We can safely assume that we'll get back the same Visa that we
		// issued.
		final JDBCVisa visa = (JDBCVisa) theVisa;
		final QueryResult result = new QueryResult();
		try {
			final ISearchExpression expression = theQuery.getSearchExpression();
			final String objectID = expression.getObjectID();
			final ISearchStep[] steps = expression.getSteps();
			// It doesn't make sense to have multiple steps for this provider
			// since the objects are not hierarchical.
			if (steps.length != 1)
				throw new UnrecoverableException("Internal Error","Invalid search expression. Multiple steps is not supported for this namespace.");
			final StringBuffer sqlCondition = new StringBuffer();
			final int searchType = steps[0].getAxis();
			final ISearchFilter filter = steps[0].getPredicate();
			switch (searchType) {
				case SearchAxis.Self:
				case SearchAxis.DescendentOrSelf:
					if (objectID == null) {
						if (filter == null || this.matchesFilter(filter))
							result.addObject(this);
						// Add current namespace
						if (searchType == SearchAxis.Self) {
							DBAuthLogger.debug("JDBCAuth search end.(SearchAxis.Self)");
							return result;
						} else
							sqlCondition.append(QueryUtil.getSqlCondition(filter));
					} else if (objectID.startsWith("u:") && objectID.equals(visa.getAccount().getObjectID())) {
						if (filter == null || this.matchesFilter(filter))
							result.addObject(visa.getAccount());
						// Add current user
						DBAuthLogger.debug("JDBCAuth search end.(objectID.startsWith('u')");
						return result;
					} else if (objectID.startsWith("u:")) {
						final String sqlID = objectID.substring(2);
						sqlCondition.append(QueryUtil.getSqlCondition(filter));
						if (sqlCondition.length() > 0)
							sqlCondition.append(" AND ");
						sqlCondition.append("ID = '" + sqlID + "' AND ISUSER = 1");
					} else if (objectID.startsWith("g:")) {
						final String sqlID = objectID.substring(2);
						sqlCondition.append(QueryUtil.getSqlCondition(filter));
						if (sqlCondition.length() > 0)
							sqlCondition.append(" AND ");
						sqlCondition.append("ID = '" + sqlID + "' AND ISGROUP = 1");
					}

					break;
				default:
					sqlCondition.append(QueryUtil.getSqlCondition(filter));

					break;
			}
			QueryUtil.searchQuery(this.connectionManager, sqlCondition.toString(), theQuery.getQueryOption(),
					theQuery.getProperties(), theQuery.getSortProperties(), result, this);
		} catch (final SQLException e) {
			e.printStackTrace();
		} catch (final UnrecoverableException e) {
			e.printStackTrace();
		}

		DBAuthLogger.debug("JDBCAuth search end.");
		return result;
	}

	/*
	 * Generate an exception with applicable display objects for the login prompt Note: If this is the initial logon, to avoid an
	 * empty logon log, be sure to throw either: a) a UserRecoverableException with null errorDetails b) a
	 * SystemRecoverableException
	 */
	private void generateAndThrowExceptionForLogonPrompt(String errorDetails) throws UserRecoverableException {
		final UserRecoverableException e = new UserRecoverableException("Please type your credentials for authentication.", errorDetails);
		e.addDisplayObject(new ReadOnlyDisplayObject("Namespace:", "CAMNamespaceDisplayName", this.getName(Locale.getDefault())));
		e.addDisplayObject(new TextDisplayObject("User ID:", "CAMUsername"));
		e.addDisplayObject(new TextNoEchoDisplayObject("Password:", "CAMPassword"));
		throw e;
	}
}
