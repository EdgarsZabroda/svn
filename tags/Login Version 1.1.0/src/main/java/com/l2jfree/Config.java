/* 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfree;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class containce global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * It's initialized at the very begin of startup, and later JIT will optimize
 * away debug/unused code.
 * 
 * @author mkizub
 */
public final class Config
{

	private static final Log	_log						= LogFactory.getLog(Config.class.getName());
	/** Enable/disable code 'in progress' */
	public static boolean		DEVELOPER;

	/** Number of login tries before IP ban gets activated, default 10*/
	public static int			LOGIN_TRY_BEFORE_BAN;
	/** Number of seconds the IP ban will last, default 10 minutes */
	public static int			LOGIN_BLOCK_AFTER_BAN;

	// Access to database
	/** Driver to access to database */
	public static String		DATABASE_DRIVER;
	/** Path to access to database */
	public static String		DATABASE_URL;
	/** Database login */
	public static String		DATABASE_LOGIN;
	/** Database password */
	public static String		DATABASE_PASSWORD;

	/** Configuration files */
	/** Properties file for login server configurations */
	public static final String	LOGIN_CONFIGURATION_FILE	= "./config/loginserver.properties";
	/** Properties file for the ID factory */
	public static final String	TELNET_FILE					= "./config/telnet.properties";

	/** Client login port/host */
	public static String		LOGIN_SERVER_HOSTNAME;
	public static int			LOGIN_SERVER_PORT;
	/** GameServer login port/host */
	public static String		LOGIN_HOSTNAME;
	public static int			LOGIN_PORT;

	/** Is telnet enabled ? */
	public static boolean		IS_TELNET_ENABLED;

	/** Show licence or not just after login (if false, will directly go to the Server List */
	public static boolean		SHOW_LICENCE;

	/** Accept new game server ? */
	public static boolean		ACCEPT_NEW_GAMESERVER;
	public static boolean		RESERVE_HOST_ON_LOGIN		= false;

	public static boolean		AUTO_CREATE_ACCOUNTS;
	public static int			GM_MIN;

	public static int			IP_UPDATE_TIME;

	public static boolean		FLOOD_PROTECTION;
	public static int			FAST_CONNECTION_LIMIT;
	public static int			NORMAL_CONNECTION_TIME;
	public static int			FAST_CONNECTION_TIME;
	public static int			MAX_CONNECTION_PER_IP;

	public static void load()
	{
		_log.info("loading login config");
		try
		{
			Properties serverSettings = new Properties();
			InputStream is = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE));
			serverSettings.load(is);
			is.close();

			LOGIN_SERVER_HOSTNAME = serverSettings.getProperty("LoginServerHostname", "0.0.0.0");
			LOGIN_SERVER_PORT = Integer.parseInt(serverSettings.getProperty("LoginServerPort", "2106"));
			LOGIN_HOSTNAME = serverSettings.getProperty("LoginHostname", "127.0.0.1");
			LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));

			DEVELOPER = Boolean.parseBoolean(serverSettings.getProperty("Developer", "false"));

			ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "True"));

			LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
			LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
			GM_MIN = Integer.parseInt(serverSettings.getProperty("GMMinLevel", "100"));

			DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");

			SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));

			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "True"));

			IP_UPDATE_TIME = Integer.parseInt(serverSettings.getProperty("IpUpdateTime", "0")) * 60 * 1000;

			FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "True"));
			FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
			NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
			FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
			MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + LOGIN_CONFIGURATION_FILE + " File.");
		}

		//      telnet
		try
		{
			Properties telnetSettings = new Properties();
			InputStream is = new FileInputStream(new File(TELNET_FILE));
			telnetSettings.load(is);
			is.close();

			IS_TELNET_ENABLED = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + TELNET_FILE + " File.");
		}

		// Initialize config properties for DB
		// ----------------------------------
		initDBProperties();
	}

	// it has no instancies
	private Config()
	{
	}

	/**
	 * To keep compatibility with old loginserver.properties, add db properties into system properties
	 * Spring will use those values later
	 */
	public static void initDBProperties()
	{
		System.setProperty("com.l2jfree.db.driverclass", DATABASE_DRIVER);
		System.setProperty("com.l2jfree.db.urldb", DATABASE_URL);
		System.setProperty("com.l2jfree.db.user", DATABASE_LOGIN);
		System.setProperty("com.l2jfree.db.password", DATABASE_PASSWORD);
	}
}
