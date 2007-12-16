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
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;

/* Packet format: F3 XX000000 YY000000 ZZ000000 */

/**
 *
 * @author  Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{
	private static final String _S__F3_ETCSTATUSUPDATE = "[S] F3 EtcStatusUpdate";

	/**
	 *
	 * Packet for lvl 3 client buff line
	 *
	 * Example:(C4)
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - empty statusbar
	 * F3 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - increased force lvl 1
	 * F3 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - weight penalty lvl 1
	 * F3 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 - chat banned
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 - Danger Area lvl 1
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 - lvl 1 grade penalty
	 *
	 * packet format: cdd //and last three are ddd???
 	 *
	 * Some test results:
	 * F3 07 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - lvl 7 increased force lvl 4 weight penalty
	 *
	 * Example:(C5 709)
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0F 00 00 00 - lvl 1 charm of courage lvl 15 Death Penalty
	 *
	 *
	 * NOTE:
	 * End of buff:
	 * You must send empty packet
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
	 * to remove the statusbar or just empty 
	 */

	private L2PcInstance _activeChar;
	private EffectCharge _effect;

	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		 _activeChar = activeChar;
		 _effect = (EffectCharge)_activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
	}

	/**
	 * @see net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xF9);				//several icons to a separate line (0 = disabled)
		if (_effect != null)
			writeD(_effect.getLevel()); // 1-7 increase force, lvl
		else
			writeD(0x00); // 1-7 increase force, lvl
        writeD(_activeChar.getWeightPenalty());
        writeD(_activeChar.getMessageRefusal() ? 1 : 0);
        writeD(0x00); // danger area
        writeD(_activeChar.getExpertisePenalty());
        writeD(_activeChar.getCharmOfCourage() ? 1 : 0);
        writeD(_activeChar.getDeathPenaltyBuffLevel());
        writeD(0x00); //souls 
	}

	/**
	 * @see net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	public String getType()
	{
		return _S__F3_ETCSTATUSUPDATE;
	}
}