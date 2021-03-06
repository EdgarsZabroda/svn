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
package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class Couple
{
    private static final Log _log = LogFactory.getLog(Couple.class.getName());
    
    // =========================================================
    // Data Field
    private int _Id                             = 0;
    private int _player1Id                      = 0;
    private int _player2Id                      = 0;
    private boolean _maried                     = false;
    private Calendar _affiancedDate;
    private Calendar _weddingDate;

    // =========================================================
    // Constructor
    public Couple(int coupleId)
    {
        this._Id = coupleId;
        
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select * from couples where id = ?");
            statement.setInt(1, this._Id);
            rs = statement.executeQuery();

            while (rs.next())
            {
                this._player1Id = rs.getInt("player1Id");
                this._player2Id = rs.getInt("player2Id");
                this._maried    = rs.getBoolean("maried");

                this._affiancedDate = Calendar.getInstance();
                this._affiancedDate.setTimeInMillis(rs.getLong("affiancedDate"));

                this._weddingDate = Calendar.getInstance();
                this._weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
            }
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: Couple.load(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public Couple(L2PcInstance player1,L2PcInstance player2)
    {
        int _tempPlayer1Id = player1.getObjectId();
        int _tempPlayer2Id = player2.getObjectId();

        this._player1Id = _tempPlayer1Id;
        this._player2Id = _tempPlayer2Id;

        this._affiancedDate = Calendar.getInstance();
        this._affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

        this._weddingDate = Calendar.getInstance();
        this._weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            this._Id = IdFactory.getInstance().getNextId();
            statement = con.prepareStatement("INSERT INTO couples (id, player1Id, player2Id, maried, affiancedDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setInt(1, this._Id);
            statement.setInt(2, this._player1Id);
            statement.setInt(3, this._player2Id);
            statement.setBoolean(4, false);
            statement.setLong(5, this._affiancedDate.getTimeInMillis());
            statement.setLong(6, this._weddingDate.getTimeInMillis());            
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("",e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void marry()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;

            statement = con.prepareStatement("UPDATE couples set maried = ?, weddingDate = ? where id = ?");
            statement.setBoolean(1, true);
            this._weddingDate = Calendar.getInstance();
            statement.setLong(2, this._weddingDate.getTimeInMillis());
            statement.setInt(3, this._Id);
            statement.execute();
            statement.close();
            this._maried = true;
        }
        catch (Exception e)
        {
            _log.error("",e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void divorce()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            
            statement = con.prepareStatement("DELETE FROM couples WHERE id=?");
            statement.setInt(1, this._Id);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.error("Exception: Couple.divorce(): " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public final int getId() { return this._Id; }

    public final int getPlayer1Id() { return this._player1Id; }
    public final int getPlayer2Id() { return this._player2Id; }    

    public final boolean getMaried() { return this._maried; }    

    public final Calendar getAffiancedDate() { return this._affiancedDate; }
    public final Calendar getWeddingDate() { return this._weddingDate; }
}
