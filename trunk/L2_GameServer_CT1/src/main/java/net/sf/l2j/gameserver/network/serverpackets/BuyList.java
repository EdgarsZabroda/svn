/*
 * This program is free software; you can redistribute it and/or modify
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

import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * sample
 *
 * 1d 
 * 1e 00 00 00 			// ??
 * 5c 4a a0 7c 			// buy list id
 * 02 00				// item count
 *  
 * 04 00 				// itemType1  0-weapon/ring/earring/necklace  1-armor/shield  4-item/questitem/adena
 * 00 00 00 00 			// objectid
 * 32 04 00 00 			// itemid
 * 00 00 00 00 			// count
 * 05 00 				// itemType2  0-weapon  1-shield/armor  2-ring/earring/necklace  3-questitem  4-adena  5-item
 * 00 00 			
 * 60 09 00 00			// price
 *  
 * 00 00
 * 00 00 00 00 
 * b6 00 00 00 
 * 00 00 00 00 
 * 00 00 
 * 00 00 				 
 * 80 00 				//	body slot 	 these 4 values are only used if itemtype1 = 0 or 1
 * 00 00 				//
 * 00 00 				//
 * 00 00 				//
 * 50 c6 0c 00
 *  
 
 * format   dd h (h dddhh hhhh d)	revision 377
 * format   dd h (h dddhh dhhh d)	revision 377
 * 
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class BuyList extends L2GameServerPacket
{
	private static final String _S__1D_BUYLIST = "[S] 11 BuyList";
	private int _listId;
	private L2ItemInstance[] _list;
	private int _money;
	private double _taxRate = 0;

	public BuyList(L2TradeList list, int currentMoney)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}	

	public BuyList(L2TradeList list, int currentMoney, double taxRate)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
		_taxRate = taxRate;
	}	
	
	public BuyList(List<L2ItemInstance> lst, int listId, int currentMoney)
	{
		_listId = listId;
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x07);
		writeD(_money);		// current money
		writeD(_listId);
		writeH(_list.length);

		for (L2ItemInstance item : _list)
		{
			if(item.getCount() >0 || item.getCount() == -1)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(item.getObjectId());
				writeD(item.getItemDisplayId());
				writeD(item.getCount() >= 0 ? item.getCount() : 0); // max amount of items that a player can buy at a time (with this itemid)
				writeH(item.getItem().getType2());					// item type2
				writeH(0x00);										// custom type1
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());						// enchant level
				writeH(0x00);										// custom type2
                writeD((int)(item.getPriceToSell() * (_taxRate)));
                writeD(0x00);
                writeD(0x00);
                writeD(0x00);
                writeD(0x00);
                writeD(0x00);
                writeD(0x00);
                writeD(0x00);
                writeD(0x00);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__1D_BUYLIST;
	}
}
