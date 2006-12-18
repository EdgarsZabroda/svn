/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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
package net.sf.l2j.loginserver.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javolution.util.FastList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class manage ban list
 * 
 * @version $Revision: $ $Date: $
 */
public class BanManager
{
    private static BanManager _instance = null;
    private static final Log _log = LogFactory.getLog(BanManager.class);
    private static List<String> _bannedIPs = new FastList<String>();

    /**
     * return singleton for banmanager
     * @return BanManager instance
     */
    public static BanManager getInstance()
    {
        if (_instance == null) return new BanManager();
        else return _instance;
    }
    
    public void addBannedIP(String ip, int incorrectCount)
    {
        _bannedIPs.add(ip);
        int time = incorrectCount * incorrectCount * 1000;
        //System.out.println("Banning ip "+ip+" for "+time/1000.0+" seconds.");
        ThreadPoolManager.getInstance().scheduleGeneral(new UnbanTask(ip), time);
    }    
    
    /**
     * 
     * @param ip
     */
    private void addBannedIP(String ip)
    {
        _bannedIPs.add(ip);
    }
    
    /**
     * 
     * @param ip
     */
    public void unBanIP(String ip)
    {
        _bannedIPs.remove(ip);
    }
    
    /**
     * Load banned list
     *
     */
    private BanManager()
    {
        try
        {
            // try to read banned list
            File file = new File("config/banned_ip.cfg");
            List lines = FileUtils.readLines(file, "UTF-8");            
            
            int count = 0;
            for (int i = 0 ; i< lines.size();i++)
            {
                String line = (String)lines.get(i);
                line = line.trim();
                if (line.length() > 0)
                {
                    count++;
                    addBannedIP(line);
                }
            }
            _log.info(count + " banned IPs defined");
        }
        catch (IOException e)
        {
            _log.warn("error while reading banned file:" + e);
        }
    }
    
    /**
     * Check if ip is in banned list
     * @param ip
     * @return true or false if ip is banned or not
     */
    public boolean isIpBanned (String ip)
    {
        return _bannedIPs.contains(ip);
    }
    

    /**
     * 
     * This runnable manage unban task for an ip
     * 
     */
    private class UnbanTask implements Runnable
    {
        private String _ip;
        public UnbanTask(String IP)
        {
            _ip = IP;
        }
        public void run()
        {
            BanManager.getInstance().unBanIP(_ip);
        }
        
    }    
}
