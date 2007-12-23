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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ShowMiniMap;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestOpenMinimap extends L2GameClientPacket
{
	
	public RequestOpenMinimap()
	{
	}
	
	protected void readImpl()
	{
	}
	
	protected void runImpl()
	{
		L2PcInstance client = getClient().getActiveChar();
		if (client == null)
			return;
		if (!client.canOpenMinimap())
		{
			client.sendPacket(new SystemMessage(2207));
			return;
		}
		else
		{
			client.sendPacket(new ShowMiniMap(1665));
			return;
		}
	}
	
	public String getType()
	{
		return "RequestOpenMinimap";
	}
}
