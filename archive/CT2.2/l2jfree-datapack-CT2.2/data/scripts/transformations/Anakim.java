package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Description: <br>
 * This will handle the transformation, giving the skills, and removing them, when the player logs out and is transformed these skills
 * do not save.
 * When the player logs back in, there will be a call from the enterworld packet that will add all their skills.
 * The enterworld packet will transform a player.
 *
 * @author Ahmed
 *
 */
public class Anakim extends L2DefaultTransformation
{
	public Anakim()
	{
		// id, colRadius, colHeight
		super(306, 15.5, 29.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 720, 2); // Anakim Holy Light Burst
		addSkill(player, 721, 2); // Anakim Energy Attack
		addSkill(player, 722, 2); // Anakim Holy Beam
		addSkill(player, 723, 1); // Anakim Sunshine
		addSkill(player, 724, 1); // Anakim Clans
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 720); // Anakim Holy Light Burst
		removeSkill(player, 721); // Anakim Energy Attack
		removeSkill(player, 722); // Anakim Holy Beam
		removeSkill(player, 723); // Anakim Sunshine
		removeSkill(player, 724); // Anakim Clans
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Anakim());
	}
}