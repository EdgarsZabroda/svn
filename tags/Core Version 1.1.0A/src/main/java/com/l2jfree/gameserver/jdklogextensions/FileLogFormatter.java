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
package com.l2jfree.gameserver.jdklogextensions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:30:08 $
 */

public class FileLogFormatter extends Formatter
{

    /* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    private static final String CRLF = "\r\n";
    private static final String _ = "\t";
    private SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");
    
    @Override
    public String format(LogRecord record)
    {
        TextBuilder output = new TextBuilder();
        
        return output
        .append(dateFmt.format(new Date(record.getMillis())))
        .append(_)
        .append(record.getLevel().getName())
        .append(_)
        .append(record.getThreadID())
        .append(_)
        .append(record.getLoggerName())
        .append(_)
        .append(record.getMessage())
        .append(CRLF)
        .toString();
    }
}
