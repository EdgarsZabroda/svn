package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class InquisitorBishop extends L2DefaultTransformation
{
	public InquisitorBishop()
	{
		// id, colRadius, colHeight
		super(316, 8.0, 22.0);
	}

	@Override
	public void onTransform(L2PcInstance player)
	{
		//if (player.getTransformationId() != getId() || player.isCursedWeaponEquipped())
		//	return;
		
		// Update transformation ID into database and player instance variables.
		player.transformInsertInfo();

		// Switch Stance
		addSkill(player, 838, 1);
		// Decrease Bow/Crossbow Attack Speed
		addSkill(player, 5491, 1);		
		
		// give transformation skills
		transformedSkills(player);
	}
	
	@Override
	public void onUntransform(L2PcInstance player)
	{
		// Switch Stance
		removeSkill(player, 838);
		// Decrease Bow/Crossbow Attack Speed
		removeSkill(player, 5491);
		
		// remove transformation skills
		removeSkills(player);
	}	

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 1523, level); // Divine Punishment
			addSkill(player, 1524, level); // Surrender to the Holy
			addSkill(player, 1525, level); // Divine Curse
			addSkill(player, 1528, level); // Divine Flash
		}
		player.addTransformAllowedSkill(new int[]{1430,1043,1042,1400,1418});
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 1523); // Divine Punishment
		removeSkill(player, 1524); // Surrender to the Holy
		removeSkill(player, 1525); // Divine Curse
		removeSkill(player, 1528); // Divine Flash
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InquisitorBishop());
	}
}