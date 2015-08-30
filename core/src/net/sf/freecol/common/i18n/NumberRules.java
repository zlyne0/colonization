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

package net.sf.freecol.common.i18n;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.freecol.common.i18n.Number.Category;


/**
 * See the
 * <a href="http://cldr.unicode.org/index/cldr-spec/plural-rules">
 * Common Locale Data Repository</a>.
 */
public class NumberRules {

    private static final Logger logger = Logger.getLogger(NumberRules.class.getName());

    /**
     * A rule that always returns category "other".
     */
    public static final Number OTHER_NUMBER_RULE = new OtherNumberRule();

    /**
     * A rule that assigns 1 to category "one", 2 to category "two"
     * and all other numbers to category "other".
     */
    public static final Number DUAL_NUMBER_RULE = new DualNumberRule();

    /**
     * A rule that assigns 1 to category "one" and all other numbers
     * to category "other".
     */
    public static final Number PLURAL_NUMBER_RULE = new PluralNumberRule();

    /**
     * A rule that assigns 0 and 1 to category "one", and all other
     * number to category "other".
     */
    public static final Number ZERO_ONE_NUMBER_RULE = new ZeroOneNumberRule();


    private static final Map<String, Number> numberMap = new HashMap<String, Number>();

    /**
     * Returns a rule appropriate for the given language, or the
     * OTHER_NUMBER_RULE if none has been defined.
     *
     * @param lang a <code>String</code> value
     * @return a <code>Number</code> value
     */
    public static Number getNumberForLanguage(String lang) {
        Number number = numberMap.get(lang);
        return (number == null) ? OTHER_NUMBER_RULE : number;
    }

    /**
     * Describe <code>isInitialized</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public static boolean isInitialized() {
        return !numberMap.isEmpty();
    }

    private static void processNumberRule(DefaultNumberRule numberRule, String loc) {
        String[] locales = (loc == null) ? null : loc.split(" ");
        if (locales != null) {            
            Number number = null;
            switch(numberRule.countRules()) {
            case 0:
                number = OTHER_NUMBER_RULE;
                break;
            case 1:
                Rule rule = numberRule.getRule(Category.one);
                if (rule != null) {
                    if ("n is 1".equals(rule.toString())) {
                        number = PLURAL_NUMBER_RULE;
                    } else if ("n in 0..1".equals(rule.toString())) {
                        number = ZERO_ONE_NUMBER_RULE;
                    }
                }
                break;
            case 2:
                Rule oneRule = numberRule.getRule(Category.one);
                Rule twoRule = numberRule.getRule(Category.two);
                if (oneRule != null
                    && "n is 1".equals(oneRule.toString())
                    && twoRule != null
                    && "n is 2".equals(twoRule.toString())) {
                    number = DUAL_NUMBER_RULE;
                }
                break;
            default:
                number = numberRule;
            }
            for (String locale : locales) {
                numberMap.put(locale, number);
            }
        }
    }
    
    public static void load() {
    	DefaultNumberRule numberRule = new DefaultNumberRule();
    	{
    		String plu = "one";
    		Category category = Category.valueOf(plu);
            Rule rule = new Rule("n is 1");
            numberRule.addRule(category, rule);
    	}
    	{
    		String plu = "few";
    		Category category = Category.valueOf(plu);
            Rule rule = new Rule("n mod 10 in 2..4 and n mod 100 not in 12..14");
            numberRule.addRule(category, rule);
    	}
    	{
    		String plu = "many";
    		Category category = Category.valueOf(plu);
            Rule rule = new Rule("n is not 1 and n mod 10 in 0..1 or n mod 10 in 5..9 or n mod 100 in 12..14");
            numberRule.addRule(category, rule);
    	}
    	processNumberRule(numberRule, "pl");
    	
    	numberRule = new DefaultNumberRule();
    	{
    		String plu = "one";
    		Category category = Category.valueOf(plu);
            Rule rule = new Rule("n is 1");
            numberRule.addRule(category, rule);
    	}
        numberMap.put("el en eo es et fi fo gl he iw it", numberRule);
    }
}
