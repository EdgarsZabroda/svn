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
package com.l2jfree.gameserver.geodata.pathfinding.cellnodes;

import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.geodata.pathfinding.AbstractNodeLoc;
import com.l2jfree.gameserver.geodata.pathfinding.Node;
import com.l2jfree.gameserver.geodata.pathfinding.PathFinding;
import com.l2jfree.gameserver.model.L2World;

/**
 * @author Sami
 */
public class CellPathFinding extends PathFinding
{
	//private static Logger _log = Logger.getLogger(WorldPathFinding.class.getName());
	private static CellPathFinding _instance;
	
	public static CellPathFinding getInstance()
	{
		if (_instance == null)
			_instance = new CellPathFinding();
		
		return _instance;
	}
	
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#PathNodesExist(short)
	 */
	@Override
	public boolean pathNodesExist(short regionoffset)
	{
		return false;
	}
	
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#FindPath(int, int, short, int, int, short)
	 */
	@Override
	public AbstractNodeLoc[] findPath(int x, int y, int z, int tx, int ty, int tz)
	{
		int gx = (x - L2World.MAP_MIN_X) >> 4;
		int gy = (y - L2World.MAP_MIN_Y) >> 4;
		if (!GeoData.getInstance().hasGeo(x, y))
			return null;
		short gz = GeoData.getInstance().getHeight(x, y, z);
		int gtx = (tx - L2World.MAP_MIN_X) >> 4;
		int gty = (ty - L2World.MAP_MIN_Y) >> 4;
		if (!GeoData.getInstance().hasGeo(tx, ty))
			return null;
		short gtz = GeoData.getInstance().getHeight(tx, ty, tz);
		Node start = readNode(gx, gy, gz);
		Node end = readNode(gtx, gty, gtz);
		return searchByClosest(start, end);
	}
	
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#ReadNeighbors(short, short)
	 */
	@Override
	public Node[] readNeighbors(Node n, int idx)
	{
		return GeoData.getInstance().getNeighbors(n);
	}
	
	//Private
	
	public Node readNode(int gx, int gy, short z)
	{
		return new Node(new NodeLoc(gx, gy, z), 0);
	}
	
	private CellPathFinding()
	{
		//
	}
}
