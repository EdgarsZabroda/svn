package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.L2Registry;
import net.sf.l2j.gameserver.model.FishData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author -Nemesiss-
 *
 */
public class FishTable
{
    private final static Log _log = LogFactory.getLog(SkillTreeTable.class.getName());
    private static final FishTable _instance = new FishTable();

    private static List<FishData> _Fishs_Normal;
    private static List<FishData> _Fishs_Easy;
    private static List<FishData> _Fishs_Hard;

    public static FishTable getInstance()
    {
        return _instance;
    }
    private FishTable()
    {
        //Create table that contains all fish datas
        int count   = 0;
        java.sql.Connection con = null;
        try
        {
            con = L2Registry.getConnection(con);
            _Fishs_Easy = new FastList<FishData>();
            _Fishs_Normal = new FastList<FishData>();
            _Fishs_Hard = new FastList<FishData>();
            FishData fish;
            PreparedStatement statement = con.prepareStatement("SELECT id, level, name, hp, hpregen, fish_type, fish_group, fish_guts, guts_check_time, wait_time, combat_time FROM fish ORDER BY id");
            ResultSet Fishes = statement.executeQuery();

                while (Fishes.next())
                {
                    int id = Fishes.getInt("id");
                    int lvl = Fishes.getInt("level");
                    String name = Fishes.getString("name");
                    int hp = Fishes.getInt("hp");                   
                    int hpreg = Fishes.getInt("hpregen");
                    int type = Fishes.getInt("fish_type");
                    int group = Fishes.getInt("fish_group");
                    int fish_guts = Fishes.getInt("fish_guts");
                    int guts_check_time = Fishes.getInt("guts_check_time");
                    int wait_time = Fishes.getInt("wait_time");
                    int combat_time = Fishes.getInt("combat_time");
                    fish = new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
                    switch (fish.getGroup()) {
                        case 0:
                            _Fishs_Easy.add(fish);
                            break;
                        case 1:
                            _Fishs_Normal.add(fish);
                            break;
                        case 2:
                            _Fishs_Hard.add(fish);
                    }
                }
                Fishes.close();
                statement.close();
                count = _Fishs_Easy.size() + _Fishs_Normal.size() + _Fishs_Hard.size();
        }
        catch (Exception e)
        {
            _log.fatal("error while creating fishes table"+ e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        _log.info("FishTable: Loaded " + count + " Fishes.");
    }
    /**
     * @param Fish - lvl
     * @param Fish - type
     * @param Fish - group
     * @return List of Fish that can be fished
     */
    public List<FishData> getfish(int lvl, int type, int group)
    {
        List<FishData> result = new FastList<FishData>();
        List<FishData> _Fishs = null;
        switch (group) {
            case 0:
                _Fishs = _Fishs_Easy;
                break;
            case 1:
                _Fishs = _Fishs_Normal;
                break;
            case 2:
                _Fishs = _Fishs_Hard;
        }
        if (_Fishs == null)
        {
            // the fish list is empty
            _log.warn("Fish are not defined !");
            return null;
        }
        for (FishData f : _Fishs)
        {
            if (f.getLevel()!= lvl) continue;
            if (f.getType() != type) continue;

            result.add(f);
        }
        if (result.size() == 0) _log.warn("Cant Find Any Fish!? - Lvl: "+lvl+" Type: " +type);
        return result;
    }

}
