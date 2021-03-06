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
package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;

/**
 *
 * @author  Noctarius
 */
public class ChatSystem implements IChatHandler
{
	private SystemChatChannelId[] _chatTypes = { SystemChatChannelId.Chat_System };

	/**
	 * @see de.dc.l2j.gameserver.handler.IChatHandler#getChatTypes()
	 */
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	/**
	 * @see de.dc.l2j.gameserver.handler.IChatHandler#useChatHandler(de.dc.l2j.gameserver.character.player.L2PcInstance, java.lang.String, de.dc.l2j.gameserver.network.enums.SystemChatChannelId, java.lang.String)
	 */
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		//TODO: Find out what this channel is original intended for
		//      For me it is my emotechannel, because normal all-chan is affected
		//      by a language skill system. This one is readable by everyone.
    	CreatureSay cs = new CreatureSay(activeChar.getObjectId(), chatType.getId(), activeChar.getName() + "'s Emote", text);         

    	for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
	        if (player != null && activeChar.isInsideRadius(player, 1250, false, true)){
	            player.sendPacket(cs);
	            player.broadcastSnoop(activeChar.getObjectId(), chatType.getId(), activeChar.getName(), text);
	        }
    	
    	activeChar.sendPacket(cs);
    	activeChar.broadcastSnoop(activeChar.getObjectId(), chatType.getId(), activeChar.getName(), text);
	}
}