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
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.model.zone.L2FishingZone;
import net.sf.l2j.gameserver.model.zone.L2WaterZone;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.model.zone.L2Zone.ZoneType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ZoneManager
{
	protected static Log _log = LogFactory.getLog(ZoneManager.class.getName());

	private static ZoneManager _instance;

	private FastMap<ZoneType, FastList<L2Zone>> _zones;

	public static final ZoneManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ZoneManager();
			_instance.load();
		}
		return _instance;
	}

	public void reload()
	{
		int zoneCount = 0;
		
		// Get the world regions
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		for (int x=0; x < worldRegions.length; x++)
		{
			for (int y=0; y < worldRegions[x].length; y++)
			{
				worldRegions[x][y].getZones().clear();
			}
		}

		// Load the zones
		load();
	}

	private void load()
	{
		Document doc = null;

		for (File f : Util.getDatapackFiles("zone", ".xml"))
		{
			int count = 0;
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error loading file " + f.getAbsolutePath(), e);
				continue;
			}
			try
			{
				count = parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error in file " + f.getAbsolutePath(), e);
				e.printStackTrace();
				continue;
			}
			_log.info("ZoneManager: "+f.getName()+" loaded with "+count+" zones");
		}
	}

	protected int parseDocument(Document doc)
	{
		int zoneCount = 0;
		
		// Get the world regions
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("zone".equalsIgnoreCase(d.getNodeName()))
					{
						L2Zone zone = L2Zone.parseZone(d);
						if(zone == null)continue;
						
						// Register the zone to any intersecting world region
						int ax,ay,bx,by;
						for (int x=0; x < worldRegions.length; x++)
						{
							for (int y=0; y < worldRegions[x].length; y++)
							{
								ax = (x-L2World.OFFSET_X) << L2World.SHIFT_BY;
								bx = ((x+1)-L2World.OFFSET_X) << L2World.SHIFT_BY;
								ay = (y-L2World.OFFSET_Y) << L2World.SHIFT_BY;
								by = ((y+1)-L2World.OFFSET_Y) << L2World.SHIFT_BY;
								
								if (zone.intersectsRectangle(ax, bx, ay, by))
								{
									worldRegions[x][y].addZone(zone);
								}
							}
						}
						zoneCount++;
					}
				}
			}
		}
		return zoneCount;
	}

	public static short getMapRegion(int x, int y)
	{
		int rx = ((x - L2World.MAP_MIN_X) >> 15) + 16;
		int ry = ((y - L2World.MAP_MIN_Y) >> 15) + 10;
		return (short) ((rx << 8) + ry);
	}

	public FastMap<L2Zone.ZoneType, FastList<L2Zone>> getZoneMap()
	{
		if (_zones == null)
			_zones = new FastMap<L2Zone.ZoneType, FastList<L2Zone>>();
		return _zones;
	}

	public FastList<L2Zone> getZones(L2Zone.ZoneType type)
	{
		if (!getZoneMap().containsKey(type))
			getZoneMap().put(type, new FastList<L2Zone>());

		return getZoneMap().get(type);
	}

	public final L2Zone isInsideZone(L2Zone.ZoneType zt, int x, int y)
	{
		for (L2Zone temp : getZones(zt))
			if (temp.isInsideZone(x, y))
				return temp;
		return null;
	}
}
