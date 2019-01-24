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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;

import com.cognos.CAM_AAA.authentication.IAccount;
import com.cognos.CAM_AAA.authentication.INamespace;
import com.cognos.CAM_AAA.authentication.IQueryOption;
import com.cognos.CAM_AAA.authentication.ISearchFilter;
import com.cognos.CAM_AAA.authentication.ISearchFilterConditionalExpression;
import com.cognos.CAM_AAA.authentication.ISearchFilterFunctionCall;
import com.cognos.CAM_AAA.authentication.ISearchFilterRelationExpression;
import com.cognos.CAM_AAA.authentication.ISortProperty;
import com.cognos.CAM_AAA.authentication.QueryResult;
import com.cognos.CAM_AAA.authentication.UnrecoverableException;
import com.ibm.cognos.auth.adapters.Account;
import com.ibm.cognos.auth.adapters.Group;
import com.ibm.cognos.auth.adapters.Visa;
import com.ibm.cognos.auth.util.DBAuthLogger;
import com.ibm.cognos.auth.util.EncryptUtil;

public class QueryUtil {

	private static void addSQLConditionFragment(final StringBuffer sqlCondition, final String columnName, final String likePattern) {
		sqlCondition.append(" " + columnName + " LIKE '" + likePattern + "' ESCAPE '!'");
	}

	public static Account createAccount(final ConnectionManager connectionManager, final String userName, final String password) throws UnrecoverableException {
		DBAuthLogger.debug("QueryUtil createAccount start.");
		
		String encPassword = EncryptUtil.getEncryptedString(connectionManager.encryptType, password);
		String userCheckSql;
		if(EncryptUtil.getCredentialPassword().equals(password)) {
			userCheckSql = "SELECT USER_ID FROM " + connectionManager.USER_TABLE + " WHERE USER_ID = ? AND '" + encPassword + "' = ?";
		} else {
			userCheckSql = "SELECT USER_ID FROM " + connectionManager.USER_TABLE + " WHERE USER_ID = ? AND USER_PW = ?";
		}
		final Object[][] data = QueryUtil.query(connectionManager, userCheckSql, userName, encPassword);
		final String userID = QueryUtil.getSingleStringResult(data);

		if (null != userID) {
			final String userSearchPath = "u:" + userID;
			DBAuthLogger.debug("QueryUtil createAccount end.");
			//return connectionManager.getAccountCache().findAccount(userSearchPath);
			return connectionManager.getAccountCache().findAccount(userSearchPath, "createAccount");
		} else {		
			DBAuthLogger.debug("QueryUtil createAccount end.");
			throw new UnrecoverableException("Invalid Credentials", "Could not authenticate with the provide credentials");
		}
	}

	public static String escapeSpecialChars(final String str) {
		final StringBuffer escapedString = new StringBuffer(str);
		final String bangApostrophe = "!'";
		final String bangPercent = "!%";
		
		for (int i = 0; i < escapedString.length();) {
			final char c = escapedString.charAt(i);

			switch (c) {
				case '\'':
					escapedString.insert(i, bangApostrophe);
					i += bangApostrophe.length() + 1;
					break;
				case '%':
					escapedString.insert(i, bangPercent);
					i += bangPercent.length() + 1;
					break;
				default:
					i++;
					break;
			}
		}

		return escapedString.toString();
	}

	private static Object[] getDataRow(final ResultSet resultSet) throws SQLException {
		final ResultSetMetaData rsMetaData = resultSet.getMetaData();
		final Object[] row = new Object[rsMetaData.getColumnCount()];

		for (int i = 0; i < row.length; ++i)
			row[i] = resultSet.getObject(i + 1);

		return row;
	}

	public static Locale getLocale(final String localeID) {
		if (2 > localeID.length())
			return Locale.ENGLISH;

		final String language = localeID.substring(0, 2);
		final int fullLocaleLength = 5;
		if (fullLocaleLength == localeID.length()) {
			final String country = localeID.substring(3, 5);
			return new Locale(language, country);
		}

		return new Locale(language);
	}

	private static Object[] getMetaDataRow(final ResultSet resultSet) throws SQLException {
		final ResultSetMetaData rsMetaData = resultSet.getMetaData();
		final Object[] metaDataRow = new Object[rsMetaData.getColumnCount()];
		for (int i = 0; i < metaDataRow.length; ++i)
			metaDataRow[i] = rsMetaData.getColumnName(i);

		return metaDataRow;
	}

	private static Object[] getSingleRowResult(final Object[][] data) {
		if (1 == data.length)
			return data[0];

		return null;
	}

	private static String getSingleStringResult(final Object[][] data) {
		final Object[] row = QueryUtil.getSingleRowResult(data);

		if (null != row && 1 == row.length)
			return String.valueOf(row[0]);

		return null;
	}

	public static String getSqlCondition(final ISearchFilter theSearchFilter) {
		final String falseCondition = " 1 = 0 ";
		final String trueCondition = "1 = 1 ";

		final StringBuffer sqlCondition = new StringBuffer();
		if (theSearchFilter != null)
			switch (theSearchFilter.getSearchFilterType()) {
				case ISearchFilter.ConditionalExpression:
					final ISearchFilterConditionalExpression item = (ISearchFilterConditionalExpression) theSearchFilter;
					final String operator = item.getOperator();
					final ISearchFilter[] filters = item.getFilters();
					if (filters.length > 0) {
						sqlCondition.append("( ");
						sqlCondition.append(QueryUtil.getSqlCondition(filters[0]));
						for (int i = 1; i < filters.length; i++) {
							sqlCondition.append(' ');
							sqlCondition.append(operator);
							sqlCondition.append(' ');
							sqlCondition.append(QueryUtil.getSqlCondition(filters[i]));
						}
						sqlCondition.append(" )");
					}
					
					break;
				case ISearchFilter.FunctionCall:
					final ISearchFilterFunctionCall functionItem = (ISearchFilterFunctionCall) theSearchFilter;
					final String functionName = functionItem.getFunctionName();
					if (functionName.equals(ISearchFilterFunctionCall.Contains)) {
						final String[] parameter = functionItem.getParameters();
						final String propertyName = parameter[0];
						final String value = parameter[1];
						final String likePattern = "%" + QueryUtil.escapeSpecialChars(value) + "%";

						if (propertyName.equals("@objectClass")) {
							if (-1 != "account".indexOf(value))
								sqlCondition.append(" ISUSER = 1 ");
							else if (-1 != "group".indexOf(value))
								sqlCondition.append(" ISGROUP = 1 ");
							else
								sqlCondition.append(falseCondition);

						}
						else if (propertyName.equals("@defaultName") || propertyName.equals("@name"))
							QueryUtil.addSQLConditionFragment(sqlCondition, "NAME", likePattern);
						else if (propertyName.equals("@userName"))
							QueryUtil.addSQLConditionFragment(sqlCondition, "USERNAME", likePattern);
						else
							sqlCondition.append(trueCondition);
					} else if (functionName.equals(ISearchFilterFunctionCall.StartsWith)) {
						final String[] parameter = functionItem.getParameters();
						final String propertyName = parameter[0];
						final String value = parameter[1];
						final String likePattern = QueryUtil.escapeSpecialChars(value) + "%";

						if (propertyName.equals("@objectClass")) {
							if ("account".startsWith(value))
								sqlCondition.append(" ISUSER = 1 ");
							else if ("group".startsWith(value))
								sqlCondition.append(" ( ISGROUP = 1 ) ");
							else
								//
								// Make sure this is a false statement
								//
								sqlCondition.append(falseCondition);
						} else if (propertyName.equals("@defaultName") || propertyName.equals("@name"))
							QueryUtil.addSQLConditionFragment(sqlCondition, "NAME", likePattern);
						else if (propertyName.equals("@userName"))
							QueryUtil.addSQLConditionFragment(sqlCondition, "USERNAME", likePattern);
						else
							//
							// We ignore the properties that are not
							// supported.
							//
							sqlCondition.append(trueCondition);
					} else if (functionName.equals(ISearchFilterFunctionCall.EndsWith)) {
						final String[] parameter = functionItem.getParameters();
						final String propertyName = parameter[0];
						final String value = parameter[1];
						if (propertyName.equals("@objectClass")) {
							if ("account".endsWith(value))
								sqlCondition.append(" ISUSER = 1 ");
							else if ("group".endsWith(value))
								sqlCondition.append(" ( ISGROUP = 1 ) ");
							else
								sqlCondition.append(falseCondition);
						}
						else if (propertyName.equals("@defaultName") || propertyName.equals("@name"))
							QueryUtil.addSQLConditionFragment(sqlCondition, "NAME", "%" + QueryUtil.escapeSpecialChars(value));
						else if (propertyName.equals("@userName"))
							QueryUtil.addSQLConditionFragment(sqlCondition, "USERNAME", "%" + QueryUtil.escapeSpecialChars(value));
						else
							sqlCondition.append(trueCondition);
					} else
						sqlCondition.append(trueCondition);
					
					break;
				case ISearchFilter.RelationalExpression:
					final ISearchFilterRelationExpression relationalItem = (ISearchFilterRelationExpression) theSearchFilter;
					final String propertyName = relationalItem.getPropertyName();
					final String constraint = relationalItem.getConstraint();
					final String relationalOperator = relationalItem.getOperator();
					if (propertyName.equals("@objectClass")) {
						if (constraint.equals("account")) {
							if (relationalOperator.equals(ISearchFilterRelationExpression.EqualTo))
								sqlCondition.append(" ISUSER = 1 ");
							else if (relationalOperator.equals(ISearchFilterRelationExpression.NotEqual))
								sqlCondition.append(" ISUSER = 0 ");
							else
								//
								// Make sure this is a false statement
								//
								sqlCondition.append(falseCondition);
						} else if (constraint.equals("group")) {
							if (relationalOperator.equals(ISearchFilterRelationExpression.EqualTo))
								sqlCondition.append(" ( ISGROUP = 1 ) ");
							else if (relationalOperator.equals(ISearchFilterRelationExpression.NotEqual))
								sqlCondition.append(" ( ISGROUP = 0 ) ");
							else
								sqlCondition.append(falseCondition);
						} else
							sqlCondition.append(falseCondition);
					} else if (propertyName.equals("@defaultName") || propertyName.equals("@name"))
						sqlCondition.append(" NAME " + relationalOperator + " '" + constraint + "'");
					else if (propertyName.equals("@userName"))
						sqlCondition.append(" USERNAME " + relationalOperator + " '" + constraint + "'");
					else
						sqlCondition.append(trueCondition);
				
					break;
			}

		return sqlCondition.toString();
	}

	public static Object[][] query(ConnectionManager connectionManager, final String sql, final Object... parameters) throws UnrecoverableException {
		return QueryUtil.queryImpl(connectionManager, sql, false, parameters);
	}

	private static Object[][] queryImpl(ConnectionManager connectionManager, final String sql, final boolean includeMetadata,
			final Object... parameters) throws UnrecoverableException {
		DBAuthLogger.debug("QueryUtil query start.");
		DBAuthLogger.debug("#### query sql : " + sql);
		for (int i = 0; i < parameters.length; ++i) {
			DBAuthLogger.debug("#### query sql param[" + i + "] : " + parameters[i]);
		}

		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		final List< Object[] > data = new ArrayList< Object[] >();

		try {
			conn = connectionManager.getConnection();
			statement = conn.prepareStatement(sql);
			for (int i = 0; i < parameters.length; ++i) {
				statement.setObject(i + 1, parameters[i]);
			}

			resultSet = statement.executeQuery();

			if (includeMetadata)
				data.add(QueryUtil.getMetaDataRow(resultSet));

			while (resultSet.next())
				data.add(QueryUtil.getDataRow(resultSet));
		} catch (final SQLException ex) {
			throw new UnrecoverableException("SQL Exception", "An exception was caught while querying the authentication database.");
		} finally {
			if(resultSet != null) try { resultSet.close(); } catch(Exception e) {}
			if(statement != null) try { statement.close(); } catch(Exception e) {}
			connectionManager.closeConnection(conn);
		}

		DBAuthLogger.debug("QueryUtil query end.");
		return data.toArray(new Object[0][]);
	}

	public static Object[][] queryWithMetaData(final ConnectionManager connectionManager, final String sql, final Object... parameters) throws UnrecoverableException {
		return QueryUtil.queryImpl(connectionManager, sql, true, parameters);
	}

	public static void searchQuery(final ConnectionManager connectionManager, final String theSqlCondition,
			final IQueryOption theQueryOption, final String[] theProperties, final ISortProperty[] theSortProperties,
			final QueryResult theResult, final INamespace theNamespace) throws SQLException, UnrecoverableException {
		DBAuthLogger.debug("QueryUtil searchQuery start.");
		
		// searchQuery
		final StringBuffer sqlStatement = new StringBuffer();
		sqlStatement.append("SELECT ID, NAME, ISUSER, ISGROUP FROM ( \n");
		sqlStatement.append("    SELECT USER_ID AS ID, USER_NM AS NAME, 1 AS ISUSER, 0 AS ISGROUP FROM " + connectionManager.USER_TABLE + " \n");
		sqlStatement.append("    UNION ALL \n");
		sqlStatement.append("    SELECT GROUP_ID AS ID, GROUP_NM AS NAME, 0 AS ISUSER, 1 AS ISGROUP FROM " + connectionManager.GROUP_TABLE + " \n");
		sqlStatement.append(") T1 \n");
		if (theSqlCondition.length() > 0)
			sqlStatement.append(" WHERE ").append(theSqlCondition);

		final long maxCount = theQueryOption.getMaxCount();
		final long skipCount = theQueryOption.getSkipCount();

		String theSortClause = new String();
		if (theSortProperties != null) {
			for (int i = 0; i < theSortProperties.length; i++) {
				final ISortProperty property = theSortProperties[i];
				if (property.getPropertyName().equals("name")) {
					if (theSortClause.length() > 0)
						theSortClause += ", ";

					theSortClause += "NAME";
					if (property.getSortOrder() == ISortProperty.SortOrderAscending)
						theSortClause += " ASC";
					else
						theSortClause += " DESC";
				}
			}

			if (theSortClause.length() > 0)
				sqlStatement.append(" ORDER BY ").append(theSortClause);
		}

		final Object[][] data = QueryUtil.query(connectionManager, sqlStatement.toString());
		if (0 < data.length) {
			long curSkip = 0, curMax = 0;
			final int isUserCol = 2;
			final int isGroupCol = 3;

			for (int i = 0; i < data.length; i++) {
				final Object[] row = data[i];
				
				boolean bIsUser = false;
				boolean bIsGroup = false;
				
				if(row[isUserCol] instanceof Integer) {
					bIsUser = ((Integer) row[isUserCol]).intValue() == 1;
					bIsGroup = ((Integer) row[isGroupCol]).intValue() == 1;
				} else {
					bIsUser = ((BigDecimal) row[isUserCol]).intValue() == 1;
					bIsGroup = ((BigDecimal) row[isGroupCol]).intValue() == 1;
				}

				// We need to handle paging information
				if (bIsUser || bIsGroup) {
					if (curSkip++ < skipCount) // We need to skip skipCount
												// first objects
						continue;
					else if (curMax >= maxCount && maxCount > 0) // If we
																	// already
																	// have
																	// maxCount
																	// objects,
																	// we can
																	// stop
																	// looking
						break;
					else
						// curMax < maxCount - we need to keep retrieving
						// entries
						curMax++;
				}
				else
					// If the entry is neither a user nor a role, we'll skip it
					continue;

				final String objectID = String.valueOf(row[0]);
				final String objectName = (String) row[1];

				if (bIsUser) {
					final String searchPath = "u:" + objectID;
					final Account account = connectionManager.getAccountCache().findAccount(searchPath);
					account.addName(Locale.getDefault(), objectName);
					account.setUserName(objectID);

					// The following two custom properties used for testing
					// purposes
					account.addCustomProperty("newProp1", "value1");
					account.addCustomProperty("newProp2", "value2");

					theResult.addObject(account);
				} else if (bIsGroup) {
					final String searchPath = "g:" + objectID;
					final Group group = connectionManager.getGroupCache().findGroup(searchPath);
					group.addName(Locale.getDefault(), objectName);
					theResult.addObject(group);
				}
			}
		}
		DBAuthLogger.debug("QueryUtil searchQuery end.");
	}

	public static void updateMembership(final ConnectionManager connectionManager, final Visa theVisa) throws SQLException, UnrecoverableException {
		DBAuthLogger.debug("QueryUtil updateMembership start.");
		
		final IAccount account = theVisa.getAccount();
		final String userID = account.getObjectID().substring(2);
		final Object[][] data =
				QueryUtil.query(connectionManager, "SELECT T1.GROUP_ID, T1.GROUP_NM FROM " + connectionManager.GROUP_TABLE + " T1, " + connectionManager.USER_GROUP_MAPP + " T2 WHERE T1.GROUP_ID = T2.GROUP_ID AND T2.USER_ID = ?", userID);

		for (int i = 0; i < data.length; ++i) {
			final Object[] row = data[i];
			final String groupID = String.valueOf(row[0]);
			final String groupName = (String) row[1];
			//final Group group = connectionManager.getGroupCache().findGroup("g:" + groupID);
			final Group group = connectionManager.getGroupCache().findGroup("g:" + groupID, "u:" + userID);
			group.addName(account.getContentLocale(), groupName);
			theVisa.addGroup(group);
		}
		DBAuthLogger.debug("QueryUtil updateMembership end.");
	}
}
