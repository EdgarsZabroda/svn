/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.handler.zonehandlers;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author G1ta0
 *
 */
public class MotherTreeZone extends DefaultZone
{

	/**
	 * @param zone
	 */
	public MotherTreeZone(IZone zone)
	{
		super(zone);
	}

	@Override
	public void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) character;

			if(pc.getRace() != Race.elf)
				return;

			if(pc.isInParty())
				for(L2PcInstance member : pc.getParty().getPartyMembers())
					if(member.getRace() != Race.elf)
						// if player is in party with a non-elven race Mother Tree effect is cancelled
						return;
		}
		super.onEnter(character);
	}

	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);
	}
}
