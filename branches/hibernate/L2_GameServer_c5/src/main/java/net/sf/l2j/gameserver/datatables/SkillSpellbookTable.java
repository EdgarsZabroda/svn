package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javolution.util.FastMap;
import net.sf.l2j.L2Registry;
import net.sf.l2j.gameserver.model.L2Skill;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SkillSpellbookTable
{
	private final static Log _log = LogFactory.getLog(SkillTreeTable.class.getName());
	private static SkillSpellbookTable _instance;

	private static FastMap<Integer, Integer> _skillSpellbooks;

	public static SkillSpellbookTable getInstance()
	{
        if (_instance == null)
            _instance = new SkillSpellbookTable();
        
		return _instance;
	}
    
	private SkillSpellbookTable()
	{
		_skillSpellbooks = new FastMap<Integer, Integer>();
		java.sql.Connection con = null;
        
		try
		{
			con = L2Registry.getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT skill_id, item_id FROM skill_spellbooks");
			ResultSet spbooks = statement.executeQuery();

			while (spbooks.next())
				_skillSpellbooks.put(spbooks.getInt("skill_id") , spbooks.getInt("item_id"));

			spbooks.close();
			statement.close();
            
			_log.info("SkillSpellbookTable: Loaded " + _skillSpellbooks.size() + " Spellbooks.");		
		}
		catch (Exception e)
		{
			_log.warn("Error while loading spellbook data: " +  e);
		}
		finally
		{
			try	
            {
				con.close(); 
			} 
            catch (Exception e) {}
		}
	}

    public int getBookForSkill(int skillId)
    {
        if (!_skillSpellbooks.containsKey(skillId))
            return -1;
        
        return _skillSpellbooks.get(skillId);
    }
    
    public int getBookForSkill(L2Skill skill)
    {
        return getBookForSkill(skill.getId());
    }
}
