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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.geodata.pathfinding.AbstractNodeLoc;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.instancemanager.MapRegionManager;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.model.mapregion.L2MapRegion;
import com.l2jfree.gameserver.templates.StatsSet;
import com.l2jfree.gameserver.templates.chars.L2CharTemplate;

public class DoorTable
{
	private final static Log					_log	= LogFactory.getLog(DoorTable.class.getName());

	private FastMap<Integer, L2DoorInstance>	_staticItems;

	private static DoorTable					_instance;

	public static DoorTable getInstance()
	{
		if (_instance == null)
			_instance = new DoorTable();

		return _instance;
	}

	private DoorTable()
	{
		_staticItems = new FastMap<Integer, L2DoorInstance>();
		parseData();
		checkAutoOpen();
	}

	public void reloadAll()
	{
		respawn();
	}

	public void respawn()
	{
		_staticItems = null;
		_instance = new DoorTable();
	}

	private void parseData()
	{

		LineNumberReader lnr = null;
		try
		{
			File doorData = new File(Config.DATAPACK_ROOT, "data/door.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));

			String line = null;

			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;

				L2DoorInstance door = parseList(line);
				_staticItems.put(door.getDoorId(), door);
				door.spawnMe(door.getX(), door.getY(), door.getZ());
			}

			_initialized = true;
			_log.info("DoorTable: Loaded " + _staticItems.size() + " Door Templates.");
		}
		catch (FileNotFoundException e)
		{
			_initialized = false;
			_log.warn("door.csv is missing in data folder", e);
		}
		catch (Exception e)
		{
			_initialized = false;
			_log.warn("error while creating door table ", e);
		}
		finally
		{
			try
			{
				if (lnr != null)
					lnr.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void registerToClanHalls()
	{
		if (_staticItems == null)
			return;

		for (L2DoorInstance door : _staticItems.values())
		{
			ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 700);
			if (clanhall != null)
			{
				clanhall.getDoors().add(door);
				door.setClanHall(clanhall);
				if (_log.isDebugEnabled())
					_log.debug("door " + door.getDoorName() + " attached to ch " + clanhall.getName());
			}
		}
	}

	public void setCommanderDoors()
	{
		if (_staticItems == null)
			return;

		for (L2DoorInstance door : _staticItems.values())
		{
			if (door.getFort() != null && door.getOpen())
			{
				door.setOpen(false);
				door.setIsCommanderDoor(true);
			}
		}
	}

	public static L2DoorInstance parseList(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		
		String name = st.nextToken();
		int id = Integer.parseInt(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		int rangeXMin = Integer.parseInt(st.nextToken());
		int rangeYMin = Integer.parseInt(st.nextToken());
		int rangeZMin = Integer.parseInt(st.nextToken());
		int rangeXMax = Integer.parseInt(st.nextToken());
		int rangeYMax = Integer.parseInt(st.nextToken());
		int rangeZMax = Integer.parseInt(st.nextToken());
		int hp = Integer.parseInt(st.nextToken());
		int pdef = Integer.parseInt(st.nextToken());
		int mdef = Integer.parseInt(st.nextToken());
		boolean unlockable = false;
		if (st.hasMoreTokens())
			unlockable = Boolean.parseBoolean(st.nextToken());
		boolean startOpen = false;
		if (st.hasMoreTokens())
			startOpen = Boolean.parseBoolean(st.nextToken());
		
		if (rangeXMin > rangeXMax)
			_log.fatal("Error in door data rangeX, ID:" + id + ", rangeXMin : " + rangeXMin + ", rangeXMax : " + rangeXMax);
		if (rangeYMin > rangeYMax)
			_log.fatal("Error in door data rangeY, ID:" + id + ", rangeYMin : " + rangeYMin + ", rangeYMax : " + rangeYMax);
		if (rangeZMin > rangeZMax)
			_log.fatal("Error in door data rangeZ, ID:" + id + ", rangeZMin : " + rangeZMin + ", rangeZMax : " + rangeZMax);
		int collisionRadius = 0; // (max) radius for movement checks
		if ((rangeXMax - rangeXMin) > (rangeYMax - rangeYMin))
			collisionRadius = rangeYMax - rangeYMin;
		
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", id);
		npcDat.set("level", 0);
		npcDat.set("jClass", "door");

		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);

		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);

		//npcDat.set("name", "");
		npcDat.set("collision_radius", collisionRadius);
		npcDat.set("collision_height", rangeZMax - rangeZMin & 0xfff0);
		npcDat.set("fcollision_radius", collisionRadius);
		npcDat.set("fcollision_height", rangeZMax - rangeZMin & 0xfff0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("name", name);
		npcDat.set("baseHpMax", hp);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", pdef);
		npcDat.set("baseMDef", mdef);

		L2CharTemplate template = new L2CharTemplate(npcDat);
		L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, name, unlockable);
		door.setRange(rangeXMin, rangeYMin, rangeZMin, rangeXMax, rangeYMax, rangeZMax);
		try
		{
			door.setMapRegion(MapRegionManager.getInstance().getRegion(x, y, z));
		}
		catch (Exception e)
		{
			_log.fatal("Error in door data, ID:" + id);
		}
		template.setCollisionRadius(Math.min(x - rangeXMin, y - rangeYMin));
		door.getStatus().setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
		door.setOpen(startOpen);
		door.getPosition().setXYZInvisible(x, y, z);

		return door;
	}

	public static boolean isInitialized()
	{
		return _initialized;
	}

	private static boolean	_initialized	= false;

	public L2DoorInstance getDoor(Integer id)
	{
		return _staticItems.get(id);
	}

	public void putDoor(L2DoorInstance door)
	{
		_staticItems.put(door.getDoorId(), door);
	}

	public L2DoorInstance[] getDoors()
	{
		L2DoorInstance[] _allTemplates = _staticItems.values().toArray(new L2DoorInstance[_staticItems.size()]);
		return _allTemplates;
	}

	/**
	 * Performs a check and sets up a scheduled task for 
	 * those doors that require auto opening/closing.
	 */
	public void checkAutoOpen()
	{
		for (L2DoorInstance doorInst : _staticItems.values())
		{
			// Garden of Eva (every 7 minutes)
			if (doorInst.getDoorName().startsWith("goe"))
				doorInst.setAutoActionDelay(420000);

			// Tower of Insolence (every 5 minutes)
			else if (doorInst.getDoorName().startsWith("aden_tower"))
				doorInst.setAutoActionDelay(300000);

		/* TODO: check which are automatic
		// devils (every 5 minutes)
		else if (doorInst.getDoorName().startsWith("pirate_isle"))
		    doorInst.setAutoActionDelay(300000);
		// Cruma Tower (every 20 minutes) 
		else if (doorInst.getDoorName().startsWith("cruma")) 
		    doorInst.setAutoActionDelay(1200000);
		// Coral Garden Gate (every 15 minutes) 
		else if (doorInst.getDoorName().startsWith("Coral_garden")) 
		    doorInst.setAutoActionDelay(900000);
		// Normil's cave (every 5 minutes) 
		else if (doorInst.getDoorName().startsWith("Normils_cave")) 
		    doorInst.setAutoActionDelay(300000);
		// Normil's Garden (every 15 minutes) 
		else if (doorInst.getDoorName().startsWith("Normils_garden")) 
		    doorInst.setAutoActionDelay(900000);
		*/
		}
	}
	public boolean checkIfDoorsBetween(AbstractNodeLoc start, AbstractNodeLoc end)
	{
		return checkIfDoorsBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
	}
	
	public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz)
	{
		L2MapRegion region;
		try
		{
			region = MapRegionManager.getInstance().getRegion(x, y, z);
		}
		catch (Exception e)
		{
			return false;
		}
		
		// there are quite many doors, maybe they should be splitted
		for (L2DoorInstance doorInst : _staticItems.values())
		{
			if (doorInst.getMapRegion() != region) 
				continue;
			if (doorInst.getXMax() == 0)
				continue;
			
			// line segment goes through box
			// first basic checks to stop most calculations short
			// phase 1, x
			if (x <= doorInst.getXMax() && tx >= doorInst.getXMin() || tx <= doorInst.getXMax() && x >= doorInst.getXMin())
			{
				//phase 2, y
				if (y <= doorInst.getYMax() && ty >= doorInst.getYMin() || ty <= doorInst.getYMax() && y >= doorInst.getYMin())
				{
					// phase 3, basically only z remains but now we calculate it with another formula (by rage)
					// in some cases the direct line check (only) in the beginning isn't sufficient, 
					// when char z changes a lot along the path
					if (doorInst.getStatus().getCurrentHp() > 0 && !doorInst.getOpen()) 
					{
						int px1 = doorInst.getXMin();
						int py1 = doorInst.getYMin();
						int pz1 = doorInst.getZMin();
						int px2 = doorInst.getXMax();
						int py2 = doorInst.getYMax();
						int pz2 = doorInst.getZMax();
						
						int l = tx - x;
						int m = ty - y;
						int n = tz - z;
						
						int dk;
						
						if ((dk = (doorInst.getA() * l + doorInst.getB() * m + doorInst.getC() * n)) == 0) continue; // Parallel
						
						float p = (float)(doorInst.getA() * x + doorInst.getB() * y + doorInst.getC() * z + doorInst.getD()) / (float)dk;
						
						int fx = (int)(x - l * p);
						int fy = (int)(y - m * p);
						int fz = (int)(z - n * p);
						
						if((Math.min(x,tx) <= fx && fx <= Math.max(x,tx)) &&
								(Math.min(y,ty) <= fy && fy <= Math.max(y,ty)) &&
								(Math.min(z,tz) <= fz && fz <= Math.max(z,tz)))
						{

							if (((fx >= px1 && fx <= px2) || (fx >= px2 && fx <= px1)) &&
									((fy >= py1 && fy <= py2) || (fy >= py2 && fy <= py1)) &&
									((fz >= pz1 && fz <= pz2) || (fz >= pz2 && fz <= pz1)))
								return true; // Door between
						}
					}
				}
			}
		}
		return false;
	}

}
