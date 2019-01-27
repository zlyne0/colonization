package promitech.colonization.ui.resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sf.freecol.common.i18n.Number;
import net.sf.freecol.common.i18n.NumberRules;
import net.sf.freecol.common.i18n.Selector;
import net.sf.freecol.common.i18n.TurnSelector;
import net.sf.freecol.common.model.Identifiable;
import promitech.colonization.ui.resources.StringTemplate.TemplateType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

public class Messages {

	private static final Messages messagesResources = new Messages();
	private static final Map<String, Selector> tagMap = new HashMap<String, Selector>();
	static {
		tagMap.put("turn", new TurnSelector());
	}
    public static void setGrammaticalNumber(Number number) {
        tagMap.put("plural", number);
    }
	
	private I18NBundle msgBundle;
	
	private Messages() {
	}

	public static Messages instance() {
		return messagesResources;
	}
	
	public void load() {
		load(Locale.getDefault());
	}

	public void load(Locale locale) {
		I18NBundle.setExceptionOnMissingKey(false);
		
		FileHandle baseFileHandle = Gdx.files.internal("i18n/FreeColMessages");
		msgBundle = I18NBundle.createBundle(baseFileHandle, locale);

		NumberRules.load();
		setGrammaticalNumber(NumberRules.getNumberForLanguage(locale.getLanguage()));
	}
	
	public static String msg(String key) {
		return messagesResources.msgBundle.get(key);
	}
	
	public static String msgName(String key) {
		return messagesResources.msgBundle.get(nameKey(key));
	}

	public static String msgName(Identifiable identifiable) {
		return msg(nameKey(identifiable.getId()));
	}
	
	public static String shortDescriptionMsg(Identifiable identifiable) {
		return msg(shortDescriptionKey(identifiable.getId()));
	}
	
	public void dispose() {
	}

	public static String nameKey(String id) {
		return id + ".name";
	}

	public static String descriptionKey(String id) {
		return id + ".description";
	}
	
	public static String shortDescriptionKey(String id) {
		return id + ".shortDescription";
	}
	
    public static boolean containsKey(String key) {
    	String st = messagesResources.msgBundle.get(key);
    	if (st.equals("???" + key + "???")) {
    		return false;
    	}
    	return true;
    }
	
    public static int keyMessagePrefixCount(String keyMsg) {
    	int count = 0;
    	while (count < 100) {
    		if (containsKey(keyMsg + count)) {
    			count++;
    		} else {
    			break;
    		}
    	}
    	return count;
    }
    
    private static Selector getSelector(String tag) {
        return tagMap.get(tag.toLowerCase(Locale.US));
    }
    
    public static String message(StringTemplate template) {
        String result = "";
        switch (template.getTemplateType()) {
        case LABEL:
            if (template.getReplacements() == null || template.getReplacements().isEmpty()) {
                return msg(template.getId());
            } else {
                for (StringTemplate other : template.getReplacements()) {
                    result += template.getId() + message(other);
                }
                if (result.length() > template.getId().length()) {
                    return result.substring(template.getId().length());
                } else {
                    System.out.println("incorrect use of template " + template);
                    return result;
                }
            }
        case TEMPLATE:
            if (containsKey(template.getId())) {
                result = msg(template.getId());
            } else if (template.getDefaultId() != null) {
                result = msg(template.getDefaultId());
            }
            result = replaceChoices(result, template);
            for (int index = 0; index < template.getKeys().size(); index++) {
                result = result.replace(template.getKeys().get(index),
                    message(template.getReplacements().get(index)));
            }
            return result;
        case KEY:
            String key = msg(template.getId());
            if (key == null) {
                return template.getId();
            } else {
                return replaceChoices(key, null);
            }
        case NAME:
        default:
            return template.getId();
        }
    }

    /**
     * Replace all choice formats in the given string, using keys and
     * replacement values from the given template, which may be null.
     *
     * A choice format is enclosed in double brackets and consists of
     * a tag, followed by a colon, followed by an optional selector,
     * followed by a pipe character, followed by one or several
     * choices separated by pipe characters. If there is only one
     * choice, it must be a message identifier or a
     * variable. Otherwise, each choice consists of a key and a value
     * separated by an assignment character. Example:
     * "{{tag:selector|key1=val1|key2=val2}}".
     *
     * @param input a <code>String</code> value
     * @param template a <code>StringTemplate</code> value
     * @return a <code>String</code> value
     */
    private static String replaceChoices(String input, StringTemplate template) {
        int openChoice = 0;
        int closeChoice = 0;
        int highWaterMark = 0;
        StringBuilder result = new StringBuilder();
        while ((openChoice = input.indexOf("{{", highWaterMark)) >= 0) {
            result.append(input.substring(highWaterMark, openChoice));
            closeChoice = findMatchingBracket(input, openChoice + 2);
            if (closeChoice < 0) {
                // no closing brackets found
                System.out.println("Mismatched brackets: " + input);
                return result.toString();
            }
            highWaterMark = closeChoice + 2;
            int colonIndex = input.indexOf(':', openChoice + 2);
            if (colonIndex < 0 || colonIndex > closeChoice) {
            	System.out.println("No tag found: " + input);
                continue;
            }
            String tag = input.substring(openChoice + 2, colonIndex);
            int pipeIndex = input.indexOf('|', colonIndex + 1);
            if (pipeIndex < 0 || pipeIndex > closeChoice) {
            	System.out.println("No choices found: " + input);
                continue;
            }
            String selector = input.substring(colonIndex + 1, pipeIndex);
            if (selector.isEmpty()) {
                selector = "default";
            } else if (selector.startsWith("%") && selector.endsWith("%")) {
                if (template == null) {
                    selector = "default";
                } else {
                    StringTemplate replacement = template.getReplacement(selector);
                    if (replacement == null) {
                    	System.out.println("Failed to find replacement for " + selector);
                        continue;
                    } else {
                        selector = message(replacement);
                        Selector taggedSelector = getSelector(tag);
                        if (taggedSelector != null) {
                            selector = taggedSelector.getKey(selector, input);
                        }
                    }
                }
            } else {
                Selector taggedSelector = getSelector(tag);
                if (taggedSelector != null) {
                    selector = taggedSelector.getKey(selector, input);
                }
            }
            int keyIndex = input.indexOf(selector, pipeIndex + 1);
            if (keyIndex < 0 || keyIndex > closeChoice) {
                // key not found, choice might be a key itself
                String otherKey = input.substring(pipeIndex + 1, closeChoice);
                if (otherKey.startsWith("%") && otherKey.endsWith("%") && template != null) {
                    StringTemplate replacement = template.getReplacement(otherKey);
                    if (replacement == null) {
                    	System.out.println("Failed to find replacement for " + otherKey);
                        continue;
                    } else if (replacement.getTemplateType() == TemplateType.KEY) {
                        otherKey = msg(replacement.getId());
                        keyIndex = otherKey.indexOf("{{");
                        if (keyIndex < 0) {
                            // not a choice format
                            result.append(otherKey);
                        } else {
                            keyIndex = otherKey.indexOf(selector, keyIndex);
                            if (keyIndex < 0) {
                            	System.out.println("Failed to find key " + selector + " in replacement " + replacement.getId());
                                continue;
                            } else {
                                result.append(getChoice(otherKey, selector));
                            }
                        }
                    } else {
                    	System.out.println("Choice substitution attempted, but template type was " + replacement.getTemplateType());
                        continue;
                    }
                } else if (containsKey(otherKey)) {
                    otherKey = getChoice(msg(otherKey), selector);
                    result.append(otherKey);
                } else {
                	System.out.println("Unknown key or untagged choice: '" + otherKey
                                   + "', selector was '" + selector
                                   + "', trying 'default' instead");
                    int defaultStart = otherKey.indexOf("default=");
                    if (defaultStart >= 0) {
                        defaultStart += 8;
                        int defaultEnd = otherKey.indexOf('|', defaultStart);
                        String defaultChoice;
                        if (defaultEnd < 0) {
                            defaultChoice = otherKey.substring(defaultStart);
                        } else {
                            defaultChoice = otherKey.substring(defaultStart, defaultEnd);
                        }
                        result.append(defaultChoice);
                    } else {
                    	System.out.println("No default choice found.");
                        continue;
                    }
                }
            } else {
                int start = keyIndex + selector.length() + 1;
                int replacementIndex = input.indexOf('|', start);
                int nextOpenIndex = input.indexOf("{{", start);
                if (nextOpenIndex >= 0 && nextOpenIndex < replacementIndex) {
                    replacementIndex = input.indexOf('|', findMatchingBracket(input, nextOpenIndex + 2) + 2);
                }
                int end = (replacementIndex < 0 || replacementIndex > closeChoice)
                    ? closeChoice : replacementIndex;
                String replacement = input.substring(start, end);
                if (!replacement.contains("{{")) {
                    result.append(replacement);
                } else {
                    result.append(replaceChoices(replacement, template));
                }
            }
        }
        result.append(input.substring(highWaterMark));
        return result.toString();
    }

    /**
     * Return the choice tagged with the given key, or null, if the
     * given input string does not contain the key.
     *
     * @param input a <code>String</code> value
     * @param key a <code>String</code> value
     * @return a <code>String</code> value
     */
    private static String getChoice(String input, String key) {
        int keyIndex = input.indexOf(key);
        if (keyIndex < 0) {
            return null;
        } else {
            int start = keyIndex + key.length() + 1;
            int end = input.indexOf('|', start);
            if (end < 0) {
                end = input.indexOf("}}", start);
                if (end < 0) {
                	System.out.println("Failed to find end of choice for key " + key + " in input " + input);
                    return null;
                }
            }
            return input.substring(start, end);
        }
    }
    
    /**
     * Return the index of the matching pair of brackets, or -1 if
     * none is found.
     *
     * @param input a <code>String</code> value
     * @param start an <code>int</code> value
     * @return an <code>int</code> value
     */
    private static int findMatchingBracket(String input, int start) {
        char last = 0;
        int level = 0;
        for (int index = start; index < input.length(); index++) {
            switch(input.charAt(index)) {
            case '{':
                if (last == '{') {
                    last = 0;
                    level++;
                } else {
                    last = '{';
                }
                break;
            case '}':
                if (last == '}') {
                    if (level == 0) {
                        return index - 1;
                    } else {
                        last = 0;
                        level--;
                    }
                } else {
                    last = '}';
                }
                break;
            default:
                break;
            }
        }
        // found no matching bracket
        return -1;
    }
}
