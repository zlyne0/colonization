/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.util;

import java.util.Collection;

/**
 * Collection of small static helper methods using Strings.
 */
public class StringUtils {

	public static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}
	
    public static String join(String delimiter, Collection<String> collection) {
	    StringBuilder str = new StringBuilder();
        for (String s : collection) {
            if (str.length() == 0) {
                str.append(delimiter);
            }
            str.append(s);
        }
        return str.toString();
    }

    /**
     * Truncate a string to a maximum length.
     *
     * @param str The string to chop.
     * @param maxLength The maximum length.
     * @return A string not exceeding maxLength.
     */
    public static String chop(String str, int maxLength) {
        return (str.length() > maxLength) ? str.substring(0, maxLength) : str;
    }

    /**
     * Gets the last part of a string after a supplied delimiter.
     *
     * @param s The string to operate on.
     * @param delim The delimiter.
     * @return The last part of the string after the last instance of
     *     the delimiter, or the original string if the delimiter is
     *     not present.
     */
    public static String lastPart(String s, String delim) {
        int last = (s == null) ? -1 : s.lastIndexOf(delim);
        return (last > 0) ? s.substring(last+delim.length(), s.length())
            : s;
    }
}
