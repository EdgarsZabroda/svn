/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.instancemanager.IrcManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.script.DateRange;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.7 $ $Date: 2005/03/29 23:15:14 $
 */
public class Announcements
{
	private final static Log		_log					= LogFactory.getLog(Announcements.class.getName());

	private static Announcements	_instance;
	private List<String>			_announcements			= new FastList<String>();
	private List<List<Object>>		_eventAnnouncements		= new FastList<List<Object>>();
	private String					leaderboardAnnouncement	= null;

	public Announcements()
	{
		loadAnnouncements();
	}

	public static Announcements getInstance()
	{
		if (_instance == null)
			_instance = new Announcements();

		return _instance;
	}

	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.info("data/announcements.txt doesn't exist");
		}
	}

	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (int i = 0; i < _announcements.size(); i++)
		{
			CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Announce.getId(), activeChar.getName(), _announcements.get(i).replace("%name%",
					activeChar.getName()).toString());
			activeChar.sendPacket(cs);
		}
		if (leaderboardAnnouncement != null)
		{
			CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Announce.getId(), activeChar.getName(), leaderboardAnnouncement);
			activeChar.sendPacket(cs);
		}

		for (int i = 0; i < _eventAnnouncements.size(); i++)
		{
			List<Object> entry = _eventAnnouncements.get(i);

			DateRange validDateRange = (DateRange) entry.get(0);
			String[] msg = (String[]) entry.get(1);
			Date currentDate = new Date();

			if (validDateRange.isValid() && validDateRange.isWithinRange(currentDate))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1);
				for (String element : msg)
				{
					sm.addString(element);
				}
				activeChar.sendPacket(sm);
			}
		}
	}

	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		FastList<Object> entry = new FastList<Object>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
	}

	public void listAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		TextBuilder replyMSG = new TextBuilder("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>" + _announcements.get(i) + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i
					+ "\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}

	public void setLeaderboardAnnouncement(String announce)
	{
		leaderboardAnnouncement = announce;
	}

	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}

	private void readFromDisk(File file)
	{
		LineNumberReader lnr = null;
		try
		{
			int i = 0;
			String line = null;
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					String announcement = st.nextToken();
					_announcements.add(announcement);

					i++;
				}
			}
			if (_log.isDebugEnabled())
				_log.info("Announcements: Loaded " + i + " Announcements.");
		}
		catch (IOException e1)
		{
			_log.fatal("Error reading announcements", e1);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e2)
			{
			}
		}
	}

	private void saveToDisk()
	{
		File file = new File("data/announcements.txt");
		FileWriter save = null;

		try
		{
			save = new FileWriter(file);
			for (int i = 0; i < _announcements.size(); i++)
			{
				save.write(_announcements.get(i));
				save.write("\r\n");
			}
			save.flush();
			save.close();
			save = null;
		}
		catch (IOException e)
		{
			_log.warn("saving the announcements file has failed: " + e);
		}
	}

	public void announceToAll(String text)
	{
		CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Announce.getId(), "", text);

		if (Config.IRC_ENABLED && Config.IRC_ANNOUNCE)
			IrcManager.getInstance().getConnection().sendChan("10Announce: " + text);

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
	}

	public void announceToAll(SystemMessage sm)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}

	// Method fo handling announcements from admin
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			announceToAll(text);
		}

		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}

	/**
	 * Announce to players.<BR>
	 * <BR>
	 * 
	 * @param message
	 *            The String of the message to send to player
	 */
	public void announceToPlayers(String message)
	{
		// Get all players
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			player.sendMessage(message);
	}
}
