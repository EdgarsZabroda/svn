package net.sf.l2j.gameserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.FishData;
import net.sf.l2j.gameserver.model.FishDropData;

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

    private static FastList<FishData> _Fishs;
    private static FastList<FishDropData> _FishRewards;

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
            con = L2DatabaseFactory.getInstance().getConnection();
            _Fishs = new FastList<FishData>();
            FishData fish;
            PreparedStatement statement = con.prepareStatement("SELECT id, level, name, hp, speed, hpregen, type FROM fish ORDER BY id");
            ResultSet Fishes = statement.executeQuery();

                while (Fishes.next())
                {
                    int id = Fishes.getInt("id");
                    int lvl = Fishes.getInt("level");
                    String name = Fishes.getString("name");
                    int hp = Fishes.getInt("hp");                   
                    int hpreg = Fishes.getInt("hpregen");
                    int type = Fishes.getInt("type");
                    fish = new FishData(id, lvl, name, hp, hpreg, type);
                    _Fishs.add(fish);
                }
                Fishes.close();
                statement.close();
                count = _Fishs.size();
        }
        catch (Exception e)
        {
            _log.fatal( "error while creating fishes table"+ e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        //Create Table that contains all fish rewards (drop of fish)
        _log.info("FishTable: Loaded " + count + " Fishes.");
        java.sql.Connection con2 = null;
        int count2 = 0;
        try
        {
            con2 = L2DatabaseFactory.getInstance().getConnection();
            _FishRewards = new FastList<FishDropData>();
            FishDropData fishreward;
            PreparedStatement statement = con2.prepareStatement("SELECT fishid, rewardid, count, minchance, maxchance FROM fishreward ORDER BY fishid");
            ResultSet FishReward = statement.executeQuery();

                while (FishReward.next())
                {
                    int fishid = FishReward.getInt("fishid");
                    int rewardid = FishReward.getInt("rewardid");
                    int drop = FishReward.getInt("count");
                    int minchance = FishReward.getInt("minchance");
                    int maxchance = FishReward.getInt("maxchance");
                    fishreward = new FishDropData(fishid, rewardid, drop, minchance, maxchance);
                    _FishRewards.add(fishreward);
                }
                FishReward.close();
                statement.close();
                count2 = _FishRewards.size();
        }
        catch (Exception e)
        {
            _log.fatal( "error while creating fish rewards table"+ e);
        }
        finally
        {
            try { con2.close(); } catch (Exception e) {}
        }
        _log.info("FishRewardsTable: Loaded " + count2 + " FishRewards.");

    }
    /**
     * @param Fish - lvl
     * @param Fish - type
     * @return List of Fish that can be fished
     */
    public FastList<FishData> getfish(int lvl, int type)
    {
        FastList<FishData> result = new FastList<FishData>();
        if (_Fishs == null)
        {
            // the fish list is empty
            _log.warn("Fish are not defined !");
            return null;
        }
        for (FishData f: _Fishs)
        {
            if (f.getLevel()!= lvl) continue;
            if (f.getType() != type) continue;

            result.add(f);
        }
        if (result.size() == 0) _log.warn("Cant Find Any Fish!? - Lvl: "+lvl+" Type: " +type);
        return result;
    }
    /**
     * @param fishid
     * @return List of all item that this fish can drop if open
     */
    public FastList<FishDropData> GetFishRreward(int fishid)
    {
        FastList<FishDropData> result = new FastList<FishDropData>();
        if (_FishRewards == null)
        {
            // the fish list is empty
            _log.warn("FishRewards are not defined !");
            return null;
        }
        for (FishDropData d: _FishRewards)
        {
            if (d.getFishId()!= fishid) continue;

            result.add(d);
        }
        if (result.size() == 0) _log.warn("Cant Find Any Fish Reward for ItemID: "+fishid);

        return result;
    }
    public int GetFishItemCount()
    {
        return _FishRewards.size();
    }
    public int getFishIdfromList(int i)
    {
        return _FishRewards.get(i).getFishId();
    }

}
