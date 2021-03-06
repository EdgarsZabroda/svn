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
package com.l2jfree.gameserver.network.client.packets.sendable;

import com.l2jfree.gameserver.gameobjects.L2Player;
import com.l2jfree.gameserver.network.client.L2Client;
import com.l2jfree.gameserver.network.client.packets.L2ServerPacket;
import com.l2jfree.network.mmocore.MMOBuffer;

/**
 * @author savormix (generated)
 */
public abstract class GMViewCharacterInfoPacket extends L2ServerPacket
{
	/**
	 * A nicer name for {@link GMViewCharacterInfoPacket}.
	 * 
	 * @author savormix (generated)
	 * @see GMViewCharacterInfoPacket
	 */
	public static final class ViewPlayerInfo extends GMViewCharacterInfoPacket
	{
		/**
		 * Constructs this packet.
		 * 
		 * @see GMViewCharacterInfoPacket#GMViewCharacterInfoPacket()
		 */
		public ViewPlayerInfo()
		{
		}
	}
	
	/** Constructs this packet. */
	public GMViewCharacterInfoPacket()
	{
	}
	
	@Override
	protected int getOpcode()
	{
		return 0x95;
	}
	
	@Override
	protected void writeImpl(L2Client client, L2Player activeChar, MMOBuffer buf) throws RuntimeException
	{
		// TODO: when implementing, consult an up-to-date packets_game_server.xml and/or savormix
		buf.writeD(0); // Location X
		buf.writeD(0); // Location Y
		buf.writeD(0); // Location Z
		buf.writeD(0); // Vehicle OID
		buf.writeD(0); // My OID
		buf.writeS(""); // Name
		buf.writeD(0); // Race
		buf.writeD(0); // Sex
		buf.writeD(0); // Starter class
		buf.writeD(0); // Level
		buf.writeQ(0L); // XP
		buf.writeF(0D); // XP %
		buf.writeD(0); // STR
		buf.writeD(0); // DEX
		buf.writeD(0); // CON
		buf.writeD(0); // INT
		buf.writeD(0); // WIT
		buf.writeD(0); // MEN
		buf.writeD(0); // Maximum HP
		buf.writeD(0); // Current HP
		buf.writeD(0); // Maximum MP
		buf.writeD(0); // Current MP
		buf.writeD(0); // SP
		buf.writeD(0); // Current carried weight
		buf.writeD(0); // Maximum carried weight
		buf.writeD(0); // Weapon status
		buf.writeD(0); // Shirt OID
		buf.writeD(0); // Right earring OID
		buf.writeD(0); // Left earring OID
		buf.writeD(0); // Necklace OID
		buf.writeD(0); // Right ring OID
		buf.writeD(0); // Left ring OID
		buf.writeD(0); // Helmet OID
		buf.writeD(0); // Main weapon OID
		buf.writeD(0); // Shield/support weapon OID
		buf.writeD(0); // Gloves OID
		buf.writeD(0); // Chest armor OID
		buf.writeD(0); // Leg armor OID
		buf.writeD(0); // Boots OID
		buf.writeD(0); // Cloak OID
		buf.writeD(0); // Two-handed weapon OID
		buf.writeD(0); // 1st hair item OID
		buf.writeD(0); // 2nd hair item OID
		buf.writeD(0); // Right bracelet OID
		buf.writeD(0); // Left bracelet OID
		buf.writeD(0); // 1st talisman OID
		buf.writeD(0); // 2nd talisman OID
		buf.writeD(0); // 3rd talisman OID
		buf.writeD(0); // 4th talisman OID
		buf.writeD(0); // 5th talisman OID
		buf.writeD(0); // 6th talisman OID
		buf.writeD(0); // Belt OID
		buf.writeD(0); // Shirt
		buf.writeD(0); // Right earring
		buf.writeD(0); // Left earring
		buf.writeD(0); // Necklace
		buf.writeD(0); // Right ring
		buf.writeD(0); // Left ring
		buf.writeD(0); // Helmet
		buf.writeD(0); // Main weapon
		buf.writeD(0); // Shield/support weapon
		buf.writeD(0); // Gloves
		buf.writeD(0); // Chest armor
		buf.writeD(0); // Leg armor
		buf.writeD(0); // Boots
		buf.writeD(0); // Cloak
		buf.writeD(0); // Two-handed weapon
		buf.writeD(0); // 1st hair item
		buf.writeD(0); // 2nd hair item
		buf.writeD(0); // Right bracelet
		buf.writeD(0); // Left bracelet
		buf.writeD(0); // 1st talisman
		buf.writeD(0); // 2nd talisman
		buf.writeD(0); // 3rd talisman
		buf.writeD(0); // 4th talisman
		buf.writeD(0); // 5th talisman
		buf.writeD(0); // 6th talisman
		buf.writeD(0); // Belt
		buf.writeD(0); // Shirt augmentation
		buf.writeD(0); // Right earring augmentation
		buf.writeD(0); // Left earring augmentation
		buf.writeD(0); // Necklace augmentation
		buf.writeD(0); // Right ring augmentation
		buf.writeD(0); // Left ring augmentation
		buf.writeD(0); // Helmet augmentation
		buf.writeD(0); // Main weapon augmentation
		buf.writeD(0); // Shield/support weapon augmentation
		buf.writeD(0); // Gloves augmentation
		buf.writeD(0); // Chest armor augmentation
		buf.writeD(0); // Leg armor augmentation
		buf.writeD(0); // Boots augmentation
		buf.writeD(0); // Cloak augmentation
		buf.writeD(0); // Two-handed weapon augmentation
		buf.writeD(0); // 1st hair item augmentation
		buf.writeD(0); // 2nd hair item augmentation
		buf.writeD(0); // Right bracelet augmentation
		buf.writeD(0); // Left bracelet augmentation
		buf.writeD(0); // 1st talisman augmentation
		buf.writeD(0); // 2nd talisman augmentation
		buf.writeD(0); // 3rd talisman augmentation
		buf.writeD(0); // 4th talisman augmentation
		buf.writeD(0); // 5th talisman augmentation
		buf.writeD(0); // 6th talisman augmentation
		buf.writeD(0); // Belt augmentation
		buf.writeD(0); // Talisman slots
		buf.writeD(0); // Can equip cloak
		buf.writeD(0); // P. Atk.
		buf.writeD(0); // Attack speed
		buf.writeD(0); // P. Def.
		buf.writeD(0); // Evasion
		buf.writeD(0); // Accuracy
		buf.writeD(0); // Critical
		buf.writeD(0); // M. Atk.
		buf.writeD(0); // Casting speed
		buf.writeD(0); // Attack speed (dupe)
		buf.writeD(0); // M. Def.
		buf.writeD(0); // In PvP
		buf.writeD(0); // Karma
		buf.writeD(0); // Running speed (on ground)
		buf.writeD(0); // Walking speed (on ground)
		buf.writeD(0); // Running speed (in water)
		buf.writeD(0); // Walking speed (in water)
		buf.writeD(0); // Running speed (in air) ???
		buf.writeD(0); // Walking speed (in air) ???
		buf.writeD(0); // Running speed (in air) while mounted?
		buf.writeD(0); // Walking speed (in air) while mounted?
		buf.writeF(0D); // Movement speed multiplier
		buf.writeF(0D); // Attack speed multiplier
		buf.writeF(0D); // Collision radius
		buf.writeF(0D); // Collision height
		buf.writeD(0); // Hair style
		buf.writeD(0); // Hair color
		buf.writeD(0); // Face
		buf.writeD(0); // Game Master
		buf.writeS(""); // Title
		buf.writeD(0); // Pledge ID
		buf.writeD(0); // Pledge crest ID
		buf.writeD(0); // Alliance ID
		buf.writeC(0); // Mount type
		buf.writeC(0); // Private store
		buf.writeC(0); // Can use dwarven recipes
		buf.writeD(0); // PK Count
		buf.writeD(0); // PvP Count
		buf.writeH(0); // Recomendations
		buf.writeH(0); // Evaluation score
		buf.writeD(0); // Current class
		buf.writeD(0); // 0
		buf.writeD(0); // Maximum CP
		buf.writeD(0); // Current CP
		buf.writeC(0); // Moving
		buf.writeC(0); // ???
		buf.writeD(0); // Pledge rank
		buf.writeC(0); // Noble
		buf.writeC(0); // Hero
		buf.writeD(0); // Name color
		buf.writeD(0); // Title color
		buf.writeH(0); // Attack element
		buf.writeH(0); // Attack element power
		buf.writeH(0); // Fire defense
		buf.writeH(0); // Water defense
		buf.writeH(0); // Wind defense
		buf.writeH(0); // Earth defense
		buf.writeH(0); // Holy defense
		buf.writeH(0); // Dark defense
		buf.writeD(0); // Fame
		buf.writeD(0); // Vitality
	}
}
