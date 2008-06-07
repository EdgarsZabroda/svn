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
package com.l2jfree.gameserver.idfactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/04/11 10:06:12 $
 */
public abstract class IdFactory
{
	private final static Log		_log				= LogFactory.getLog(IdFactory.class.getName());

	protected static final String[]	ID_UPDATES			=
														{
			"UPDATE items                 SET owner_id = ?    WHERE owner_id = ?",
			"UPDATE items                 SET object_id = ?   WHERE object_id = ?",
			"UPDATE character_quests      SET charId = ?      WHERE charId = ?",
			"UPDATE character_friends     SET charId = ?      WHERE charId = ?",
			"UPDATE character_friends     SET friendId = ?    WHERE friendId = ?",
			"UPDATE character_hennas      SET charId = ?      WHERE charId = ?",
			"UPDATE character_recipebook  SET charId = ?      WHERE charId = ?",
			"UPDATE character_shortcuts   SET charId = ?      WHERE charId = ?",
			"UPDATE character_shortcuts   SET shortcut_id = ? WHERE shortcut_id = ? AND type = 1", // items
			"UPDATE character_macroses    SET charId = ?      WHERE charId = ?",
			"UPDATE character_skills      SET charId = ?      WHERE charId = ?",
			"UPDATE character_skills_save SET charId = ?      WHERE charId = ?",
			"UPDATE character_subclasses  SET charId = ?      WHERE charId = ?",
			"UPDATE characters            SET charId = ?      WHERE charId = ?",
			"UPDATE characters            SET clanid = ?      WHERE clanid = ?",
			"UPDATE clan_data             SET clan_id = ?     WHERE clan_id = ?",
			"UPDATE siege_clans           SET clan_id = ?     WHERE clan_id = ?",
			"UPDATE clan_data             SET ally_id = ?     WHERE ally_id = ?",
			"UPDATE clan_data             SET leader_id = ?   WHERE leader_id = ?",
			"UPDATE pets                  SET item_obj_id = ? WHERE item_obj_id = ?",
			// added by DaDummy
			"UPDATE auction_bid          SET bidderId = ?      WHERE bidderId = ?",
			"UPDATE auction_watch        SET charId = ?        WHERE charId = ?",
			"UPDATE character_hennas     SET charId = ?        WHERE charId = ?",
			"UPDATE clan_wars            SET clan1 = ?         WHERE clan1 = ?",
			"UPDATE clan_wars            SET clan2 = ?         WHERE clan2 = ?",
			"UPDATE clanhall             SET ownerId = ?       WHERE ownerId = ?",
			"UPDATE petitions            SET charId = ?        WHERE charId = ?",
			"UPDATE posts                SET post_ownerid = ?  WHERE post_ownerid = ?",
			"UPDATE seven_signs          SET charId = ?        WHERE charId = ?",
			"UPDATE topic                SET topic_ownerid = ? WHERE topic_ownerid = ?",
			"UPDATE itemsonground        SET object_id = ?     WHERE object_id = ?",
			// added by GDL
			"UPDATE olympiad_nobles          SET charId = ?         WHERE charId = ?",
			"UPDATE clan_privs               SET clan_id = ?        WHERE clan_id = ?",
			"UPDATE clan_skills              SET clan_id = ?        WHERE clan_id = ?",
			"UPDATE clan_subpledges          SET clan_id = ?        WHERE clan_id = ?",
			"UPDATE character_recommends     SET charId = ?         WHERE charId = ?",
			"UPDATE character_recommends     SET target_id = ?      WHERE target_id = ?",
			"UPDATE character_raidpoints     SET owner_id = ?       WHERE owner_id = ?",
			"UPDATE couples                  SET id = ?             WHERE id = ?",
			"UPDATE couples                  SET player1Id = ?      WHERE player1Id = ?",
			"UPDATE couples                  SET player2Id = ?      WHERE player2Id = ?",
			"UPDATE cursed_weapons           SET playerId = ?       WHERE playerId = ?",
			"UPDATE forums                   SET forum_owner_id = ? WHERE forum_owner_id = ?",
			"UPDATE heroes                   SET charId = ?         WHERE charId = ?" };

	protected static final String[]	ID_CHECKS			=
														{
			"SELECT owner_id    FROM items                 WHERE object_id >= ?   AND object_id < ?",
			"SELECT object_id   FROM items                 WHERE object_id >= ?   AND object_id < ?",
			"SELECT charId      FROM character_quests      WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_friends     WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_friends     WHERE friendId >= ?    AND friendId < ?",
			"SELECT charId      FROM character_hennas      WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_recipebook  WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_shortcuts   WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_macroses    WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_skills      WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_skills_save WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM character_subclasses  WHERE charId >= ?      AND charId < ?",
			"SELECT charId      FROM characters            WHERE charId >= ?      AND charId < ?",
			"SELECT clanid      FROM characters            WHERE clanid >= ?      AND clanid < ?",
			"SELECT clan_id     FROM clan_data             WHERE clan_id >= ?     AND clan_id < ?",
			"SELECT clan_id     FROM siege_clans           WHERE clan_id >= ?     AND clan_id < ?",
			"SELECT ally_id     FROM clan_data             WHERE ally_id >= ?     AND ally_id < ?",
			"SELECT leader_id   FROM clan_data             WHERE leader_id >= ?   AND leader_id < ?",
			"SELECT item_obj_id FROM pets                  WHERE item_obj_id >= ? AND item_obj_id < ?",
			// added by DaDummy
			"SELECT friendId    FROM character_friends     WHERE friendId >= ?    AND friendId < ?",
			"SELECT charId      FROM seven_signs           WHERE charId >= ?      AND charId < ?",
			"SELECT object_id   FROM itemsonground         WHERE object_id >= ?   AND object_id < ?" };

	protected boolean				_initialized;

	public static final int			FIRST_OID			= 0x10000000;
	public static final int			LAST_OID			= 0x7FFFFFFF;
	public static final int			FREE_OBJECT_ID_SIZE	= LAST_OID - FIRST_OID;

	protected static IdFactory		_instance			= null;

	protected IdFactory()
	{
		setAllCharacterOffline();
		cleanUpDB();
	}

	static
	{
		switch (Config.IDFACTORY_TYPE)
		{
		case Compaction:
			_instance = new CompactionIDFactory();
			break;
		case BitSet:
			_instance = new BitSetIDFactory();
			break;
		case Stack:
			_instance = new StackIDFactory();
			break;
		case Increment:
			_instance = new IncrementIDFactory();
			break;
		case Rebuild:
			_instance = new BitSetRebuildFactory();
			break;
		}
	}

	/**
	 * Sets all character offline
	 */
	protected void setAllCharacterOffline()
	{
		Connection con2 = null;
		try
		{
			con2 = L2DatabaseFactory.getInstance().getConnection(con2);
			Statement s2 = con2.createStatement();
			s2.executeUpdate("UPDATE characters SET online = 0;");
			if (_log.isDebugEnabled())
				_log.debug("Updated characters online status.");
			s2.close();
		}
		catch (SQLException e)
		{
		}
		finally
		{
			try
			{
				con2.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Cleans up Database
	 */
	protected void cleanUpDB()
	{
		// TODO:
		// Check for more cleanup query
		// Check order

		Connection conn = null;
		try
		{
			int cleanCount = 0;
			conn = L2DatabaseFactory.getInstance().getConnection(conn);
			Statement stmt = conn.createStatement();

			// If a character not exists
			cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.friendId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_quests WHERE character_quests.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_skills WHERE character_skills.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_raidpoints WHERE owner_id NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM couples WHERE couples.player1Id NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM couples WHERE couples.player2Id NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM heroes WHERE heroes.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles WHERE charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM items WHERE loc <> 'clanwh' and items.owner_id NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
			cleanCount += stmt.executeUpdate("DELETE FROM seven_signs WHERE seven_signs.charId NOT IN (SELECT charId FROM characters);");

			// If a clan not exists
			cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data) OR clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM forums WHERE forum_owner_id <> 0 AND forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM items WHERE loc = 'clanwh' and items.owner_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");

			stmt.executeUpdate("UPDATE characters SET `clanid`='0', `clan_privs`='0', `clan_join_expiry_time`='0', `clan_create_expiry_time`='0' WHERE characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
			stmt.executeUpdate("UPDATE clan_data SET ally_id=0 WHERE clan_data.ally_id NOT IN (SELECT clanid FROM characters WHERE clanid!=0 GROUP BY clanid);");
			stmt.executeUpdate("UPDATE clanhall SET ownerId=0, paidUntil=0, paid=0 WHERE clanhall.ownerId NOT IN (SELECT clan_id FROM clan_data);");

			// If the clanhall isn't free
			cleanCount += stmt.executeUpdate("DELETE FROM auction WHERE auction.id IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.auctionId IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			stmt.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
			// If the clanhall is free
			cleanCount += stmt.executeUpdate("DELETE FROM clanhall_functions WHERE clanhall_functions.hall_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0);");

			// Others
			stmt.executeUpdate("UPDATE items SET loc='INVENTORY' WHERE loc='PAPERDOLL' AND loc_data=0;");
			stmt.executeUpdate("DELETE FROM augmentations WHERE `item_id` NOT IN (SELECT object_id FROM items);");

			stmt.close();
			_log.info("Cleaned " + cleanCount + " elements from database.");
		}
		catch (SQLException e)
		{
			_log.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	protected int[] extractUsedObjectIDTable() throws SQLException
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			//create a temporary table
			Statement s = con.createStatement();
			try
			{
				s.executeUpdate("DROP TABLE temporaryObjectTable");
			}
			catch (SQLException e)
			{
			}

			s.executeUpdate("CREATE TABLE temporaryObjectTable" + " (object_id int NOT NULL PRIMARY KEY)");

			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id)" + " SELECT charId FROM characters");
			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id)" + " SELECT object_id FROM items");
			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id)" + " SELECT clan_id FROM clan_data");
			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id)" + " SELECT object_id FROM itemsonground");

			ResultSet result = s.executeQuery("SELECT COUNT(object_id) FROM temporaryObjectTable");

			result.next();
			int size = result.getInt(1);
			int[] tmp_obj_ids = new int[size];
			if (_log.isDebugEnabled())
				_log.info("tmp table size: " + tmp_obj_ids.length);
			result.close();

			result = s.executeQuery("select object_id from temporaryObjectTable ORDER BY object_id");

			int idx = 0;
			while (result.next())
			{
				tmp_obj_ids[idx++] = result.getInt(1);
			}

			result.close();
			s.close();

			return tmp_obj_ids;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public static IdFactory getInstance()
	{
		return _instance;
	}

	public abstract int getNextId();

	/**
	 * return a used Object ID back to the pool
	 * @param object ID
	 */
	public abstract void releaseId(int id);

	public abstract int getCurrentId();

	public abstract int size();
}
