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

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.SkillsEngine;
import com.l2jfree.gameserver.templates.L2WeaponType;
import com.l2jfree.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.6.2.18 $ $Date: 2005/04/06 16:13:25 $
 */
public class SkillTable
{
	private static SkillTable		_instance;
	private FastMap<Integer, L2Skill>	_skills;

	public static SkillTable getInstance()
	{
		if (_instance == null)
			_instance = new SkillTable();

		return _instance;
	}

	private SkillTable()
	{
		_skills = new FastMap<Integer, L2Skill>();
		SkillsEngine.getInstance().loadAllSkills(_skills);
	}

	public void reload()
	{
		_instance = new SkillTable();
	}

	/**
	 * Provides the skill hash
	 * @param skill The L2Skill to be hashed
	 * @return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(L2Skill skill)
	{
		return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel());
	}

	/**
	 * Centralized method for easier change of the hashing sys
	 * @param skillId The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return Util.hash(skillId * 512 + skillLevel);
	}

	public L2Skill getInfo(int skillId, int level)
	{
		return _skills.get(SkillTable.getSkillHashCode(skillId, level));
	}

	public int getMaxLevel(int magicId, int level)
	{
		L2Skill temp;

		while (level < 100)
		{
			temp = _skills.get(SkillTable.getSkillHashCode(magicId, ++level));

			if (temp == null)
				return level - 1;
		}

		return level;
	}

	private static final L2WeaponType[]	weaponDbMasks	=
														{
			L2WeaponType.ETC,
			L2WeaponType.BOW,
			L2WeaponType.POLE,
			L2WeaponType.DUALFIST,
			L2WeaponType.DUAL,
			L2WeaponType.BLUNT,
			L2WeaponType.SWORD,
			L2WeaponType.DAGGER,
			L2WeaponType.BIGSWORD,
			L2WeaponType.ROD,
			L2WeaponType.BIGBLUNT,
			L2WeaponType.ANCIENT_SWORD,
			L2WeaponType.RAPIER,
			L2WeaponType.CROSSBOW						};

	public int calcWeaponsAllowed(int mask)
	{
		if (mask == 0)
			return 0;

		int weaponsAllowed = 0;

		for (int i = 0; i < weaponDbMasks.length; i++)
		{
			if ((mask & (1 << i)) != 0)
				weaponsAllowed |= weaponDbMasks[i].mask();
		}

		return weaponsAllowed;
	}

	/**
	 * Returns an array with siege skills. If addNoble == true, will add also Advanced headquarters.
	 */
	public L2Skill[] getSiegeSkills(boolean addNoble)
	{
		FastList<L2Skill> list = new FastList<L2Skill>();

		list.add(_skills.get(SkillTable.getSkillHashCode(246, 1))); 
		list.add(_skills.get(SkillTable.getSkillHashCode(247, 1)));

		if (addNoble)
			list.add(_skills.get(SkillTable.getSkillHashCode(326, 1)));

		L2Skill[] temp = new L2Skill[list.size()];
		list.toArray(temp);

		return temp;
	}

}