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
package com.l2jfree.gameserver.datatables;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
public final class ShotTable
{
	private static final Log _log = LogFactory.getLog(ShotTable.class);
	
	private static ShotTable _instance;
	
	public static ShotTable getInstance()
	{
		if (_instance == null)
			_instance = new ShotTable();
		
		return _instance;
	}
	
	private ShotTable()
	{
		_log.info("ShotTable: initialized.");
	}
	
	//================================================================
	// Public
	
	public boolean isShot(int id) // 012345
	{
		return isPcShot(id) || isBeastShot(id);
	}
	
	//================================================================
	public boolean isPcShot(int id) // 0123
	{
		return isMagicShot(id) || isSoulShot(id) || isFishingShot(id);
	}
	
	public boolean isBeastShot(int id) // 45
	{
		return isBeastMagicShot(id) || isBeastSoulShot(id);
	}
	
	//================================================================
	public boolean isMagicShot(int id) // 0 - blessed spirit, 1 - spirit
	{
		switch (id)
		{
			case 3947: // ng
			case 3948: // d
			case 3949: // c
			case 3950: // b
			case 3951: // a
			case 3952: // s
				return true;
			case 2509: // ng
			case 2510: // d
			case 2511: // c
			case 2512: // b
			case 2513: // a
			case 2514: // s
			case 5790: // beginner
				return true;
		}
		return false;
	}
	
	public boolean isBeastMagicShot(int id) // 4 - beast spirit
	{
		switch (id)
		{
			case 6646: // sps
			case 6647: // bsps
				return true;
		}
		return false;
	}
	
	//================================================================
	public boolean isSoulShot(int id) // 2 - soul
	{
		switch (id)
		{
			case 1835: // ng
			case 1463: // d
			case 1464: // c
			case 1465: // b
			case 1466: // a
			case 1467: // s
			case 5789: // beginner
				return true;
		}
		return false;
	}
	
	public boolean isBeastSoulShot(int id) // 5 - beast soul
	{
		switch (id)
		{
			case 6645: // ss
				return true;
		}
		return false;
	}
	
	//================================================================
	public boolean isFishingShot(int id) // 3 - fishing
	{
		switch (id)
		{
			case 6535: // ng
			case 6536: // d
			case 6537: // c
			case 6538: // b
			case 6539: // a
			case 6540: // s
				return true;
		}
		return false;
	}
}
