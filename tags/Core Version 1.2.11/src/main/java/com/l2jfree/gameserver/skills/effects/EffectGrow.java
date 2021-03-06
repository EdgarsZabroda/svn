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
package com.l2jfree.gameserver.skills.effects;

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

public final class EffectGrow extends L2Effect
{
	public EffectGrow(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2NpcInstance)
		{
			//L2NpcInstance npc = (L2NpcInstance) getEffected();
			//TODO: Uncomment lines when fix for mobs falling underground is found
			//npc.setCollisionHeight((int) (npc.getTemplate().getCollisionHeight() * 1.24));
			//npc.setCollisionRadius((int) (npc.getTemplate().getCollisionRadius() * 1.19));
			
			getEffected().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_GROW);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		//L2NpcInstance npc = (L2NpcInstance) getEffected();
		//TODO: Uncomment lines when fix for mobs falling underground is found
		//npc.setCollisionHeight(npc.getTemplate().getCollisionHeight());
		//npc.setCollisionRadius(npc.getTemplate().getCollisionRadius());
		getEffected().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_GROW);
	}
}
