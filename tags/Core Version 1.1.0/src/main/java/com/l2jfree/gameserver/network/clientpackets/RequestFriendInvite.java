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

import com.l2jfree.gameserver.model.L2FriendList;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.FriendAddRequest;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestFriendInvite extends L2GameClientPacket
{
	private static final String _C__5E_REQUESTFRIENDINVITE = "[C] 5E RequestFriendInvite";

	private String _name;
	
    @Override
    protected void readImpl()
    {
        _name = readS();
    }

    @Override
    protected void runImpl()
	{
		SystemMessage sm;
		L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null)
            return;
        
        L2PcInstance friend = L2World.getInstance().getPlayer(_name);
        
    	if (friend == null)
        {
    	    //Target is not found in the game.
    		sm = new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
    		activeChar.sendPacket(sm);
    	}
        else if (friend == activeChar)
        {
    	    //You cannot add yourself to your own friend list.
        	sm = new SystemMessage(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
        	activeChar.sendPacket(sm);
    	}
        else if (L2FriendList.isInFriendList(activeChar, friend))
        { 
            // Target is already in friend list.
        	sm = new SystemMessage(SystemMessageId.S1_ALREADY_ON_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
        }
        else if (!friend.isProcessingRequest())
		{
		    activeChar.onTransactionRequest(friend);
		    sm = new SystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS);
		    sm.addString(activeChar.getName());
		    friend.sendPacket(sm);
		    
		    FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
		    friend.sendPacket(ajf);
    	} 
        else 
        {
    		sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			sm.addString(_name);
			activeChar.sendPacket(sm);
    	}
    	
    	sm = null;
	}
	
	@Override
	public String getType()
	{
		return _C__5E_REQUESTFRIENDINVITE;
	}
}
