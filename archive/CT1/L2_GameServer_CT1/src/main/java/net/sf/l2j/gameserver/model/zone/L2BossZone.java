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
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.AntharasManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.BaiumManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.BaylorManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.FrintezzaManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.SailrenManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.ValakasManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.VanHalterManager;
import net.sf.l2j.gameserver.model.L2Character;

public class L2BossZone extends L2DefaultZone
{
	@Override
	protected void register()
	{
		switch (_boss)
		{
		case ANTHARAS:
			AntharasManager.getInstance().registerZone(this);
			break;
		case BAIUM:
			BaiumManager.getInstance().registerZone(this);
			break;
		case BAYLOR:
			BaylorManager.getInstance().registerZone(this);
			break;
		case FRINTEZZA:
			FrintezzaManager.getInstance().registerZone(this);
			break;
		case FOURSEPULCHERS:
			FourSepulchersManager.getInstance().registerZone(this);
			break;
		case SAILREN:
			SailrenManager.getInstance().registerZone(this);
			break;
		case VALAKAS:
			ValakasManager.getInstance().registerZone(this);
			break;
		case VANHALTER:
			VanHalterManager.getInstance().registerZone(this);
			break;

		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_boss == Boss.SUNLIGHTROOM)
			character.setInsideZone(FLAG_SUNLIGHTROOM, true);

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (_boss == Boss.SUNLIGHTROOM)
			character.setInsideZone(FLAG_SUNLIGHTROOM, false);

		super.onExit(character);
	}
}
