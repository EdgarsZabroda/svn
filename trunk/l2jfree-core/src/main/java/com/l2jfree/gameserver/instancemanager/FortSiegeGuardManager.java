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
package com.l2jfree.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.actor.instance.L2FortBallistaInstance;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public class FortSiegeGuardManager
{
	protected static final Log	_log				= LogFactory.getLog(FortSiegeGuardManager.class);

	private final Fort				_fort;
	protected FastMap<Integer, FastList<L2Spawn>> _siegeGuards = new FastMap<Integer, FastList<L2Spawn>>();
	protected FastList<L2Spawn> _siegeGuardsSpawns;

	public FortSiegeGuardManager(Fort fort)
	{
		_fort = fort;
	}

	/**
	 * Spawn guards.<BR><BR>
	 */
	public void spawnSiegeGuard()
	{
		try
		{
			FastList<L2Spawn> monsterList = getSiegeGuardSpawn().get(getFort().getFortId());
			if (monsterList != null)
			{
				for (L2Spawn spawnDat : monsterList)
				{
					spawnDat.doSpawn();
					if (spawnDat.getLastSpawn() instanceof L2FortBallistaInstance)
						spawnDat.stopRespawn();
					else
						spawnDat.startRespawn();
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Error spawning siege guards for fort " + getFort().getName() + ":" + e.getMessage(), e);
		}
	}

	/**
	 * Unspawn guards.<BR><BR>
	 */
	public void unspawnSiegeGuard()
	{
		try
		{
			FastList<L2Spawn> monsterList = getSiegeGuardSpawn().get(getFort().getFortId());
			if (monsterList != null)
			{
				for (L2Spawn spawnDat : monsterList)
				{
					spawnDat.stopRespawn();
					spawnDat.getLastSpawn().doDie(spawnDat.getLastSpawn());
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Error unspawning siege guards for fort " + getFort().getName() + ":" + e.getMessage(), e);
		}
	}

	/**
	 * Load guards.<BR><BR>
	 */
	void loadSiegeGuard()
	{
		_siegeGuards.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_siege_guards Where fortId = ? ");
			statement.setInt(1, getFort().getFortId());
			ResultSet rs = statement.executeQuery();

			L2Spawn spawn1;
			L2NpcTemplate template1;

			_siegeGuardsSpawns = new FastList<L2Spawn>();
			while (rs.next())
			{
				int fortId = rs.getInt("fortId");
				template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if (template1 != null)
				{
					spawn1 = new L2Spawn(template1);
					spawn1.setId(rs.getInt("id"));
					spawn1.setAmount(1);
					spawn1.setLocx(rs.getInt("x"));
					spawn1.setLocy(rs.getInt("y"));
					spawn1.setLocz(rs.getInt("z"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn1.setLocation(0);

					_siegeGuardsSpawns.add(spawn1);
				}
				else
				{
					_log.warn("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
				_siegeGuards.put(fortId, _siegeGuardsSpawns);
			}
			rs.close();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warn("Error loading siege guard for fort " + getFort().getName() + ":", e1);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final Fort getFort()
	{
		return _fort;
	}

	public final FastMap<Integer, FastList<L2Spawn>> getSiegeGuardSpawn()
	{
		return _siegeGuards;
	}
}