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

package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinSiege extends L2GameClientPacket{
    
    private static final String _C__A4_RequestJoinSiege = "[C] a4 RequestJoinSiege";
    //private final static Log _log = LogFactory.getLog(RequestJoinSiege.class.getName());

    private int _castleId;
    private int _isAttacker;
    private int _isJoining;
    
    @Override
    protected void readImpl()
    {
        _castleId = readD();
        _isAttacker = readD();
        _isJoining = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if(activeChar == null) return;
        if (!activeChar.isClanLeader()) return;
        
        if (_castleId < 100)
        {
            Castle castle = CastleManager.getInstance().getCastleById(_castleId);
            if (castle == null) return;
    
            if (_isJoining == 1)
            {
                if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
                    return;
                }
                if (_isAttacker == 1)
                    castle.getSiege().registerAttacker(activeChar);
                else
                    castle.getSiege().registerDefender(activeChar);
            }
            else
                castle.getSiege().removeSiegeClan(activeChar);
    
            castle.getSiege().listRegisterClan(activeChar);
        }
        else
        {
            Fort fort = FortManager.getInstance().getFortById(_castleId);
            if (fort == null) return;
    
            if (_isJoining == 1)
            {
                if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
                    return;
                }
                if (_isAttacker == 1)
                    fort.getSiege().registerAttacker(activeChar);
                else
                    fort.getSiege().registerDefender(activeChar);
            }
            else
                fort.getSiege().removeSiegeClan(activeChar);
    
            fort.getSiege().listRegisterClan(activeChar);
        }
    }
    
    
    @Override
    public String getType()
    {
        return _C__A4_RequestJoinSiege;
    }
}
