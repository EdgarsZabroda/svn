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

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.L2Item;
import com.l2jfree.gameserver.util.IllegalPlayerAction;
import com.l2jfree.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestCrystallizeItem extends L2GameClientPacket
{
	private static final String _C__72_REQUESTDCRYSTALLIZEITEM = "[C] 72 RequestCrystallizeItem";

	private final static Log _log = LogFactory.getLog(RequestCrystallizeItem.class
			.getName());

	private int _objectId;

	private int _count;

    @Override
    protected void readImpl()
    {
        _objectId = readD();
        _count = readD();
    }

    @Override
    protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null)
        {
            _log.debug("RequestCrystalizeItem: activeChar was null");
            return;
        }
        
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_CREATEITEM && Shutdown.getCounterInstance() != null 
        		&& Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
        {
			activeChar.sendMessage("Item creation is not allowed during restart/shutdown.");
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
            return;
        }
		
        if (_count <= 0)
        {
            Util.handleIllegalPlayerAction(activeChar,
                                           "[RequestCrystallizeItem] count <= 0! ban! oid: "
                                           + _objectId + " owner: " + activeChar.getName(),
                                           IllegalPlayerAction.PUNISH_KICK);
            return;
        }
        
        if (activeChar.getPrivateStoreType() != 0
                || activeChar.isInCrystallize())
        {
            activeChar
            .sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            return;
        }
        
        int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
        if (skillLevel <= 0)
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
            activeChar.sendPacket(sm);
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
            
        PcInventory inventory = activeChar.getInventory();
		if (inventory != null)
        {
            L2ItemInstance item = inventory.getItemByObjectId(_objectId);

            if (item == null || item.isWear())
            {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
                
            if ((item).isHeroItem())
                return;
            if (_count > item.getCount())
            {
                _count = activeChar.getInventory().getItemByObjectId(_objectId)
                .getCount();
            }
        }
		
		L2ItemInstance itemToRemove = activeChar.getInventory()
		.getItemByObjectId(_objectId);
		if (itemToRemove == null || itemToRemove.isWear())
		    {
		    return;
		    }
		if (!itemToRemove.getItem().isCrystallizable()
		        || (itemToRemove.getItem().getCrystalCount() <= 0)
		        || (itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_NONE))
		{
		    _log.warn("" + activeChar.getObjectId()
		                 + " tried to crystallize "
		                 + itemToRemove.getItem().getItemId());
            return;
        }

        // Check if the char can crystallize items and return if false;
        boolean canCrystallize = true;

        switch (itemToRemove.getItem().getCrystalType())
        {
            case L2Item.CRYSTAL_C:
            {
                if (skillLevel <= 1)
                {
                    canCrystallize = false;
                }
                break;
            }
            case L2Item.CRYSTAL_B:
            {
                if (skillLevel <= 2)
                {
                    canCrystallize = false;
                }
                break;
            }
            case L2Item.CRYSTAL_A:
            {
                if (skillLevel <= 3)
                {
                    canCrystallize = false;
                }
                break;
            }
            case L2Item.CRYSTAL_S:
            case L2Item.CRYSTAL_S80:
            {
                if (skillLevel <= 4)
                {
                    canCrystallize = false;
                }
                break;
            }
        }
        
        if (!canCrystallize)
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
            activeChar.sendPacket(sm);
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

		activeChar.setInCrystallize(true); 
		
		//unequip if needed
		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped =
				activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped) {
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			// activeChar.updatePDef();
			// activeChar.updatePAtk();
			// activeChar.updateMDef();
			// activeChar.updateMAtk();
			// activeChar.updateAccuracy();
			// activeChar.updateCriticalChance();
		}

		// remove from inventory
		L2ItemInstance removedItem = activeChar.getInventory().destroyItem(
				"Crystalize", _objectId, _count, activeChar, null);

		// add crystals
		int crystalId = itemToRemove.getItem().getCrystalItemId();
		int crystalAmount = itemToRemove.getCrystalCount();
		L2ItemInstance createditem = activeChar.getInventory().addItem(
				"Crystalize", crystalId, crystalAmount, activeChar,
				itemToRemove);

		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(createditem);
		sm.addNumber(crystalAmount);
		activeChar.sendPacket(sm);
        sm = null;

		// send inventory update
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0)
				iu.addRemovedItem(removedItem);
			else
				iu.addModifiedItem(removedItem);

			if (createditem.getCount() != crystalAmount)
				iu.addModifiedItem(createditem);
			else
				iu.addNewItem(createditem);

			activeChar.sendPacket(iu);
		} else
			activeChar.sendPacket(new ItemList(activeChar, false));

		// status & user info
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

		activeChar.broadcastUserInfo();

		L2World world = L2World.getInstance();
		world.removeObject(removedItem);

		activeChar.setInCrystallize(false); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__72_REQUESTDCRYSTALLIZEITEM;
	}
}
