package com.ibm.cognos.auth.jdbc.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.cognos.CAM_AAA.authentication.INamespaceConfiguration;
import com.ibm.cognos.auth.util.DBAuthLogger;

public class ConnectionManager {
	private AccountCache accountCache;
	private GroupCache groupCache;
	
	private String dbConnString;
	private String dbDriver;
	private String dbUsername;
	private String dbPassword;
	private String singleSignon;
	private String dbConnType;
	public String encryptType;
	
	// Table Setting
	// User Info
	public String USER_TABLE = "BI_USER_INFO";
	// Group Info
	public String GROUP_TABLE = "BI_GROUP_INFO";
	// User Group Mapping
	public String USER_GROUP_MAPP = "BI_USER_GROUP_MAPP";
	
	public ConnectionManager() {}

	public static String getSqlExceptionDetails(final SQLException e) {
		final StringBuffer buffer = new StringBuffer();

		for (SQLException se = e; null != se; se = se.getNextException()) {
			buffer.append("SQL STATE: " + se.getSQLState());
			buffer.append("ERROR CODE: " + se.getErrorCode());
			buffer.append("MESSAGE: " + se.getMessage());
			buffer.append("\n");
		}

		return buffer.toString();
	}

	public void init(final INamespaceConfiguration configuration) throws IOException {
		DBAuthLogger.debug("ConnectionManager init start.");
		this.loadProperties(configuration);
		DBAuthLogger.debug("ConnectionManager init end.");
	}

	private void createAccountCache() {
		this.accountCache = new AccountCache(this);
	}

	private void createGroupCache() {
		this.groupCache = new GroupCache(this);
	}

	public AccountCache getAccountCache() {
		if (null == this.accountCache)
			this.createAccountCache();

		return this.accountCache;
	}

	public GroupCache getGroupCache() {
		if (null == this.groupCache)
			this.createGroupCache();

		return this.groupCache;
	}

	public boolean singleSignOnEnabled() {
		if (singleSignon != null && singleSignon.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}

	private void loadProperties(final INamespaceConfiguration configuration) throws IOException {
		DBAuthLogger.debug("ConnectionManager loadProperties start.");
		
		Properties props = new Properties();

		String installLocation = configuration.getInstallLocation();
		File file = new File(installLocation + File.separator + "configuration" + File.separator + configuration.getID() + ".properties");
		if (file.exists())
			props.load(new FileInputStream(file));

		this.dbConnString    = props.getProperty("dbConnString");
		this.dbDriver        = props.getProperty("dbDriver");
		this.dbUsername      = props.getProperty("dbUsername");
		this.dbPassword      = props.getProperty("dbPassword");
		this.singleSignon    = props.getProperty("singleSignon");
		this.dbConnType      = props.getProperty("dbConnType");
		this.encryptType     = props.getProperty("encryptType");
		this.USER_TABLE      = props.getProperty("USER_TABLE");
		this.GROUP_TABLE     = props.getProperty("GROUP_TABLE");
		this.USER_GROUP_MAPP = props.getProperty("USER_GROUP_MAPP");
		
		DBAuthLogger.debug("#### dbConnString    : " + this.dbConnString);
		DBAuthLogger.debug("#### dbDriver        : " + this.dbDriver);
		DBAuthLogger.debug("#### dbUsername      : " + this.dbUsername);
		DBAuthLogger.debug("#### dbPassword      : " + this.dbPassword);
		DBAuthLogger.debug("#### singleSignon    : " + this.singleSignon);
		DBAuthLogger.debug("#### dbConnType      : " + this.dbConnType);
		DBAuthLogger.debug("#### encryptType     : " + this.encryptType);
		DBAuthLogger.debug("#### logMode         : " + props.getProperty("logMode"));
		DBAuthLogger.debug("#### logPath         : " + props.getProperty("logPath"));
		DBAuthLogger.debug("#### USER_TABLE      : " + this.USER_TABLE);
		DBAuthLogger.debug("#### GROUP_TABLE     : " + this.GROUP_TABLE);
		DBAuthLogger.debug("#### USER_GROUP_MAPP : " + this.USER_GROUP_MAPP);
		
		DBAuthLogger.debug("ConnectionManager loadProperties end.");
		DBAuthLogger.setLogMode(props.getProperty("logMode"));
		if(props.getProperty("logPath") != null && !"".equals(props.getProperty("logPath"))) {
			DBAuthLogger.setLogPath(props.getProperty("logPath"));
		}
	}

	public synchronized Connection getConnection() {
		DBAuthLogger.debug("ConnectionManager getConnection start.");
		Connection conn = null;
		try {
			if("INFO".equals(this.dbConnType)) {
				Class.forName(this.dbDriver);
				conn = DriverManager.getConnection(this.dbConnString, this.dbUsername, this.dbPassword);
			} else if("POOL".equals(this.dbConnType)) {
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DBAuth");
				conn = ds.getConnection();
			}
		} catch (ClassNotFoundException ex) {
			DBAuthLogger.error("ConnectionManager getConnection Error1 : " + ex);
		} catch (final SQLException ex) {
			DBAuthLogger.error("ConnectionManager getConnection Error2 : " + ex);
		} catch (NamingException ex) {
			DBAuthLogger.error("ConnectionManager getConnection Error3 : " + ex);
		}
			
		DBAuthLogger.debug("ConnectionManager getConnection end.");
		return conn;
	}

	public void closeConnection(Connection conn) {
		DBAuthLogger.debug("ConnectionManager closeConnection(conn) start.");
		if(conn != null) try { conn.close(); conn = null; } catch(Exception e) {}
		DBAuthLogger.debug("ConnectionManager closeConnection(conn) end.");
	}
}
