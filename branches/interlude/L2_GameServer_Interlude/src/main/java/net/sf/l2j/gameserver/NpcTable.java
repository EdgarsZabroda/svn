/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.FactionManager;
import net.sf.l2j.gameserver.model.L2DropCategory;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Faction;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
public class NpcTable
{
    private final static Log _log = LogFactory.getLog(NpcTable.class.getName());

    private static NpcTable _instance;

    private FastMap<Integer, L2NpcTemplate> _npcs;
    private boolean _initialized = false;

    public static NpcTable getInstance()
    {
        if (_instance == null)
            _instance = new NpcTable();

        return _instance;
    }

    private NpcTable()
    {
        _npcs = new FastMap<Integer, L2NpcTemplate>();
        
        restoreNpcData();
    }


    private void restoreNpcData()
    {
        java.sql.Connection con = null;
        
        try
        {
            try 
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement;
                statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "class", "collision_radius", "collision_height", "level", "sex", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level"}) + " FROM npc");
                ResultSet npcdata = statement.executeQuery();
                
                fillNpcTable(npcdata);
                npcdata.close();
                statement.close();
            } 
            catch (Exception e) {
                _log.fatal("NPCTable: Error creating NPC table: " + e);
            }

            try 
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement;
                statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "class", "collision_radius", "collision_height", "level", "sex", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level"}) + " FROM custom_npc");
                ResultSet npcdata = statement.executeQuery();
                
                fillNpcTable(npcdata);
                npcdata.close();
                statement.close();
            } 
            catch (Exception e) {
                _log.fatal("NPCTable: Error creating custom NPC table: " + e);
            }
            
            try 
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
                ResultSet npcskills = statement.executeQuery();
                L2NpcTemplate npcDat = null;
                L2Skill npcSkill = null;
    
                while (npcskills.next())
                {
                    int mobId = npcskills.getInt("npcid");
                    npcDat = _npcs.get(mobId);
                    
                    if (npcDat == null)
                        continue;
                    
                    int skillId = npcskills.getInt("skillid");
                    int level = npcskills.getInt("level");
                    
                    if (npcDat.race == 0)
                    {
                        if (skillId >= 4290 && skillId <= 4302)
                        {
                            npcDat.setRace(skillId);
                            continue;
                        }
                    }
                    
                    if (npcDat.rateHp == 1)
                    {
                        if (skillId >= 4303 && skillId <= 4310)
                        {
                            if (!Config.ENABLE_RATE_HP)
                                continue;
                            
                            npcDat.setRateHp(skillId - 4301);
                            continue;
                        }
                        else if (skillId == 4311)
                        {
                            if (!Config.ENABLE_RATE_HP)
                                continue;
                            
                            npcDat.setRateHp(0.5);
                            continue;
                        }
                    }
                
                    switch (skillId)
                    {
                        case 4084:
                            int resistPAtk = npcDat.getResist(Stats.POWER_DEFENCE) - 20;
                            
                            if (resistPAtk < 0) 
                                resistPAtk = 0;
                            
                            npcDat.removeResist(Stats.POWER_DEFENCE);
                            npcDat.addResist(Stats.POWER_DEFENCE, resistPAtk);
                            break;
                        case 4272:
                        case 4276:
                            int resistBow = npcDat.getResist(Stats.BOW_WPN_RES);
                                
                            if (skillId == 4272)
                                resistBow -= 70;
                            else if (skillId == 4276)
                                resistBow += 20;
                            
                            if (resistBow < 0) 
                                resistBow = 0;
                            
                            npcDat.removeResist(Stats.BOW_WPN_RES);
                            npcDat.addResist(Stats.BOW_WPN_RES, resistBow);
                            break;
                        case 4273:
                            int resistDagger = npcDat.getResist(Stats.DAGGER_WPN_RES) - 80;
                        
                            if (resistDagger < 0) 
                                resistDagger = 0;
                            
                            npcDat.removeResist(Stats.DAGGER_WPN_RES);
                            npcDat.addResist(Stats.DAGGER_WPN_RES, resistDagger);
                            break;
                        case 4274:
                            int resistBlunt = npcDat.getResist(Stats.BLUNT_WPN_RES) + 20;

                            if (resistBlunt < 0) 
                                resistBlunt = 0;
                            
                            npcDat.removeResist(Stats.BLUNT_WPN_RES);
                            npcDat.addResist(Stats.BLUNT_WPN_RES, resistBlunt);
                            break;
                    }
                            
                    npcSkill = SkillTable.getInstance().getInfo(skillId, level);
                    
                    if (npcSkill == null)
                        continue;
                    
                    npcDat.addSkill(npcSkill);
                }
                
                npcskills.close();
                statement.close();
            } 
            catch (Exception e) {
                _log.fatal("NPCTable: Error reading NPC skills table: " + e);
            }
            
            try 
            {
                PreparedStatement statement2 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"mobId", "itemId", "min", "max", "category", "chance"}) + " FROM droplist ORDER BY mobId, chance DESC");
                ResultSet dropData = statement2.executeQuery();
                L2DropData dropDat = null;
                L2NpcTemplate npcDat = null;
                
                while (dropData.next())
                {
                    int mobId = dropData.getInt("mobId");
                    npcDat = _npcs.get(mobId);
                    if (npcDat == null)
                    {
                        _log.fatal("NPCTable: No npc correlating with id : " + mobId);
                        continue;
                    }
                    dropDat = new L2DropData();
                    
                    dropDat.setItemId(dropData.getInt("itemId"));
                    dropDat.setMinDrop(dropData.getInt("min"));
                    dropDat.setMaxDrop(dropData.getInt("max"));
                    dropDat.setChance(dropData.getInt("chance"));
                    
                    int category = dropData.getInt("category");
                    
                    npcDat.addDropData(dropDat, category);
                }

                dropData.close();
                statement2.close();
            } 
            catch (Exception e) {
                _log.fatal("NPCTable: Error reading NPC drop data: " + e);
            }

            try 
            {
                PreparedStatement statement3 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"npc_id", "class_id"}) + " FROM skill_learn");
                ResultSet learndata = statement3.executeQuery();
                
                while (learndata.next())
                {
                    int npcId = learndata.getInt("npc_id");
                    int classId = learndata.getInt("class_id");
                    L2NpcTemplate npc = getTemplate(npcId);
                    
                    if (npc == null)
                    {
                        _log.warn("NPCTable: Error getting NPC template ID " + npcId + " while trying to load skill trainer data.");
                        continue;
                    }
                    
                    npc.addTeachInfo(ClassId.values()[classId]);
                }
                
                learndata.close();
                statement3.close();
            } 
            catch (Exception e) {
                _log.fatal("NPCTable: Error reading NPC trainer data: " + e);
            }
            
            try 
            {
                PreparedStatement statement4 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"boss_id", "minion_id", "amount_min", "amount_max"}) + " FROM minions");
                ResultSet minionData = statement4.executeQuery();
                L2MinionData minionDat = null;
                L2NpcTemplate npcDat = null;
                int cnt = 0;
                
                while (minionData.next())
                {
                    int raidId = minionData.getInt("boss_id");
                    npcDat = _npcs.get(raidId);
                    minionDat = new L2MinionData();                 
                    minionDat.setMinionId(minionData.getInt("minion_id"));
                    minionDat.setAmountMin(minionData.getInt("amount_min"));
                    minionDat.setAmountMax(minionData.getInt("amount_max"));
                    npcDat.addRaidData(minionDat);
                    cnt++;
                }

                minionData.close();
                statement4.close();
                _log.info("NpcTable: Loaded " + cnt + " Minions.");
            } 
            catch (Exception e) {
                _log.fatal("Error loading minion data: " + e);
            }                
        } 
        finally {
            try { con.close(); } catch (Exception e) {}
        }
        
        _initialized = true;
    }

    private void fillNpcTable(ResultSet NpcData)
            throws Exception
    {
        while (NpcData.next())
        {
            StatsSet npcDat = new StatsSet(); 
            int id = NpcData.getInt("id");
            
            if (Config.ASSERT) 
                assert id < 1000000; 

            npcDat.set("npcId", id);
            npcDat.set("idTemplate",NpcData.getInt("idTemplate"));
            int level = NpcData.getInt("level");
            npcDat.set("level", level);
            npcDat.set("jClass", NpcData.getString("class"));

            npcDat.set("baseShldDef", 0);
            npcDat.set("baseShldRate", 0);
            npcDat.set("baseCritRate",  38);

            npcDat.set("name", NpcData.getString("name"));
            npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
            npcDat.set("title",NpcData.getString("title"));
            npcDat.set("serverSideTitle",NpcData.getBoolean("serverSideTitle"));
            npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
            npcDat.set("collision_height", NpcData.getDouble("collision_height"));
            npcDat.set("sex", NpcData.getString("sex"));
            npcDat.set("type", NpcData.getString("type"));
            npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
            npcDat.set("revardExp", NpcData.getInt("exp"));
            npcDat.set("revardSp", NpcData.getInt("sp"));
            npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
            npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
            npcDat.set("aggroRange", NpcData.getInt("aggro"));
            npcDat.set("rhand", NpcData.getInt("rhand"));
            npcDat.set("lhand", NpcData.getInt("lhand"));
            npcDat.set("armor", NpcData.getInt("armor"));
            npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
            npcDat.set("baseRunSpd", NpcData.getInt("runspd"));

            // constants, until we have stats in DB
            npcDat.set("baseSTR", NpcData.getInt("str"));
            npcDat.set("baseCON", NpcData.getInt("con"));
            npcDat.set("baseDEX", NpcData.getInt("dex"));
            npcDat.set("baseINT", NpcData.getInt("int"));
            npcDat.set("baseWIT", NpcData.getInt("wit"));
            npcDat.set("baseMEN", NpcData.getInt("men"));

            npcDat.set("baseHpMax", NpcData.getInt("hp"));
            npcDat.set("baseCpMax", 0);
            npcDat.set("baseMpMax", NpcData.getInt("mp"));
            npcDat.set("baseHpReg", NpcData.getFloat("hpreg")>0?NpcData.getFloat("hpreg"):1.5 + ((level-1)/10));
            npcDat.set("baseMpReg", NpcData.getFloat("mpreg")>0?NpcData.getFloat("mpreg"):0.9 + 0.3*((level-1)/10));
            npcDat.set("basePAtk", NpcData.getInt("patk"));
            npcDat.set("basePDef", NpcData.getInt("pdef"));
            npcDat.set("baseMAtk", NpcData.getInt("matk"));
            npcDat.set("baseMDef", NpcData.getInt("mdef"));

            npcDat.set("factionId", NpcData.getString("faction_id"));
            npcDat.set("factionRange", NpcData.getInt("faction_range"));
            
            npcDat.set("isUndead", NpcData.getString("isUndead"));
            
            npcDat.set("absorb_level", NpcData.getString("absorb_level"));

            if(Config.FACTION_ENABLED)
            {
                Faction faction;
                for (int i = 0; i < FactionManager.getInstance().getFactions().size(); i++)
                {
                    faction = FactionManager.getInstance().getFactions().get(i);
                    if(faction.getNpcList().contains(id))
                    {
                        npcDat.set("NPCFaction", faction.getId());
                        npcDat.set("NPCFactionName", faction.getName());
                    }
                }
            }

            L2NpcTemplate template = new L2NpcTemplate(npcDat);
            template.addResist(Stats.POWER_DEFENCE,100);
            template.addResist(Stats.BOW_WPN_RES,100);
            template.addResist(Stats.BLUNT_WPN_RES,100);
            template.addResist(Stats.DAGGER_WPN_RES,100);
            
            _npcs.put(id, template);
        }
        
        _log.info("NpcTable: Loaded " + _npcs.size() + " Npc Templates.");
    }

    public void reloadNpc(int id)
    {
        java.sql.Connection con = null;
        
        try
        {
            // save a copy of the old data
            L2NpcTemplate old = getTemplate(id);
            FastMap<Integer,L2Skill> skills = new FastMap<Integer,L2Skill>();
            
            if (old.getSkills() != null)
                skills.putAll(old.getSkills());
            
            FastList<L2DropCategory> categories = new FastList<L2DropCategory>();
            
            if (old.getDropData() != null)
                categories.addAll(old.getDropData());
            
            FastList<ClassId> classIds = new FastList<ClassId>();
            
            if (old.getTeachInfo() != null)
                classIds.addAll(old.getTeachInfo());
            
            FastList<L2MinionData> minions = new FastList<L2MinionData>();
            
            if (old.getMinionData() != null)
                minions.addAll(old.getMinionData());

            // reload the NPC base data
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "class", "collision_radius", "collision_height", "level", "sex", "type", "attackrange", "hp", "mp", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level"}) + " FROM npc WHERE id=?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            fillNpcTable(rs);
            rs.close();
            st.close();

            // restore additional data from saved copy
            L2NpcTemplate created = getTemplate(id);
            
            for (L2Skill skill : skills.values())
                created.addSkill(skill);
            
            if (classIds != null)
                for (ClassId classId : classIds)
                    created.addTeachInfo(classId);
            
            for (L2MinionData minion : minions)
                created.addRaidData(minion);
        }
        catch (Exception e)
        {
            _log.warn("NPCTable: Could not reload data for NPC " + id + ": " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    // just wrapper
    public void reloadAllNpc()
    {
        restoreNpcData();
    }
    
    public void saveNpc(StatsSet npc)
    {
        java.sql.Connection con = null;
        String query = "";
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            FastMap<String, Object> set = npc.getSet();

            String name = "";
            String values = "";
            
            for (Object obj : set.keySet())
            {
                name = (String)obj;
                
                if (!name.equalsIgnoreCase("npcId"))
                {
                    if (values != "")
                        values += ", ";

                    values += name + " = '" + set.get(name) + "'";
                }
            }
            
            query = "UPDATE npc SET " + values + " WHERE id = ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, npc.getInteger("npcId"));
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("NPCTable: Could not store new NPC data in database: " + e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    public boolean isInitialized()
    {
        return _initialized;
    }
    
    public void replaceTemplate(L2NpcTemplate npc)
    {
        _npcs.put(npc.npcId, npc);
    }

    public L2NpcTemplate getTemplate(int id)
    {
        return _npcs.get(id);
    }
    
    public L2NpcTemplate getTemplateByName(String name)
    {
        for (L2NpcTemplate npcTemplate : _npcs.values())
            if (npcTemplate.name.equalsIgnoreCase(name))
                return npcTemplate;
       
        return null;
    }

    public L2NpcTemplate[] getAllOfLevel(int lvl)
    {
        FastList<L2NpcTemplate> list = new FastList<L2NpcTemplate>();
        
        for (L2NpcTemplate t : _npcs.values())
            if (t.level == lvl)
                list.add(t);

        return list.toArray(new L2NpcTemplate[list.size()]);
    }
    
    public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
    {
       FastList<L2NpcTemplate> list = new FastList<L2NpcTemplate>();
        
       for (L2NpcTemplate t : _npcs.values())
           if (t.level == lvl && "L2Monster".equals(t.type))
               list.add(t);

       return list.toArray(new L2NpcTemplate[list.size()]);
    }
    
    public L2NpcTemplate[] getAllNpcStartingWith(String letter)
    {
       FastList<L2NpcTemplate> list = new FastList<L2NpcTemplate>();
           
       for (L2NpcTemplate t : _npcs.values())
           if (t.name.startsWith(letter) && "L2Npc".equals(t.type))
               list.add(t);
   
       return list.toArray(new L2NpcTemplate[list.size()]);
    }    
}
