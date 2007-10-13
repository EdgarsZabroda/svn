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
package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MapRegionTable
{
    private final static Log _log = LogFactory.getLog(MapRegionTable.class.getName());

    private static MapRegionTable _instance;

    private final int[][] _regions = new int[20][21];

    public static enum TeleportWhereType
    {
        Castle, ClanHall, SiegeFlag, Town
    }

    public static MapRegionTable getInstance()
    {
        if (_instance == null)
        {
            _instance = new MapRegionTable();
        }
        return _instance;
    }

    private MapRegionTable()
    {
        int count2 = 0;

        // LineNumberReader lnr = null;
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con
            .prepareStatement("SELECT region, sec0, sec1, sec2, sec3, sec4, sec5, sec6, sec7, sec8, sec9 FROM mapregion");
            ResultSet rset = statement.executeQuery();
            int region;
            while (rset.next())
            {
                region = rset.getInt(1);

                for (int j = 0; j < 10; j++)
                {
                    _regions[j][region] = rset.getInt(j + 2);
                    count2++;
                    // _log.debug(j+","+region+" -> "+rset.getInt(j+2));
                }
            }

            rset.close();
            statement.close();
            if (_log.isDebugEnabled())
                _log.debug(count2 + " mapregion loaded");
        } catch (Exception e)
        {
            _log.warn("error while creating map region data: " + e);
        } finally
        {
            try
            {
                con.close();
            } catch (Exception e)
            {
            }
        }
    }

    public final int getMapRegion(int posX, int posY)
    {
        return _regions[getMapRegionX(posX)][getMapRegionY(posY)];
    }

    public final int getMapRegionX(int posX)
    {
        return (posX >> 15) + 4;// + centerTileX;
    }

    public final int getMapRegionY(int posY)
    {
        return (posY >> 15) + 10;// + centerTileX;
    }

    public int getAreaCastle(L2Character activeChar)
    {
        int area = getClosestTownNumber(activeChar);
        int castle;
        switch (area) 
        {
        case 0:  castle = 1; break;//Talking Island Village
        case 1:  castle = 4; break; //Elven Village
        case 2:  castle = 4; break; //Dark Elven Village
        case 3:  castle = 9; break; //Orc Village
        case 4:  castle = 9; break; //Dwarven Village
        case 5:  castle = 1; break; //Town of Gludio
        case 6:  castle = 1; break; //Gludin Village
        case 7:  castle = 2; break; //Town of Dion
        case 8:  castle = 3; break; //Town of Giran
        case 9:  castle = 4; break; //Town of Oren
        case 10: castle = 5; break; //Town of Aden
        case 11: castle = 5; break; //Hunters Village
        case 12: castle = 3; break; //Giran Harbor
        case 13: castle = 6; break; //Heine
        case 14: castle = 8; break; //Rune Township
        case 15: castle = 7; break; //Town of Goddard
        case 16: castle = 9; break; //Town of Shuttgart 
        case 17: castle = 4; break; //Ivory Tower
        case 18: castle = 8; break; //Primeval Isle Wharf
        default: castle = 5; break; //Town of Aden
        }
        return castle;
    }

    public int getClosestTownNumber(L2Character activeChar)
    {
        return getMapRegion(activeChar.getX(), activeChar.getY());
    }

    public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
    {
        if (activeChar instanceof L2PcInstance)
        {
            L2PcInstance player = ((L2PcInstance) activeChar);
            L2Clan clan = player.getClan();
            Castle castle = null;
            ClanHall clanhall = null;
            int townId = getClosestTownNumber(player);

            // Checking if in Dimensinal Gap
            if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), true)) // true -> ignore waiting room :)
            {
                if(player.isInParty() && player.getParty().isInDimensionalRift())
                {
                    player.getParty().getDimensionalRift().usedTeleport(player);
                }
                
                return DimensionalRiftManager.getInstance().getWaitingRoomTeleport();
            }
            
            // Checking if in arena
            for (IZone arena : ZoneManager.getInstance().getZones(ZoneType.Arena, player.getX(), player.getY()))
            {
                if (arena != null && arena.checkIfInZone(player))
                {
                    Location loc = arena.getRestartPoint(RestartType.RestartNormal);
                    if (loc == null)
                        loc = arena.getRestartPoint(RestartType.RestartRandom);
                    return loc;
                }
            }

            if (teleportWhere == TeleportWhereType.Town)
            {
                // Karma player land out of city
                if (player.getKarma() > 1 || player.isCursedWeaponEquiped())
                {
                    if (townId >= 0)
                        return TownManager.getInstance().getClosestTown(activeChar).getKarmaSpawn();
                    else // Floran Village
                        return TownManager.getInstance().getTown(16).getSpawn();
                }

                return TownManager.getInstance().getClosestTown(activeChar).getSpawn();
            }

            if (clan != null)
            {
                // If teleport to clan hall
                if (teleportWhere == TeleportWhereType.ClanHall)
                {
                    clanhall = ClanHallManager.getInstance().getClanHallByOwner(clan);
                    if (clanhall != null)
                    {
                        IZone zone = clanhall.getZone();

                        if (zone != null)
                        {
                            Location loc = zone.getRestartPoint(RestartType.RestartOwner);
                            if (loc == null)
                                loc = zone.getRestartPoint(RestartType.RestartRandom);
                            return loc;
                        }
                    }
                }

                // If teleport to castle
                if (teleportWhere == TeleportWhereType.Castle)
                    castle = CastleManager.getInstance().getCastleByOwner(clan);

                // If Teleporting to castle or
                if (castle != null && teleportWhere == TeleportWhereType.Castle)
                {

                    IZone zone = castle.getZone();

                    // If is on castle with siege and player's clan is defender
                    if (castle.getSiege() != null && castle.getSiege().getIsInProgress())
                    {
                        // Karma player respawns out of siege zone
                        if (player.getKarma() > 1 || player.isCursedWeaponEquiped())
                        {
                            zone = castle.getZone();
                            if (zone != null)
                            {
                                return zone.getRestartPoint(RestartType.RestartChaotic);
                            }
                        } else
                        {

                            zone = castle.getDefenderSpawn();
                            if (zone != null)
                            {
                                return zone.getRestartPoint(RestartType.RestartRandom);
                            }
                        }
                    }

                    zone = castle.getZone();
                    if (zone != null)
                    {
                        return zone.getRestartPoint(RestartType.RestartOwner);
                    }
                }
                else if (teleportWhere == TeleportWhereType.SiegeFlag)
                {
                    Siege siege = SiegeManager.getInstance().getSiege(clan);

                    // Check if player's clan is attacker
                    if (siege != null && siege.checkIsAttacker(clan) && siege.checkIfInZone(player))
                    {
                        // Karma player respawns out of siege zone
                        if (player.getKarma() > 1 || player.isCursedWeaponEquiped())
                        {
                            IZone zone = siege.getCastle().getZone();
                            if (zone != null)
                            {
                                return zone.getRestartPoint(RestartType.RestartChaotic);
                            }
                        }
                        // get nearest flag
                        L2NpcInstance flag = siege.getClosestFlag(player);
                        // spawn to flag
                        if (flag != null)
                            return new Location(flag.getX(), flag.getY(), flag.getZ());
                    }
                }
            }
        }

        // Get the nearest town
        // TODO: Micht: Maybe we should add some checks to prevent exception
        // here.
        return TownManager.getInstance().getClosestTown(activeChar).getSpawn();
    }
}
