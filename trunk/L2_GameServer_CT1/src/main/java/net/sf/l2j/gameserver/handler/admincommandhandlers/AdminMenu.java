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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles following admin commands:
 * - handles every admin menu command
 * 
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminMenu implements IAdminCommandHandler 
{
	private static final Log _log = LogFactory.getLog(AdminMenu.class.getName());

	private static final String[] ADMIN_COMMANDS = {
		"admin_char_manage",
		"admin_teleport_character_to_menu",
		"admin_recall_char_menu", "admin_recall_party_menu", "admin_recall_clan_menu",
		"admin_goto_char_menu",
		"admin_kick_menu",
		"admin_kill_menu",
		"admin_ban_menu",
		"admin_unban_menu"
	};
	private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;

		if (command.equals("admin_char_manage"))
			showMainPage(activeChar);
		else if (command.startsWith("admin_teleport_character_to_menu"))
		{
			String[] data = command.split(" ");
			if(data.length==5)
			{
				String playerName=data[1];
				L2PcInstance player = L2World.getInstance().getPlayer(playerName);
				if(player!=null)
					teleportCharacter(player,Integer.parseInt(data[2]),Integer.parseInt(data[3]),Integer.parseInt(data[4]),activeChar, "Admin is teleporting you.");
			}
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_recall_char_menu"))
		{
			try
			{
				String targetName = command.substring(23);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportCharacter(player,activeChar.getX(),activeChar.getY(),activeChar.getZ(),activeChar, "Admin is teleporting you.");
			}
			catch (StringIndexOutOfBoundsException e){}
		}
		else if (command.startsWith("admin_recall_party_menu"))
		{
			int x=activeChar.getX(), y = activeChar.getY(), z=activeChar.getZ();
			try
			{
				String targetName = command.substring(24);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				if(player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					return true;
				}
				if(!player.isInParty())
				{
					activeChar.sendMessage("Player is not in party.");
					teleportCharacter(player,x,y,z,activeChar, "Admin is teleporting you.");
					return true;
				}
				for(L2PcInstance pm : player.getParty().getPartyMembers())
					teleportCharacter(pm, x, y, z, activeChar, "Your party is being teleported by an Admin.");
			}
			catch (Exception e){}
		}
		else if (command.startsWith("admin_recall_clan_menu"))
		{
			int x=activeChar.getX(), y = activeChar.getY(), z=activeChar.getZ();
			try
			{
				String targetName = command.substring(23);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				if(player == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					return true;
			}
				L2Clan clan = player.getClan();
				if(clan==null)
				{
					activeChar.sendMessage("Player is not in a clan.");
					teleportCharacter(player,x,y,z,activeChar, "Admin is teleporting you.");
					return true;
				}
				L2PcInstance[] members = clan.getOnlineMembers("");
				for (L2PcInstance element : members)
					teleportCharacter(element, x, y, z, activeChar, "Your clan is being teleported by an Admin.");
			}
			catch (Exception e){}
 		}
		else if (command.startsWith("admin_goto_char_menu"))
		{
			try
			{
				String targetName = command.substring(21);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Target not found.");
				showMainPage(activeChar);
			}
		}
		else if (command.equals("admin_kill_menu"))
		{
			handleKill(activeChar);
		}
		else if (command.startsWith("admin_kick_menu"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				if (plyr != null)
				{
					plyr.logout();
					sm.addString("You kicked " + plyr.getName() + " from the game.");
				}
				else
					sm.addString("Player " + player + " was not found in the game.");
				activeChar.sendPacket(sm);
			}
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_ban_menu"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				if (plyr != null)
				{
					plyr.logout();
				}
				setAccountAccessLevel(player, activeChar, -100);
			}
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_unban_menu"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				setAccountAccessLevel(player, activeChar, 0);
			}
			showMainPage(activeChar);
		}
		return true;
	}
	public String[] getAdminCommandList() 
	{
		return ADMIN_COMMANDS;
	}
	private boolean checkLevel(int level) 
	{
		return (level >= REQUIRED_LEVEL);
	}
	private void handleKill(L2PcInstance activeChar)
	{
		handleKill(activeChar, null);
	}
	private void handleKill(L2PcInstance activeChar, String player)
	{
		L2Object obj = activeChar.getTarget();
		L2Character target = (L2Character)obj;
		String filename = "main_menu.htm";
		if (player != null)
		{
			L2PcInstance plyr = L2World.getInstance().getPlayer(player);
			if (plyr != null)
				target = plyr;
			activeChar.sendMessage("You killed " + plyr.getName());
		}
		if (target != null)
		{
			if (target instanceof L2PcInstance)
			{
				target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
				filename = "charmanage.htm";
			}
			else if (target.isChampion())
				target.reduceCurrentHp(target.getMaxHp()*Config.CHAMPION_HP + 1, activeChar);
			else 
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		AdminHelpPage.showHelpPage(activeChar, filename);
	}

	private void teleportCharacter(L2PcInstance player, int x, int y, int z, L2PcInstance activeChar, String message)
	{
		if (player != null) 
		{
			player.sendMessage(message);
			player.teleToLocation(x, y, z, true);
		}
		else
			activeChar.sendMessage("Target not found.");
		showMainPage(activeChar);
	}
	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		L2PcInstance player = null;
		if (target != null && target instanceof L2PcInstance) 
			player = (L2PcInstance)target;
		else 
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (player.getObjectId() == activeChar.getObjectId())
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		else
		{
			activeChar.teleToLocation(player.getX(), player.getY(), player.getZ(), true);
			activeChar.sendMessage("You're teleporting yourself to character " + player.getName());
		}
		showMainPage(activeChar);
	}
	/**
	 * @param activeChar
	 */
	private void showMainPage(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}

	private void setAccountAccessLevel(String player, L2PcInstance activeChar, int banLevel)
	{
		java.sql.Connection con = null;
		try
		{		   
			con = L2DatabaseFactory.getInstance().getConnection(con);
			String stmt = "SELECT account_name FROM characters WHERE char_name = ?";
			PreparedStatement statement = con.prepareStatement(stmt);
			statement.setString(1, player);
			ResultSet result = statement.executeQuery();
			if (result.next())
			{
				String acc_name = result.getString(1);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				if(acc_name.length() > 0)
				{
					LoginServerThread.getInstance().sendAccessLevel(acc_name, banLevel);
					sm.addString("Account Access Level for "+player+" set to "+banLevel+".");
				}
				else
					sm.addString("Couldn't find player: "+player+".");
				activeChar.sendPacket(sm);
			}
			else
				activeChar.sendMessage("Specified player name didn't lead to a valid account.");
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Could not set accessLevel:"+e);
			if (_log.isDebugEnabled())
				e.printStackTrace();
		}
		finally 
		{
			try 
			{ 
				con.close(); 
			}
			catch (Exception e) {}
		}
	}
}
