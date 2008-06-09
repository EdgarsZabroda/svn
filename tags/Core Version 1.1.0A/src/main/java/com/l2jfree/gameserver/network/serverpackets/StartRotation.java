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
package com.l2jfree.gameserver.network.serverpackets;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public final class StartRotation extends L2GameServerPacket
{
	private static final String S_7A_STARTROTATION = "[S] 7a StartRotation";
	private int _charObjId;
	private int _degree;
	private int _side;

	public StartRotation(L2PcInstance player, int degree, int side)
	{
		_charObjId = player.getObjectId();
		_degree = degree;
		_side = side;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x7a);
		writeD(_charObjId);
		writeD(_degree);
		writeD(_side);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return S_7A_STARTROTATION;
	}
}
