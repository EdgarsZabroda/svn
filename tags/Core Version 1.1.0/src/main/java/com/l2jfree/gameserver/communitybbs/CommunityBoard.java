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
package com.l2jfree.gameserver.communitybbs;

import com.l2jfree.Config;
import com.l2jfree.gameserver.communitybbs.Manager.ClanBBSManager;
import com.l2jfree.gameserver.communitybbs.Manager.PostBBSManager;
import com.l2jfree.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfree.gameserver.communitybbs.Manager.TopBBSManager;
import com.l2jfree.gameserver.communitybbs.Manager.TopicBBSManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ShowBoard;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class CommunityBoard
{
	private static CommunityBoard _instance;

	public CommunityBoard()
	{
	}

	public static CommunityBoard getInstance()
	{
		if (_instance == null)
		{
			_instance = new CommunityBoard();
		}

		return _instance;
	}

	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(Config.COMMUNITY_TYPE.equals("full"))
		{
			if (command.startsWith("_bbsclan"))
			{
				ClanBBSManager.getInstance().parsecmd(command,activeChar);
			}
			else if(command.startsWith("_bbsmemo"))
			{
				TopicBBSManager.getInstance().parsecmd(command,activeChar);
			}
			else if(command.startsWith("_bbstopics"))
			{
				TopicBBSManager.getInstance().parsecmd(command,activeChar);
			}
			else if(command.startsWith("_bbsposts"))
			{
				PostBBSManager.getInstance().parsecmd(command,activeChar);
			}
			else if(command.startsWith("_bbstop"))
			{
				TopBBSManager.getInstance().parsecmd(command,activeChar);
			}
			else if(command.startsWith("_bbshome"))
			{
				TopBBSManager.getInstance().parsecmd(command,activeChar);
			}
			else if(command.startsWith("_bbsloc"))
			{
				RegionBBSManager.getInstance().parsecmd(command,activeChar);
			}
			else
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null,"102"));
				activeChar.sendPacket(new ShowBoard(null,"103"));
			}
		}
		else  if(Config.COMMUNITY_TYPE.equals("old"))
		{
			RegionBBSManager.getInstance().parsecmd(command,activeChar);
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CB_OFFLINE));
		}
	}

	/**
	 * @param client
	 * @param url
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if (Config.COMMUNITY_TYPE.equals("full"))
		{
			if (url.equals("Topic"))
			{
				TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Post"))
			{
				PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Region"))
			{
				RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + url + " is not implemented yet</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
		}
		else if (Config.COMMUNITY_TYPE.equals("old"))
		{
			RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>The Community board is currently disable</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
}
