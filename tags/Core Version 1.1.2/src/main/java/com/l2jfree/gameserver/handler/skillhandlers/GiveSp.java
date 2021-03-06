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

package com.l2jfree.gameserver.handler.skillhandlers;

import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillType;

/**
 * @author Forsaiken
 */

public class GiveSp implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = {SkillType.GIVE_SP};

	public void useSkill(@SuppressWarnings("unused") L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for (L2Object obj : targets)
		{
			L2Character target = (L2Character)obj;
			if (target != null)
			{
				int spToAdd = (int)skill.getPower();
				target.addExpAndSp(0, spToAdd);
			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}