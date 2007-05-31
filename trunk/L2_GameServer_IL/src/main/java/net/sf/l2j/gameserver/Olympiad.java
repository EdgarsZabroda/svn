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

/**
 * @author godson
 */

package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.OlympiadStadiaManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.serverpackets.ExOlympiadUserInfo;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Olympiad
{
    protected static Log _log = LogFactory.getLog(Olympiad.class.getName());
    
    private static Olympiad _instance;
    
    protected static FastMap<Integer, StatsSet> _nobles;
    protected static FastList<StatsSet> _heroesToBe;
    protected static FastList<L2PcInstance> _nonClassBasedRegisters;
    protected static FastMap<Integer, FastList<L2PcInstance>> _classBasedRegisters;
    
    private static final String OLYMPIAD_DATA_FILE = "config/olympiad.properties";
    public static final String OLYMPIAD_HTML_FILE = "data/html/olympiad/";
    private static final String OLYMPIAD_LOAD_NOBLES = "SELECT * from olympiad_nobles";
    private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles " +
            "values (?,?,?,?,?)";
    private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles set " +
            "olympiad_points = ?, competitions_done = ? where char_id = ?";
    private static final String OLYMPIAD_GET_HEROS = "SELECT char_id, char_name from " +
            "olympiad_nobles where class_id = ? and competitions_done >= 9 order by " +
            "olympiad_points desc, competitions_done desc";
    private static final String GET_EACH_CLASS_LEADER = "SELECT char_name from " +
            "olympiad_nobles where class_id = ? order by olympiad_points desc, " +
            "competitions_done desc";
    private static final String OLYMPIAD_DELETE_ALL = "DELETE from olympiad_nobles";
    
    private static final int COMP_START = Config.ALT_OLY_START_TIME; // 8PM - 12AM
    private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
    private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 3hours 55mins :P
    protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 3mins
    protected static final long BATTLE_WAIT = Config.ALT_OLY_BWAIT; // 10mins
    protected static final long INITIAL_WAIT = Config.ALT_OLY_IWAIT;  // 5mins
    protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD; // 1 week
    protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD; // 24 hours
    
    /* FOR TESTING
    private static final int COMP_START = 8; // 1PM - 2PM
    private static final int COMP_MIN = 15; // 20mins
    private static final long COMP_PERIOD = 7200000; // 2hours
    private static final long BATTLE_PERIOD = 180000; // 3mins
    private static final long BATTLE_WAIT = 600000; // 10mins
    private static final long INITIAL_WAIT = 300000;  // 5mins
    private static final long WEEKLY_PERIOD = 7200000; // 2 hours
    private static final long VALIDATION_PERIOD = 3600000; // 1 hour */
    
    private static final int COLLISIEUMS = 11;  // 22 in all, 11 each for (Classed and NonClassed)
    
    private static final int DEFAULT_POINTS = 18;
    protected static final int WEEKLY_POINTS = 3;
    
    public static final String CHAR_ID = "char_id";
    public static final String CLASS_ID = "class_id";
    public static final String CHAR_NAME = "char_name";
    public static final String POINTS = "olympiad_points";
    public static final String COMP_DONE = "competitions_done";
    
    protected long _olympiadEnd;
    protected long _validationEnd;
    protected int _period;
    protected long _nextWeeklyChange;
    protected int _currentCycle;
    private long _compEnd;
    private Calendar _compStart;
    protected static boolean _inCompPeriod;
    protected static boolean _isOlympiadEnd;
    protected static boolean _compStarted;
    protected static boolean _battleStarted;
    protected ScheduledFuture _scheduledCompStart;
    protected ScheduledFuture _scheduledCompEnd;
    protected ScheduledFuture _scheduledOlympiadEnd;
    protected ScheduledFuture _scheduledManagerTask;
    protected ScheduledFuture _scheduledWeeklyTask;
    protected ScheduledFuture _scheduledValdationTask;
    
    protected static final int[][] _stadiums = 
    {
     {-20814, -21189, -3030},
     {-120324, -225077, -3331},
     {-102495, -209023, -3331},
     {-120156, -207378, -3331},
     {-87628, -225021, -3331},
     {-81705, -213209, -3331},
     {-87593, -207339, -3331},
     {-93709, -218304, -3331},
     {-77157, -218608, -3331},
     {-69682, -209027, -3331},
     {-76887, -201256, -3331},
     {-109985, -218701, -3331},
     {-126367, -218228, -3331},
     {-109629, -201292, -3331},
     {-87523, -240169, -3331},
     {-81748, -245950, -3331},
     {-77123, -251473, -3331},
     {-69778, -241801, -3331},
     {-76754, -234014, -3331},
     {-93742, -251032, -3331},
     {-87466, -257752, -3331},
     {-114413, -213241, -3331}
    };
    
    protected static Random _rnd;
    
    private static enum COMP_TYPE
    {
        CLASSED,
        NON_CLASSED
    }
    
    protected static OlympiadManager _manager;
    
    public static Olympiad getInstance()
    {
        if (_instance == null)
            _instance = new Olympiad();
        return _instance; 
    }
    
    public Olympiad()
    {
        try
        {
            load();
        }
        catch(IOException e)
        {
            _log.error(e.getMessage(),e);
        }
        catch(SQLException s)
        {
            _log.error(s.getMessage(),s);
        }
        
        if (_period == 0) init();
    }
    
    private void load() throws IOException, SQLException
    {
        _nobles = new FastMap<Integer, StatsSet>();
        
        Properties OlympiadProperties = new Properties();
        InputStream is =  new FileInputStream(new File("./" + OLYMPIAD_DATA_FILE));  
        OlympiadProperties.load(is);
        is.close();
        
        _currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
        _period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
        _olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
        _validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValdationEnd", "0"));
        _nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));
        
        switch(_period)
        {
            case 0:
                if (_olympiadEnd == 0 || _olympiadEnd < Calendar.getInstance().getTimeInMillis())
                    setNewOlympiadEnd();
                else
                    _isOlympiadEnd = false;
                break;
            case 1:
                if (_validationEnd > Calendar.getInstance().getTimeInMillis())
                {
                    _isOlympiadEnd = true;
                    
                    _scheduledValdationTask  = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
                        public void run()
                        {
                            _period = 0;
                            _currentCycle++;
                            deleteNobles();
                            setNewOlympiadEnd();
                            init();
                        }
                    }, getMillisToValidationEnd());
                }
                else
                {
                    _currentCycle++;
                    _period = 0;
                    deleteNobles();
                    setNewOlympiadEnd();
                }
                break;
                default:
                    _log.warn("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
                return;
        }
        
        try
        {
            Connection con = null;
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
            ResultSet rset = statement.executeQuery();
            
            while(rset.next())
            {
                StatsSet statDat = new StatsSet();
                int charId = rset.getInt(CHAR_ID);
                statDat.set(CLASS_ID, rset.getInt(CLASS_ID));
                statDat.set(CHAR_NAME, rset.getString(CHAR_NAME));
                statDat.set(POINTS, rset.getInt(POINTS));
                statDat.set(COMP_DONE, rset.getInt(COMP_DONE));
                statDat.set("to_save", false);
                
                _nobles.put(charId, statDat);
            }
            
            rset.close();
            statement.close();
            con.close();
        }
        catch(Exception e)
        {
            _log.error(e.getMessage(),e);
        }
        
        synchronized(this)
        {
            _log.info("Olympiad System: Loading Olympiad System....");
            if (_period == 0)
                _log.info("Olympiad System: Currently in Olympiad Period");
            else
                _log.info("Olympiad System: Currently in Validation Period");
            
            _log.info("Olympiad System: Period Ends....");
            
            long milliToEnd;
            if (_period == 0)
                milliToEnd = getMillisToOlympiadEnd();
            else
                milliToEnd = getMillisToValidationEnd();

            double numSecs = (milliToEnd / 1000) % 60;
            double countDown = ((milliToEnd / 1000) - numSecs) / 60;
            int numMins = (int) Math.floor(countDown % 60);
            countDown = (countDown - numMins) / 60;
            int numHours = (int) Math.floor(countDown % 24);
            int numDays = (int) Math.floor((countDown - numHours) / 24);

            _log.info("Olympiad System: In " + numDays + " days, " + numHours
                + " hours and " + numMins + " mins.");
            
            if (_period == 0)
            {
                _log.info("Olympiad System: Next Weekly Change is in....");
                
                milliToEnd = getMillisToWeekChange();
                
                double numSecs2 = (milliToEnd / 1000) % 60;
                double countDown2 = ((milliToEnd / 1000) - numSecs2) / 60;
                int numMins2 = (int) Math.floor(countDown2 % 60);
                countDown2 = (countDown2 - numMins2) / 60;
                int numHours2 = (int) Math.floor(countDown2 % 24);
                int numDays2 = (int) Math.floor((countDown2 - numHours2) / 24);

                _log.info("Olympiad System: " + numDays2 + " days, " + numHours2
                    + " hours and " + numMins2 + " mins.");
            }
        }
        
        _log.info("Olympiad System: Loaded " + _nobles.size() + " Nobles");
        
    }
    
    protected void init()
    {
        if (_period == 1)
            return;
        _nonClassBasedRegisters = new FastList<L2PcInstance>();
        _classBasedRegisters = new FastMap<Integer, FastList<L2PcInstance>>();
        
        _compStart = Calendar.getInstance();
        _compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
        _compStart.set(Calendar.MINUTE, COMP_MIN);
        _compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
        
        _scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
            public void run()
            {
                SystemMessage sm = new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_ENDED);
                sm.addNumber(_currentCycle);
                
                Announcements.getInstance().announceToAll(sm);
                Announcements.getInstance().announceToAll("Olympiad Validation Period has began");
                
                _isOlympiadEnd = true;
                if (_scheduledManagerTask != null)
                    _scheduledManagerTask.cancel(true);
                if (_scheduledWeeklyTask != null)
                    _scheduledWeeklyTask.cancel(true);
                
                Calendar validationEnd = Calendar.getInstance();
                _validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;
                
                saveNobleData();
                
                _period = 1;
                
                sortHerosToBe();
                
                giveHeroBonus();
                
                Hero.getInstance().computeNewHeroes(_heroesToBe);
                
                try {
                    save();
                }
                catch (Exception e) {
                    _log.warn("Olympiad System: Failed to save Olympiad configuration: " + e);
                }
                
                _scheduledValdationTask  = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
                    public void run()
                    {
                        Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
                        _period = 0;
                        _currentCycle++;
                        deleteNobles();
                        setNewOlympiadEnd();
                        init();
                    }
                }, getMillisToValidationEnd());
            }
        }, getMillisToOlympiadEnd());
        
        updateCompStatus();
        scheduleWeeklyChange();
    }
    
    public boolean registerNoble(L2PcInstance noble, boolean classBased)
    {
        SystemMessage sm;
        
        if (_compStarted)
        {
            noble.sendMessage("Cant Register whilst competition is under way");
            return false;
        }
        
        if (!_inCompPeriod)
        {
            sm = new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
            noble.sendPacket(sm);
            return false;
        }
        
        if (noble.isCursedWeaponEquiped())
        {
           noble.sendMessage("You can't participate to Olympiad while holding a cursed weapon.");
           return false;
        }
        
        if (!noble.isNoble())
        {
            sm = new SystemMessage(SystemMessage.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
            noble.sendPacket(sm);
            return false;
        }
        
        if (noble.getBaseClass() != noble.getClassId().getId())
        {
            sm = new SystemMessage(SystemMessage.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
            noble.sendPacket(sm);
            return false;
        }
        
        if (!_nobles.containsKey(noble.getObjectId()))
        {
            StatsSet statDat = new StatsSet();
            statDat.set(CLASS_ID, noble.getClassId().getId());
            statDat.set(CHAR_NAME, noble.getName());
            statDat.set(POINTS, DEFAULT_POINTS);
            statDat.set(COMP_DONE, 0);
            statDat.set("to_save", true);
            
            _nobles.put(noble.getObjectId(), statDat);
        }
        
        if (_classBasedRegisters.containsKey(noble.getClassId().getId()))
        {
            FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
            for (L2PcInstance partecipant: classed)
            {
            	if (partecipant.getObjectId()==noble.getObjectId())
            	{
            		sm = new SystemMessage(SystemMessage.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS);
            		noble.sendPacket(sm);
            		return false;
            	}
            }
        }
        
        for (L2PcInstance partecipant: _nonClassBasedRegisters)
        {
        	if (partecipant.getObjectId()==noble.getObjectId())
        	{
        		sm = new SystemMessage(SystemMessage.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME);
        		noble.sendPacket(sm);
        		return false;
        	}
        }
        
        if (getNoblePoints(noble.getObjectId()) < 3)
        {
            noble.sendMessage("Cant register when you have less than 3 points");
            return false;
        }
        
        if (classBased)
        {
            if (_classBasedRegisters.containsKey(noble.getClassId().getId()))
            {
                FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
                classed.add(noble);
                
                _classBasedRegisters.remove(noble.getClassId().getId());
                _classBasedRegisters.put(noble.getClassId().getId(), classed);
                
                sm = new SystemMessage(SystemMessage.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
                noble.sendPacket(sm);
            }
            else
            {
                FastList<L2PcInstance> classed = new FastList<L2PcInstance>();
                classed.add(noble);
                
                _classBasedRegisters.put(noble.getClassId().getId(), classed);
                
                sm = new SystemMessage(SystemMessage.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
                noble.sendPacket(sm);
                
            }
        }
        else
        {
            _nonClassBasedRegisters.add(noble);
            sm = new SystemMessage(SystemMessage.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
            noble.sendPacket(sm);
        }
        
        return true;
    }
    
    public boolean unRegisterNoble(L2PcInstance noble)
    {
        SystemMessage sm;
        
        if (_compStarted)
        {
            noble.sendMessage("Cant Unregister whilst competition is under way");
            return false;
        }
        
        if (!_inCompPeriod)
        {
            sm = new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
            noble.sendPacket(sm);
            return false;
        }
        
        if (!noble.isNoble())
        {
            sm = new SystemMessage(SystemMessage.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
            noble.sendPacket(sm);
            return false;
        }
        
        /*if (noble.getBaseClass() != noble.getClassId().getId())
        {
            noble.sendMessage("Only your main can unregister");
            return false;
        }*/
        /*
        if (!_nonClassBasedRegisters.contains(noble))
        {
            sm = new SystemMessage(SystemMessage.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
            noble.sendPacket(sm);
            return false;
        }
        */
        
        if (!_classBasedRegisters.containsKey(noble.getClassId().getId()) && !_nonClassBasedRegisters.contains(noble))
        {
            sm = new SystemMessage(SystemMessage.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
            noble.sendPacket(sm);
            return false;
        }
        else if (_classBasedRegisters.containsKey(noble.getClassId().getId()))
        {
            FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
            if (!classed.contains(noble))
            {
                sm = new SystemMessage(SystemMessage.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
                noble.sendPacket(sm);
                return false;
            }
        }
        
        if (_nonClassBasedRegisters.contains(noble))
            _nonClassBasedRegisters.remove(noble);
        else
        {
            FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
            classed.remove(noble);
            
            _classBasedRegisters.remove(noble.getClassId().getId());
            _classBasedRegisters.put(noble.getClassId().getId(), classed);
        }
        
        sm = new SystemMessage(SystemMessage.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
        noble.sendPacket(sm);
        
        return true;
    }
    
    private void updateCompStatus()
    {
        _compStarted = false;
        
        synchronized(this)
        {
            long milliToStart = getMillisToCompBegin();
            
            double numSecs = (milliToStart / 1000) % 60;
            double countDown = ((milliToStart / 1000) - numSecs) / 60;
            int numMins = (int) Math.floor(countDown % 60);
            countDown = (countDown - numMins) / 60;
            int numHours = (int) Math.floor(countDown % 24);
            int numDays = (int) Math.floor((countDown - numHours) / 24);

            _log.info("Olympiad System: Competition Period Starts in " 
                      + numDays + " days, " + numHours
                + " hours and " + numMins + " mins.");
            
            _log.info("Olympiad System: Event starts/started : " + _compStart.getTime());
        }
        
        _scheduledCompStart = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
            public void run()
            {
                if (isOlympiadEnd())
                    return;
                
                _inCompPeriod = true;
                OlympiadManager om = new OlympiadManager();
                
                Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_STARTED));
                _log.info("Olympiad System: Olympiad Game Started");
                
                _scheduledManagerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(om, INITIAL_WAIT, BATTLE_WAIT);
                _scheduledCompEnd = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
                    public void run()
                    {
                        if (isOlympiadEnd())
                            return;
                        _scheduledManagerTask.cancel(true);
                        _inCompPeriod = false;
                        Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_ENDED));
                        _log.info("Olympiad System: Olympiad Game Ended");
                        
                        try {
                            save();
                        }
                        catch (Exception e) {
                            _log.warn("Olympiad System: Failed to save Olympiad configuration: " + e);
                        }
                        
                        init();
                    }
                    }, getMillisToCompEnd());
            }
            }, getMillisToCompBegin());
    }
    
    private long getMillisToOlympiadEnd()
    {
        //if (_olympiadEnd > Calendar.getInstance().getTimeInMillis())
            return (_olympiadEnd - Calendar.getInstance().getTimeInMillis());
        //return 10L;
    }
    
    public void manualSelectHeroes()
    {
        SystemMessage sm = new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_ENDED);
        sm.addNumber(_currentCycle);
        
        Announcements.getInstance().announceToAll(sm);
        Announcements.getInstance().announceToAll("Olympiad Validation Period has began");
        
        _isOlympiadEnd = true;
        if (_scheduledManagerTask != null)
            _scheduledManagerTask.cancel(true);
        if (_scheduledWeeklyTask != null)
            _scheduledWeeklyTask.cancel(true);
        if(_scheduledOlympiadEnd != null)
            _scheduledOlympiadEnd.cancel(true);
        
        Calendar validationEnd = Calendar.getInstance();
        _validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;
        
        saveNobleData();
        
        _period = 1;
        
        sortHerosToBe();
        
        giveHeroBonus();
        
        Hero.getInstance().computeNewHeroes(_heroesToBe);
        
        try {
            save();
        }
        catch (Exception e) {
            _log.warn("Olympiad System: Failed to save Olympiad configuration: " + e);
        }
        
        _scheduledValdationTask  = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
            public void run()
            {
                Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
                _period = 0;
                _currentCycle++;
                deleteNobles();
                setNewOlympiadEnd();
                init();
            }
        }, getMillisToValidationEnd());
    }
    
    protected long getMillisToValidationEnd()
    {
        if (_validationEnd > Calendar.getInstance().getTimeInMillis())
            return (_validationEnd - Calendar.getInstance().getTimeInMillis());
        return 10L;
    }
    
    public boolean isOlympiadEnd()
    {
        return _isOlympiadEnd;
    }
    
    protected void setNewOlympiadEnd()
    {
        SystemMessage sm = new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_STARTED);
        sm.addNumber(_currentCycle);
        
        Announcements.getInstance().announceToAll(sm);
        
        Calendar currentTime = Calendar.getInstance();
        currentTime.add(Calendar.MONTH, 1);
        _olympiadEnd = currentTime.getTimeInMillis();
        
        Calendar nextChange = Calendar.getInstance();
        _nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
        
        _isOlympiadEnd = false;
    }
    
    public boolean inCompPeriod()
    {
        return _inCompPeriod;
    }
    
    private long getMillisToCompBegin()
    {
        if (_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() &&
                _compEnd > Calendar.getInstance().getTimeInMillis())
            return 10L;
        
        if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
            return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
        
        return setNewCompBegin();    
    }
    
    private long setNewCompBegin()
    {
        _compStart = Calendar.getInstance();
        _compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
        _compStart.set(Calendar.MINUTE, COMP_MIN);
        _compStart.add(Calendar.HOUR_OF_DAY, 24);
        _compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
        
        _log.info("Olympiad System: New Schedule @ " + _compStart.getTime());
        
        return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
    }
    
    protected long getMillisToCompEnd()
    {
        //if (_compEnd > Calendar.getInstance().getTimeInMillis())
            return (_compEnd - Calendar.getInstance().getTimeInMillis());
        //return 10L;
    }
    
    private long getMillisToWeekChange()
    {
        if (_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
            return (_nextWeeklyChange - Calendar.getInstance().getTimeInMillis());
        return 10L;
    }
    
    private void scheduleWeeklyChange()
    {
        _scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable() {
            public void run()
            {
                addWeeklyPoints();
                _log.info("Olympiad System: Added weekly points to nobles");
                
                Calendar nextChange = Calendar.getInstance();
                _nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
            }
        }, getMillisToWeekChange(), WEEKLY_PERIOD);
    }
    
    protected synchronized void addWeeklyPoints()
    {  
        if (_period == 1)
            return;
        
        for (Integer nobleId : _nobles.keySet())
        {
            StatsSet nobleInfo = _nobles.get(nobleId);
            int currentPoints = nobleInfo.getInteger(POINTS);
            currentPoints += WEEKLY_POINTS;
            nobleInfo.set(POINTS, currentPoints);
            
            _nobles.remove(nobleId);
            _nobles.put(nobleId, nobleInfo);
        }
    }
    
    public String[] getMatchList()
    {
        return (_manager == null)? null : _manager.getAllTitles();
    }
    
    public int getCurrentCycle()
    {
        return _currentCycle;
    }
    
    public void addSpectator(int id, L2PcInstance spectator)
    {
        if (_manager == null || (_manager.getOlympiadInstance(id) == null) || !_battleStarted)
        {
            spectator.sendPacket(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
            return;
        }
        
        L2PcInstance[] players = _manager.getOlympiadInstance(id).getPlayers();
        
        if (players == null) return;
        
        spectator.enterOlympiadObserverMode(_stadiums[id][0], _stadiums[id][1], _stadiums[id][2], id);
        
        _manager.getOlympiadInstance(id).addSpectator(spectator);
        
        spectator.sendPacket(new ExOlympiadUserInfo(players[0], 2));
        spectator.sendPacket(new ExOlympiadUserInfo(players[1], 1));
    }
    
    public void removeSpectator(int id, L2PcInstance spectator)
    {
        if (_manager == null || (_manager.getOlympiadInstance(id) == null)) return;
        
        _manager.getOlympiadInstance(id).removeSpectator(spectator);
    }
    
    public FastList<L2PcInstance> getSpectators(int id)
    {
        return _manager.getOlympiadInstance(id).getSpectators();
    }
    
    public FastMap<Integer, L2OlympiadGame> getOlympiadGames()
    {
        return _manager.getOlympiadGames();
    }
    
    public boolean playerInStadia(L2PcInstance player)
    {
        return OlympiadStadiaManager.getInstance().checkIfInZone(player);
    }
    
    public int[] getWaitingList()
    {
        int[] array = new int[2];
        
        if (!inCompPeriod())
            return null;
        
        int classCount = 0;
        
        if (_classBasedRegisters.size() != 0)
            for (FastList<L2PcInstance> classed : _classBasedRegisters.values())
            {
                classCount += classed.size();
            }
        
        array[0] = classCount;
        array[1] = _nonClassBasedRegisters.size();
        
        return array;
    }
    
    protected synchronized void saveNobleData()
    { 
        Connection con = null;
        
        if (_nobles == null)
            return;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            
            for (Integer nobleId : _nobles.keySet())
            {
                StatsSet nobleInfo = _nobles.get(nobleId);
                
                int charId = nobleId;
                int classId = nobleInfo.getInteger(CLASS_ID);
                String charName = nobleInfo.getString(CHAR_NAME);
                int points = nobleInfo.getInteger(POINTS);
                int compDone = nobleInfo.getInteger(COMP_DONE);
                boolean toSave = nobleInfo.getBool("to_save");
                
                if (toSave)
                {
                    statement = con.prepareStatement(OLYMPIAD_SAVE_NOBLES);
                    statement.setInt(1, charId);
                    statement.setInt(2, classId);
                    statement.setString(3, charName);
                    statement.setInt(4, points);
                    statement.setInt(5, compDone);
                    statement.execute();
                    
                    statement.close();
                    
                    nobleInfo.set("to_save", false);
                    
                    _nobles.remove(nobleId);
                    _nobles.put(nobleId, nobleInfo);
                }
                else
                {
                    statement = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES);
                    statement.setInt(1, points);
                    statement.setInt(2, compDone);
                    statement.setInt(3, charId);
                    statement.execute();
                    statement.close();        
                }
            }
        }
        catch(SQLException e) {_log.warn("Olympiad System: Couldnt save nobles info in db");}
        finally
        {
            try{con.close();}catch(Exception e){_log.error(e.getMessage(),e);}
        }
    }
    
    protected void sortHerosToBe()
    {
        if (_period != 1) return;
        
         _heroesToBe = new FastList<StatsSet>();
         
         Connection con = null;
         
         try
         {
             con = L2DatabaseFactory.getInstance().getConnection(con);
             PreparedStatement statement;
             ResultSet rset;
             StatsSet hero;
             
             for (int i = 88; i < 119; i++)
             {
                 statement = con.prepareStatement(OLYMPIAD_GET_HEROS);
                 statement.setInt(1, i);
                 rset = statement.executeQuery();
                 
                 if (rset.next())
                 {
                     hero = new StatsSet();
                     hero.set(CLASS_ID, i);
                     hero.set(CHAR_ID, rset.getInt(CHAR_ID));
                     hero.set(CHAR_NAME, rset.getString(CHAR_NAME));
                     
                     _heroesToBe.add(hero);
                 }
                 
                 statement.close();
                 rset.close();    
             }
         }
         catch(SQLException e){_log.warn("Olympiad System: Couldnt heros from db");}
         finally
         {
             try{con.close();}catch(Exception e){_log.error(e.getMessage(),e);}
         }
         
    }
    
    public FastList<String> getClassLeaderBoard(int classId)
    {
        //if (_period != 1) return;
        
         FastList<String> names = new FastList<String>();
         
         Connection con = null;
         
         try
         {
             con = L2DatabaseFactory.getInstance().getConnection(con);
             PreparedStatement statement;
             ResultSet rset;
             statement = con.prepareStatement(GET_EACH_CLASS_LEADER);
             statement.setInt(1, classId);
             rset = statement.executeQuery();
             
             while (rset.next())
             {
                 names.add(rset.getString(CHAR_NAME));
             }
             
             statement.close();
             rset.close();
             
             return names;
         }
         catch(SQLException e){_log.warn("Olympiad System: Couldnt heros from db");}
         finally
         {
             try{con.close();}catch(Exception e){_log.error(e.getMessage(),e);}
         }
         
         return names;
         
    }
    
    protected void giveHeroBonus()
    {
        if (_heroesToBe.size() == 0)
            return;
        
        for (StatsSet hero : _heroesToBe)
        {
            int charId = hero.getInteger(CHAR_ID);
            
            StatsSet noble = _nobles.get(charId);
            int currentPoints = noble.getInteger(POINTS);
            currentPoints += 300;
            noble.set(POINTS, currentPoints);
            
            _nobles.remove(charId);
            _nobles.put(charId, noble);
        }
    }
    
    public int getNoblessePasses(int objId)
    {
        if (_period != 1 || _nobles.size() == 0)
            return 0;
        
        StatsSet noble = _nobles.get(objId);
        if (noble == null)
            return 0;
        int points = noble.getInteger(POINTS);
        if (points <= 50)
            return 0;
        
        noble.set(POINTS, 0);
        _nobles.remove(objId);
        _nobles.put(objId, noble);
        
        points *= 1000;
        
        return points;
    }
    
    public boolean isRegisteredInComp(L2PcInstance player)
    {
        boolean result = false;
        
        if (_nonClassBasedRegisters != null && _nonClassBasedRegisters.contains(player))
            result = true;
        
        else if (_classBasedRegisters != null && _classBasedRegisters.containsKey(player.getClassId().getId()))
        {
            FastList<L2PcInstance> classed = _classBasedRegisters.get(player.getClassId().getId());
            if (classed.contains(player))
                result = true;
        }
        
        return result;
    }
    
    public int getNoblePoints(int objId)
    {
        if (_nobles.size() ==  0)
            return 0;
        
        StatsSet noble = _nobles.get(objId);
        if (noble == null)
            return 0;
        int points = noble.getInteger(POINTS);
        
        return points;
    }
    
    protected void deleteNobles()
    {
        Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement(OLYMPIAD_DELETE_ALL);
            statement.execute();
            statement.close();
        }
        catch(SQLException e){_log.warn("Olympiad System: Couldnt delete nobles from db");}
        finally
        {
            try{con.close();}catch(Exception e){_log.error(e.getMessage(),e);}
        }
        
        _nobles.clear();
    }
    
    public void save() throws IOException
    {
        saveNobleData();
        
        Properties OlympiadProperties = new Properties();
        FileOutputStream fos = new FileOutputStream(new File("./", OLYMPIAD_DATA_FILE));
        
        OlympiadProperties.setProperty("CurrentCycle", String.valueOf(_currentCycle));
        OlympiadProperties.setProperty("Period", String.valueOf(_period));
        OlympiadProperties.setProperty("OlympiadEnd", String.valueOf(_olympiadEnd));
        OlympiadProperties.setProperty("ValdationEnd", String.valueOf(_validationEnd));
        OlympiadProperties.setProperty("NextWeeklyChange", String.valueOf(_nextWeeklyChange));
        
        OlympiadProperties.store(fos, "Olympiad Properties");
        fos.close();
    }
    
    private class OlympiadManager implements Runnable
    {
        private FastMap<Integer, L2OlympiadGame> _olympiadInstances;
        private FastMap<Integer, FastList<L2PcInstance>> _classBasedParticipants;
        private FastMap<Integer, FastList<L2PcInstance>> _nonClassBasedParticipants;
        
        public OlympiadManager()
        {
            _olympiadInstances = new FastMap<Integer, L2OlympiadGame>();
            _manager = this;
        }
        
        public synchronized void run()
        {
            if (isOlympiadEnd())
            {
                _scheduledManagerTask.cancel(true);
                return;
            }
            
            if (!inCompPeriod())
                return;
            
            //Announcements.getInstance().announceToAll("Comp Match Init");
            
            if (_nobles.size() == 0)
                return;
            
            _compStarted = true;
            
            try{
                sortClassBasedOpponents();
                _nonClassBasedParticipants = pickOpponents(_nonClassBasedRegisters);
            }catch(Exception e){_log.error(e.getMessage(),e);}
            
            int classIndex = 0;
            int nonClassIndex = 0;
            int index = 0;
            
            for (int i = 0; i < COLLISIEUMS; i++)
            {
                if (_classBasedParticipants.get(classIndex) != null)
                {
                    _olympiadInstances.put(index, new L2OlympiadGame(index, COMP_TYPE.CLASSED, _classBasedParticipants.get(classIndex), _stadiums[index]));
                    index++;
                    classIndex++;
                }
                if (_nonClassBasedParticipants.get(nonClassIndex) != null)
                {
                    _olympiadInstances.put(index, new L2OlympiadGame(index, COMP_TYPE.NON_CLASSED, _nonClassBasedParticipants.get(nonClassIndex), _stadiums[index]));
                    nonClassIndex++;
                    index++;
                }
            }
            
            if (_olympiadInstances.size() == 0)
            {
                _compStarted = false;
                return;
            }
            
            for (L2OlympiadGame instance : _olympiadInstances.values())
                instance.sendMessageToPlayers(false);
            
            //Wait 20 seconds
            try{
                wait(20000);
            }catch (InterruptedException e){}
            
            for (L2OlympiadGame instance : _olympiadInstances.values())
                instance.portPlayersToArena();
            
            //Wait 20 seconds
            try{
                wait(20000);
            }catch (InterruptedException e){}
            
            for (L2OlympiadGame instance : _olympiadInstances.values())  
            	instance.removals();
            
            _battleStarted = true;
            
            //Wait 2mins
            try{
                wait(120000);
            }catch (InterruptedException e){}
            
            for (L2OlympiadGame instance : _olympiadInstances.values())
            {
            	instance.additions();
                instance.sendMessageToPlayers(true);
            }
            
            // Wait 20 seconds
            try{
                wait(20000);
            }catch (InterruptedException e){}
            
            for (L2OlympiadGame instance : _olympiadInstances.values())
            {
                instance.makePlayersVisible();
            }
                
            //Wait 3 mins (Battle)
            try{
                wait(BATTLE_PERIOD);
            }catch (InterruptedException e){}
            
            for (L2OlympiadGame instance : _olympiadInstances.values())
            {
                try{
                    instance.validateWinner();
                }catch(Exception e){_log.error(e.getMessage(),e);}
            }
            
            //Wait 20 seconds
            try{
                wait(20000);
            }catch (InterruptedException e){}
            
            for (L2OlympiadGame instance : _olympiadInstances.values())
            {
                instance.portPlayersBack();
                instance.clearSpectators();
            }
            
            //Wait 20 seconds
            try{
                wait(20000);
            }catch (InterruptedException e){}
            
            _classBasedParticipants.clear();
            _nonClassBasedParticipants.clear();
            
            _olympiadInstances.clear();
            _classBasedRegisters.clear();
            _nonClassBasedRegisters.clear();
            
            _battleStarted = false;
            _compStarted = false;
                    
        }
        
        protected L2OlympiadGame getOlympiadInstance(int index)
        {
            if (_olympiadInstances != null || _compStarted)
            {
                return _olympiadInstances.get(index);
            }
            
            return null;
        }
        
        private void sortClassBasedOpponents()
        {
            FastMap<Integer, FastList<L2PcInstance>> result = new FastMap<Integer, FastList<L2PcInstance>>();  
            _classBasedParticipants = new FastMap<Integer, FastList<L2PcInstance>>();
            
            int count = 0;
            
            if (_classBasedRegisters.size() == 0) return;
            
            for (FastList<L2PcInstance> classed : _classBasedRegisters.values())
            {
                if (classed.size() == 0) continue;
                
                try{
                   result = pickOpponents(classed);
                }catch(Exception e){_log.error(e.getMessage(),e);}
                
                if (result.size() == 0)
                    continue;
                
                for (FastList<L2PcInstance> list : result.values())
                {
                    if (count == 10) break;
                    _classBasedParticipants.put(count, list);
                    count++;
                }
                
                if (count == 10) break;
            }
        }
        
        protected FastMap<Integer, L2OlympiadGame> getOlympiadGames()
        {
            return (_olympiadInstances == null)? null : _olympiadInstances;
        }
        
        private FastMap<Integer, FastList<L2PcInstance>> pickOpponents(FastList<L2PcInstance> list) throws Exception
        {
            _rnd = new Random();
            
            FastMap<Integer, FastList<L2PcInstance>> result = 
                new FastMap<Integer, FastList<L2PcInstance>>();
            
            if (list.size() == 0)
                return result;
            
            int loopCount = (list.size() / 2);
            
            int first;
            int second;
            
            if (loopCount < 1)
                return result;
            
            int count = 0;
            
            for (int i = 0; i < loopCount; i++)
            {
                count++;
                
                FastList<L2PcInstance> opponents = new FastList<L2PcInstance>();
                first = _rnd.nextInt(list.size());
                opponents.add(list.get(first));
                list.remove(first);
                
                second = _rnd.nextInt(list.size());
                opponents.add(list.get(second));
                list.remove(second);
                
                result.put(i, opponents);
                
                if (count == 11)
                    break;
            }
            
            return result;
        }
        
        protected String[] getAllTitles()
        {
            if(!_compStarted)
                return null;
            
            if(_olympiadInstances.size() == 0)
                return null;
            
            String[] msg = new String[_olympiadInstances.size()];
            int count = 0;
            int match = 1;
            
            for (L2OlympiadGame instance : _olympiadInstances.values())
            {
                msg[count] = match + "_In Progress_" + instance.getTitle();
                count++;
                match++;
            }
            
            return msg;
        }
    }
    
    private class L2OlympiadGame
    {
        protected COMP_TYPE _type;
        private FastList<L2PcInstance> _players;
        private L2PcInstance _playerOne;
        private L2PcInstance _playerTwo;
        private int[] _playerOneLocation;
        private int[] _playerTwoLocation;
        private int[] _stadiumPort;
        private FastList<L2PcInstance> _spectators;
        private SystemMessage _sm;
        
        protected L2OlympiadGame(int id, COMP_TYPE type, FastList<L2PcInstance> list, int[] stadiumPort)
        {
            _type = type;
            _players = list;
            _stadiumPort = stadiumPort;
            _spectators = new FastList<L2PcInstance>();
            
            if (_players != null)
            {
                _playerOne = _players.get(0);
                _playerTwo = _players.get(1);
                
                if (_playerOne == null || _playerTwo == null) return;
                
                _playerOne.setOlympiadGameId(id);
                _playerTwo.setOlympiadGameId(id);
                
                _log.info("Olympiad System: Game - " + id + ": " 
                          + _playerOne.getName() + " Vs " + _playerTwo.getName());
            }
            else return;
        }
        
        protected void removals()
        {
            if (_players == null)
                return;
            
            if (_playerOne == null || _playerTwo == null) return;
            
            for (L2PcInstance player : _players)
            {
                player.setIsInOlympiadMode(true);

                //Remove clan skill
                if (player.getClan() != null){ 
                    for(L2Skill skill: player.getClan().getAllSkills())
                    { 
                        player.removeSkill(skill,false); 
                    } 
                } 

                //Remove Buffs
                for (L2Effect e : player.getAllEffects())
                    e.exit();
                
                //Remove Summon's buffs
                if (player.getPet() != null)
                {
                    L2Summon summon = player.getPet();
                    for (L2Effect e : summon.getAllEffects())
                        e.exit();
                    
                    if (summon instanceof L2PetInstance)
                        summon.unSummon(player);
                }
                
                /*if (player.getCubics() != null)
                {
                    for(L2CubicInstance cubic : player.getCubics().values())
                    {
                        cubic.stopAction();
                        player.delCubic(cubic.getId());
                    }
                    
                    player.getCubics().clear();
                }*/
                
                //Remove player from his party
                if (player.getParty() != null)
                {
                    L2Party party = player.getParty();
                    party.removePartyMember(player);
                }
                
				//Remove Hero weapons 
			 	L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			 	if (wpn == null) wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND); 
			 	if (wpn != null &&  
			 			( 
			 					(wpn.getItemId() >= 6611 && wpn.getItemId() <= 6621) ||  
			 					wpn.getItemId() == 6842 
			 			) 
			 	)
			 	{ 
			 		L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart()); 
			 		InventoryUpdate iu = new InventoryUpdate(); 
			 		for (int i = 0; i < unequiped.length; i++) 
			 			iu.addModifiedItem(unequiped[i]); 
			 		player.sendPacket(iu); 
			 		
			 		player.abortAttack(); 
			 		player.refreshExpertisePenalty(); 
			 		player.broadcastUserInfo(); 
			 		
			 		// this can be 0 if the user pressed the right mousebutton twice very fast 
			 		if (unequiped.length > 0){ 
			 			if (unequiped[0].isWear()) 
			 				return; 
			 			SystemMessage sm = null; 
			 			if (unequiped[0].getEnchantLevel() > 0){ 
			 				sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED); 
			 				sm.addNumber(unequiped[0].getEnchantLevel()); 
			 				sm.addItemName(unequiped[0].getItemId()); 
			 			}else{ 
			 				sm = new SystemMessage(SystemMessage.S1_DISARMED); 
			 				sm.addItemName(unequiped[0].getItemId()); 
			 			} 
			 			player.sendPacket(sm); 
			 		} 
			 	}
			 	
			 	//remove bsps/sps/ss automation
			 	Map<Integer, Integer> activeSoulShots = player.getAutoSoulShot();
			 	for (int itemId : activeSoulShots.values()){
			 		player.removeAutoSoulShot(itemId);
			 	}
			 	
			 	// Set HP/CP/MP to Max
                player.getStatus().setCurrentCp(player.getMaxCp());
                player.getStatus().setCurrentHp(player.getMaxHp());
                player.getStatus().setCurrentMp(player.getMaxMp());
            }
            
            _sm = new SystemMessage(SystemMessage.THE_GAME_WILL_START_IN_S1_SECOND_S);
            _sm.addNumber(120);
            broadcastMessage(_sm, false);
        }
        
        protected void portPlayersToArena()
        {
            _playerOneLocation = new int[3];
    		_playerTwoLocation = new int[3];    
    		
    		if (_playerOne == null || _playerTwo == null){
        		StatsSet playerOneStat;
        		StatsSet playerTwoStat;
        		
        		playerOneStat = _nobles.get(_playerOne.getObjectId());
    			playerTwoStat = _nobles.get(_playerTwo.getObjectId());
    			
    			if (_playerOne == null){
    				int playerOnePoints = playerOneStat.getInteger(POINTS);
    				playerOneStat.set(POINTS, playerOnePoints - (playerOnePoints / 5));
    			}
    			if (_playerTwo == null){
    				int playerTwoPoints = playerTwoStat.getInteger(POINTS);
    				playerTwoStat.set(POINTS, playerTwoPoints - (playerTwoPoints / 5));
    			}
    			_playerOne=null;
    			_playerTwo=null;
     			return;
    		}
            
            if (_playerOne == null || _playerTwo == null)
                return;
            
            _playerOneLocation[0] = _playerOne.getX();
            _playerOneLocation[1] = _playerOne.getY();
            _playerOneLocation[2] = _playerOne.getZ();
            
            _playerTwoLocation[0] = _playerTwo.getX();
            _playerTwoLocation[1] = _playerTwo.getY();
            _playerTwoLocation[2] = _playerTwo.getZ();
            
    		_playerOne.getAppearance().setInvisible();
    		_playerTwo.getAppearance().setInvisible();
            
            if (_playerOne.isSitting())
                _playerOne.standUp();
            
            if (_playerTwo.isSitting())
                _playerTwo.standUp();
            
            _playerOne.setTarget(null);
            _playerTwo.setTarget(null);
            
            _playerOne.setIsInOlympiadMode(true);
            _playerTwo.setIsInOlympiadMode(true);
            
            _playerOne.teleToLocation(_stadiumPort[0], _stadiumPort[1], _stadiumPort[2]);
            _playerTwo.teleToLocation(_stadiumPort[0], _stadiumPort[1], _stadiumPort[2]);
            
            _playerOne.setOlympiadSide(2);
            _playerTwo.setOlympiadSide(1);
        }
        
        protected void sendMessageToPlayers(boolean toBattleBegin)
        {
            if (_playerOne == null || _playerTwo == null) return;
            
            if(!toBattleBegin)
                _sm = new SystemMessage(SystemMessage.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
            else
                _sm = new SystemMessage(SystemMessage.THE_GAME_WILL_START_IN_S1_SECOND_S);
            
            _sm.addNumber(20);
            
            for (L2PcInstance player : _players)
                player.sendPacket(_sm);
        }
        
        protected void portPlayersBack()
        {
            if (_playerOne == null || _playerTwo == null)
                return;
            
            _playerOne.teleToLocation(_playerOneLocation[0],
                                      _playerOneLocation[1],
                                      _playerOneLocation[2]);
            
            _playerTwo.teleToLocation(_playerTwoLocation[0],
                                      _playerTwoLocation[1],
                                      _playerTwoLocation[2]);
            
            for (L2PcInstance player : _players)
            {
                player.setIsInOlympiadMode(false);
                player.setOlympiadSide(-1);
                player.setOlympiadGameId(-1);
                player.getStatus().setCurrentCp(player.getMaxCp());
                player.getStatus().setCurrentHp(player.getMaxHp());
                player.getStatus().setCurrentMp(player.getMaxMp());
                player.getStatus().startHpMpRegeneration();

                //Add clan skill
                if (player.getClan() != null){ 
                    for(L2Skill skill: player.getClan().getAllSkills())
                    {
                        if(skill.getMinPledgeClass() <= player.getPledgeClass())
                            player.addSkill(skill,false);
                    }
                }
            }
        }
        
        protected void validateWinner() throws Exception
        {
            StatsSet playerOneStat;
            StatsSet playerTwoStat;
            
            playerOneStat = _nobles.get(_playerOne.getObjectId());
            playerTwoStat = _nobles.get(_playerTwo.getObjectId());
            
            int playerOnePlayed = playerOneStat.getInteger(COMP_DONE);
            int playerTwoPlayed = playerTwoStat.getInteger(COMP_DONE);
            
            int playerOnePoints = playerOneStat.getInteger(POINTS);
            int playerTwoPoints = playerTwoStat.getInteger(POINTS);
            
            double playerOneHp = _playerOne.getStatus().getCurrentHp();
            double playerTwoHp = _playerTwo.getStatus().getCurrentHp();
            
            double hpDiffOne = _playerOne.getMaxHp() - playerOneHp;
            double hpDiffTwo = _playerTwo.getMaxHp() - playerTwoHp;
            
            _sm = new SystemMessage(SystemMessage.S1_HAS_WON_THE_GAME);
            
            if (_playerOne == null && _playerTwo == null)
            	return;
            
            /*
            ******************************************************************************************
            OLYMPIAD MODUS CHANGE by Padme
            ******************************************************************************************

            ******************************************************************************************
            FIRST CHANGE:
            check first if one player is dead
            then the other player is the winner - no doubt about it!
            when both are dead there is a tie - hey - dead - no breathe anymore
            ******************************************************************************************
            */
            
            if ( _playerOne.isDead() && _playerTwo.isDead() )
            {
            	hpDiffOne = 0;
            	hpDiffTwo = 0;
            }
            else if ( _playerOne.isDead() )
            {
            	hpDiffTwo = 0;
	            if (_log.isDebugEnabled()) _log.debug("Messages Olympiad: " + _playerOne.getName() + " is dead he has lost the match.");
            }
            else if ( _playerTwo.isDead() )
            {
            	hpDiffOne = 0;
	            if (_log.isDebugEnabled()) _log.debug("Messages Olympiad: " + _playerTwo.getName() + " is dead he has lost the match.");
            }
            
            /*
            ******************************************************************************************
            SECOND CHANGE:
            If one player is not in the olympiad stadium (relog/exit the game or something else)
            then the other player has won the match
            when both player are outside the stadium, theres a tie
            ******************************************************************************************
            */

            // PlayerOne has relogged during combat - he will loose the battle
            if (_playerOne.isOnline() == 0)
            {
            	hpDiffOne = 10000; // set a static value, so that no tie is possible, except both players are gone
            	_log.info("Messages Olympiad: " + _playerOne.getName() + " has left/relogged during combat.");
            }
            // PlayerTwo has relogged during combat - he will loose the battle
            if (_playerTwo.isOnline() == 0)
            {
            	hpDiffTwo = 10000; // set a static value, so that no tie is possible, except both players are gone
            	_log.info("Messages Olympiad: " + _playerTwo.getName() + " has left/relogged during combat.");
            }

            /*
            ******************************************************************************************
            ******************************************************************************************
            ******************************************************************************************
            */


            if (hpDiffOne < hpDiffTwo || _playerTwo == null)
            {
    			int pointDiff;
    			pointDiff = (playerTwoPoints / 3);
                playerOneStat.set(POINTS, playerOnePoints + pointDiff);
                playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
                
                _sm.addString(_playerOne.getName());
                broadcastMessage(_sm, true);
		            if (_log.isDebugEnabled()) _log.debug("Messages Olympiad: " + _playerOne.getName() + " has won the Match vs " + _playerTwo.getName() + ".");
            }
            else if (hpDiffTwo < hpDiffOne || _playerOne == null)
            {
    			int pointDiff;
    			pointDiff = (playerOnePoints / 3);
                playerTwoStat.set(POINTS, playerTwoPoints + pointDiff);
                playerOneStat.set(POINTS, playerOnePoints - pointDiff);
                
                _sm.addString(_playerTwo.getName());
                broadcastMessage(_sm, true);
                if (_log.isDebugEnabled()) _log.debug("Messages Olympiad: " + _playerTwo.getName() + " has won the Match vs " + _playerOne.getName() + ".");
            }
            else
            {
                _sm = new SystemMessage(SystemMessage.THE_GAME_ENDED_IN_A_TIE);
                broadcastMessage(_sm, true);
                if (_log.isDebugEnabled()) _log.debug("Messages Olympiad: " + _playerOne.getName() + " vs " + _playerTwo.getName() + " ended in a tie.");
            }
            
            playerOneStat.set(COMP_DONE, playerOnePlayed + 1);
            playerTwoStat.set(COMP_DONE, playerTwoPlayed + 1);
            
            _nobles.remove(_playerOne.getObjectId());
            _nobles.remove(_playerTwo.getObjectId());
            
            _nobles.put(_playerOne.getObjectId(), playerOneStat);
            _nobles.put(_playerTwo.getObjectId(), playerTwoStat);
            
            _sm = new SystemMessage(SystemMessage.YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S);
            _sm.addNumber(20);
            broadcastMessage(_sm, true);
        }
        
        protected void additions()
        {
            if (_playerOne == null || _playerTwo == null)
                return;
            
            for (L2PcInstance player : _players)
            {
                //Set HP/CP/MP to Max
                player.getStatus().setCurrentCp(player.getMaxCp());
                player.getStatus().setCurrentHp(player.getMaxHp());
                player.getStatus().setCurrentMp(player.getMaxMp());

                //Buff ww to both
                L2Skill skill;
                SystemMessage sm;
                
                skill = SkillTable.getInstance().getInfo(1204, 2);
                skill.getEffects(player, player);
                player.broadcastPacket(new MagicSkillUser(player, player, skill.getId(), 2, skill.getSkillTime(), 0));
                sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                sm.addSkillName(1204);
                player.sendPacket(sm);

                if (!player.isMageClass())
                {
                    //Buff haste to non-mages
                    skill = SkillTable.getInstance().getInfo(1086, 1);
                    skill.getEffects(player, player);
                    player.broadcastPacket(new MagicSkillUser(player, player, skill.getId(), 1, skill.getSkillTime(), 0));
                    sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                    sm.addSkillName(1086);
                    player.sendPacket(sm);
                }
                else
                {                    
                    //Buff acumen to mages
                    skill = SkillTable.getInstance().getInfo(1085, 1);
                    skill.getEffects(player, player);
                    player.broadcastPacket(new MagicSkillUser(player, player, skill.getId(), 1, skill.getSkillTime(), 0));
                    sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                    sm.addSkillName(1085);
                    player.sendPacket(sm);                    
                }
            }
        }
        
        protected void makePlayersVisible()
        {
           /* if (_playerOne == null || _playerTwo == null)
                return; */
            
            _sm = new SystemMessage(SystemMessage.STARTS_THE_GAME);
            
            for (L2PcInstance player : _players)
            {
                player.getAppearance().setVisible();
                player.broadcastUserInfo();
                player.sendPacket(_sm);
                if (player.getPet() != null)
                    player.getPet().updateAbnormalEffect();
            }
        }
        
        protected String getTitle()
        {
            String msg = "";
            
            if (_playerOne == null || _playerTwo == null)
                return msg;
            
            msg+= _playerOne.getName() + " : ";
            msg+= _playerTwo.getName();
            
            return msg;
        }
        
        protected L2PcInstance[] getPlayers()
        {
            L2PcInstance[] players = new L2PcInstance[2];
            
            if (_playerOne == null || _playerTwo == null) return players;
            
            players[0] = _playerOne;
            players[1] = _playerTwo;
            
            return players;
        }
        
        protected FastList<L2PcInstance> getSpectators()
        {
            return _spectators;
        }
        
        protected void addSpectator(L2PcInstance spec)
        {
            _spectators.add(spec);
        }
        
        protected void removeSpectator(L2PcInstance spec)
        {
            if (_spectators.contains(spec))
                _spectators.remove(spec);
        }
        
        protected void clearSpectators()
        { 
            if (_spectators != null)
            {
                for (L2PcInstance pc : _spectators)
                {
                    if(pc == null || !pc.inObserverMode()) continue;
                    pc.leaveOlympiadObserverMode();
                }
            }
            _spectators.clear();
        }
        
        private void broadcastMessage(SystemMessage sm, boolean toAll)
        {
            if (_playerOne == null || _playerTwo == null) return;
            
            _playerOne.sendPacket(sm);
            _playerTwo.sendPacket(sm);
            
            if (toAll)
            {
                if (_spectators != null)
                {
                    for (L2PcInstance spec : _spectators)
                    {
                        spec.sendPacket(sm);
                    }
                }
            }
        }
    }
}