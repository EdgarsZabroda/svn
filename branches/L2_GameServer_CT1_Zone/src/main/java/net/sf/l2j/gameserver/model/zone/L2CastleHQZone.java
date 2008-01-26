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
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;

public class L2CastleHQZone extends L2CastleZone
{
	@Override
	protected void register()
	{
		_castle = CastleManager.getInstance().getCastleById(_castleId);
		_castle.getHeadQuarters().registerZone(this);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance && ((L2PcInstance)character).isGM())
			character.sendMessage("Entered castle HQ zone "+getId());
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance && ((L2PcInstance)character).isGM())
			character.sendMessage("Left castle HQ zone "+getId());
	}
}
