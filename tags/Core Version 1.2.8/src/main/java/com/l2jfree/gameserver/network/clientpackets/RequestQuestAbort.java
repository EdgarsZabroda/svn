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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.instancemanager.QuestManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.network.serverpackets.QuestList;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestQuestAbort extends L2GameClientPacket
{
    private static final String _C__64_REQUESTQUESTABORT = "[C] 64 RequestQuestAbort";
    private final static Log _log = LogFactory.getLog(RequestQuestAbort.class.getName());

    
    private int _questId;
    /**
     * packet type id 0x64<p>
     */
    @Override
    protected void readImpl()
    {
        _questId = readD();
    }
    
    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        
        Quest qe = QuestManager.getInstance().getQuest(_questId);
        if (qe != null)
        {
            QuestState qs = activeChar.getQuestState(qe.getName());
            if(qs != null)
            {
                qs.exitQuest(true);
                activeChar.sendMessage("Quest aborted.");
                activeChar.sendPacket(new QuestList(activeChar));
            }
            else
            {
                if (_log.isDebugEnabled()) _log.info("Player '"+activeChar.getName()+"' try to abort quest "+qe.getName()+" but he didn't have it started.");
            }
        }
        else
        {
            if (_log.isDebugEnabled()) _log.warn("Quest (id='"+_questId+"') not found.");
        }
    }

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__64_REQUESTQUESTABORT;
    }
}
