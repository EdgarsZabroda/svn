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
package com.l2jfree.gameserver.skills.l2skills;

import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.L2Trap;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2TrapInstance;
import com.l2jfree.gameserver.templates.L2NpcTemplate;
import com.l2jfree.gameserver.templates.StatsSet;

public class L2SkillTrap extends L2Skill
{
	private int			_triggerSkillId		= 0;
	private int			_triggerSkillLvl	= 0;
	private int			_trapNpcId			= 0;
	protected L2Spawn	_trapSpawn;

	/**
	 * 
	 * @param set
	 */
	public L2SkillTrap(StatsSet set)
	{
		super(set);
		_triggerSkillId = set.getInteger("triggerSkillId");
		_triggerSkillLvl = set.getInteger("triggerSkillLvl");
		_trapNpcId = set.getInteger("trapNpcId");
	}

	/**
	 * 
	 * @see com.l2jfree.gameserver.model.L2Skill#useSkill(com.l2jfree.gameserver.model.L2Character, L2Object...)
	 */
	@Override
	public void useSkill(L2Character caster, @SuppressWarnings("unused")
	L2Object... targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
			return;

		if (_trapNpcId == 0)
			return;

		L2PcInstance activeChar = (L2PcInstance) caster;

		if (activeChar.getTrap() != null)
			return;

		if (activeChar.inObserverMode())
			return;

		if (activeChar.isMounted())
			return;

		if (_triggerSkillId == 0 || _triggerSkillLvl == 0)
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(_triggerSkillId, _triggerSkillLvl);

		if (skill == null)
			return;

		L2Trap trap;
		L2NpcTemplate TrapTemplate = NpcTable.getInstance().getTemplate(_trapNpcId);
		trap = new L2TrapInstance(IdFactory.getInstance().getNextId(), TrapTemplate, activeChar, getTotalLifeTime(), skill);
		trap.getStatus().setCurrentHp(trap.getMaxHp());
		trap.getStatus().setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(activeChar.getHeading());
		activeChar.setTrap(trap);
		L2World.getInstance().storeObject(trap);
		trap.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
}
