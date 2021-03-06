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

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class RequestReplyStartPledgeWar extends L2GameClientPacket
{
	private static final String	_C__REQUESTREPLYSTARTPLEDGEWAR = "[C] 04 RequestReplyStartPledgeWar c[sd]";
	
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		readS();
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;
		
		L2PcInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
		{
			sendAF();
			return;
		}
		
		if (_answer == 1)
			ClanTable.getInstance().storeClanWars(requestor.getClanId(), activeChar.getClanId());
		else
			requestor.sendPacket(new SystemMessage(SystemMessageId.S1_WAR_PROCLAMATION_HAS_BEEN_REFUSED).addString(activeChar.getClan().getName()));
		
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
		
		sendAF();
	}
	
	@Override
	public String getType()
	{
		return _C__REQUESTREPLYSTARTPLEDGEWAR;
	}
}
