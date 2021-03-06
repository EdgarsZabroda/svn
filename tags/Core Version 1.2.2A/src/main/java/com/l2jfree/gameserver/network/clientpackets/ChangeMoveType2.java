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
package com.l2jfree.gameserver.network.clientpackets;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class ChangeMoveType2 extends L2GameClientPacket
{
	private static final String _C__1C_CHANGEMOVETYPE2 = "[C] 1C ChangeMoveType2";

	private boolean _typeRun;
	
	/**
	 * packet type id 0x1c
	 * 
	 * sample
	 * 
	 * 1d
	 * 01 00 00 00 // type (0 = walk, 1 = run)
	 * 
	 * format:		cd
	 * @param decrypt
	 */
    @Override
    protected void readImpl()
    {
        _typeRun = readD() == 1;
    }

    @Override
    protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		    return;
		if (_typeRun)
			player.setRunning();
		else
			player.setWalking();
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__1C_CHANGEMOVETYPE2;
	}
}
