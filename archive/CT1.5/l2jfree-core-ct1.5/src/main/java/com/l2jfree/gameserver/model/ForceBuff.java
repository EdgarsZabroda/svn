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

package com.l2jfree.gameserver.model;

import java.util.concurrent.Future;

import com.l2jfree.gameserver.GeoData;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.effects.EffectForce;
import com.l2jfree.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author kombat/crion
 *
 */
public final class ForceBuff
{
	protected static final Log _log = LogFactory.getLog(ForceBuff.class);

	protected int _skillCastRange;
	protected int _forceId;
	protected int _forceLevel;
	protected L2Character _caster;
	protected L2Character _target;
	protected Future<?> _geoCheckTask;

	public L2Character getCaster()
	{
		return _caster;
	}

	public L2Character getTarget()
	{
		return _target;
	}

	public ForceBuff(L2Character caster, L2Character target, L2Skill skill)
	{
		_skillCastRange = skill.getCastRange();
		_caster = caster;
		_target = target;
		_forceId = skill.getTriggeredId();
		_forceLevel = skill.getTriggeredLevel();

		L2Effect effect = _target.getFirstEffect(_forceId);
		if (effect != null)
		{
			((EffectForce)effect).increaseForce();
		}
		else
		{
			L2Skill force = SkillTable.getInstance().getInfo(_forceId, _forceLevel);
			if (force != null)
				force.getEffects(_caster, _target);
			else
				_log.warn("Triggered skill ["+_forceId+";"+_forceLevel+"] not found!");
		}
		_geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}

	public void onCastAbort()
	{
		_caster.setForceBuff(null);
		L2Effect effect = _target.getFirstEffect(_forceId);
		if (effect != null)
			((EffectForce)effect).decreaseForce();

		_geoCheckTask.cancel(true);
	}

	public class GeoCheckTask implements Runnable
	{
		public void run()
		{
			try
			{
				if (!Util.checkIfInRange(_skillCastRange, _caster, _target, true))
					_caster.abortCast();

				if (!GeoData.getInstance().canSeeTarget(_caster, _target))
					_caster.abortCast();
			}
			catch (Exception e)
			{
				// ignore
			}
		}
	}
}
