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
package com.l2jfree.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IAdminCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/** 
 * @author Psychokiller1888
 * 
 */


public class AdminVitality implements IAdminCommandHandler
{
	private static int				level			= 0;
	private static double			vitality		= 0.0;
	
	private static final String[]	ADMIN_COMMANDS	=
	{ 
		"admin_set_vitality",
		"admin_set_vitality_level",
		"admin_full_vitality",
		"admin_empty_vitality",
		"admin_get_vitality"
	};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ENABLE_VITALITY)
			activeChar.sendMessage("Vitality is not enabled on the server!");

		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();

		if (activeChar.getTarget() instanceof L2PcInstance)
		{
			L2PcInstance target;
			target = (L2PcInstance) activeChar.getTarget();

			if (cmd.equals("admin_set_vitality"))
			{
				try
				{
					vitality = Double.parseDouble(st.nextToken());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Incorrect vitality");
				}

				target.setVitalityPoints(vitality, true);
				target.sendMessage("Admin set your Vitality points to " + vitality);
			}
			else if (cmd.equals("admin_set_vitality_level"))
			{
				try
				{
					level = Integer.parseInt(st.nextToken());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Incorrect vitality level (0-4)");
				}

				if (level >= 0 && level <= 4)
				{
					switch (level)
					{
					case 0:
						vitality = 0.0;
						break;
					case 1:
						vitality = 240.0;
						break;
					case 2:
						vitality = 1800.0;
						break;
					case 3:
						vitality = 14600.0;
						break;
					case 4:
						vitality = 18200.0;
						break;
					}
					target.setVitalityPoints(vitality, true);
					target.sendMessage("Admin set your Vitality level to " + level);
				}
				else
					activeChar.sendMessage("Incorrect vitality level (0-4)");
			}
			else if (cmd.equals("admin_full_vitality"))
			{
				target.setVitalityPoints(20000.0, true);
				target.sendMessage("Admin completly recharged your Vitality");
			}
			else if (cmd.equals("admin_empty_vitality"))
			{
				target.setVitalityPoints(1, true);
				target.sendMessage("Admin completly emptied your Vitality");
			}
			else if (cmd.equals("admin_get_vitality"))
			{
				int playerVitalityLevel = target.getVitalityLevel();
				double playerVitalityPoints = target.getVitalityPoints();

				activeChar.sendMessage("Player vitality level: " + playerVitalityLevel);
				activeChar.sendMessage("Player vitality points: " + playerVitalityPoints);
			}
			return true;
		}
		else
		{
			activeChar.sendMessage("Target not found or not a player");
			return false;
		}
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
