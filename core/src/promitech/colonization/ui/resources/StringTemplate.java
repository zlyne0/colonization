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

package promitech.colonization.ui.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.util.Utils;


/**
 * The <code>StringTemplate</code> represents a non-localized string
 * that can be localized by looking up its value in a message bundle
 * or similar Map. The StringTemplate may contain variables (keys)
 * delimited by the '%' character, such as "%amount%" that will be
 * replaced with a string or a StringTemplate. If the StringTemplate
 * contains replacement values but no keys, then it is considered a
 * "label" StringTemplate, and its value will be used to join the
 * replacement values.
 *
 * @version 1.0
 */
public class StringTemplate extends ObjectWithId {

    /**
     * The type of this StringTemplate, either NAME, a proper name
     * that must not be localized (e.g. "George Washington"), or KEY,
     * a string that must be localized (e.g. "model.goods.food.name"),
     * or TEMPLATE, a key with replacements to apply to the localized
     * value of the key, or LABEL, a separator string that will be
     * used to join the replacement values.
     */
    public static enum TemplateType { NAME, KEY, TEMPLATE, LABEL }

    /** The TemplateType of this StringTemplate. Defaults to KEY. */
    private TemplateType templateType = TemplateType.KEY;

    /**
     * An alternative key to use if the identifier is not contained in
     * the message bundle.
     */
    private String defaultId;

    /** The keys to replace within the string template. */
    private List<String> keys = null;

    /** The values with which to replace the keys in the string template. */
    private List<StringTemplate> replacements = null;

    /**
     * Deliberately empty constructor.
     */
    protected StringTemplate() {
    	super(null);
    	throw new IllegalStateException("should not be invoked");
    }

    /**
     * Creates a new <code>StringTemplate</code> instance.
     *
     * @param id The object identifier.
     * @param template A <code>StringTemplate</code> to copy.
     */
    public StringTemplate(String id, StringTemplate template) {
        super(id);
        this.templateType = template.templateType;
        this.keys = template.keys;
        this.replacements = template.replacements;
    }

    /**
     * Creates a new <code>StringTemplate</code> instance.
     *
     * @param id The object identifier.
     * @param templateType The <code>TemplateType</code> for this template.
     */
    protected StringTemplate(String id, TemplateType templateType) {
        super(id);
        this.templateType = templateType;
        this.keys = null;
        this.replacements = null;
    }

    /**
     * Get the default identifier.
     *
     * @return The default identifier.
     */
    public final String getDefaultId() {
        return defaultId;
    }

    /**
     * Set the default identifier.
     *
     * @param newDefaultId The new default identifier
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate setDefaultId(final String newDefaultId) {
        this.defaultId = newDefaultId;
        return this;
    }

    /**
     * Get the template type.
     *
     * @return The template type.
     */
    public final TemplateType getTemplateType() {
        return templateType;
    }

    /**
     * Get the keys.
     *
     * @return A list of keys.
     */
    public final List<String> getKeys() {
        return (keys == null) ? Collections.<String>emptyList()
            : keys;
    }

    /**
     * Add a key.
     * 
     * @param key The key to add.
     */
    private void addKey(String key) {
        if (keys == null) {
        	keys = new ArrayList<String>();
        }
        keys.add(key);
    }

    /**
     * Get the replacements.
     *
     * @return A list of replacements.
     */
    public final List<StringTemplate> getReplacements() {
        return (replacements == null) ? Collections.<StringTemplate>emptyList()
            : replacements;
    }
    
    /**
     * Add a replacement.
     *
     * @param replacement The <code>StringTemplate</code> replacement to add.
     */
    private void addReplacement(StringTemplate replacement) {
        if (replacements == null) {
            replacements = new ArrayList<StringTemplate>();
        }
        replacements.add(replacement);
    }


    // Factory methods

    public static StringTemplate name(String value) {
        return new StringTemplate(value, TemplateType.NAME);
    }

    public static StringTemplate key(String value) {
        return new StringTemplate(value, TemplateType.KEY);
    }

    public static StringTemplate template(String value) {
        return new StringTemplate(value, TemplateType.TEMPLATE);
    }

    public static StringTemplate label(String value) {
        return new StringTemplate(value, TemplateType.LABEL);
    }


    /**
     * Get the replacement value for a given key.
     *
     * @param key The key to find a replacement for.
     * @return The replacement found, or null if none found.
     */
    public final StringTemplate getReplacement(String key) {
        if (keys != null && replacements != null) {
            for (int index = 0; index < keys.size(); index++) {
                if (key.equals(keys.get(index))) {
                    if (replacements.size() > index) {
                        return replacements.get(index);
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Add a new key and replacement to the StringTemplate.  This is
     * only possible if the StringTemplate is of type TEMPLATE.
     *
     * @param key The key to add.
     * @param value The corresponding replacement.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate add(String key, String value) {
        if (templateType == TemplateType.TEMPLATE) {
            addKey(key);
            addReplacement(new StringTemplate(value, TemplateType.NAME));
        } else {
            throw new IllegalArgumentException("Cannot add key-value pair to StringTemplate type "
                                               + templateType);
        }
        return this;
    }

    public StringTemplate addKey(String key, String value) {
        if (templateType == TemplateType.TEMPLATE) {
            addKey(key);
            addReplacement(new StringTemplate(value, TemplateType.KEY));
        } else {
            throw new IllegalArgumentException("Cannot add key-value pair to StringTemplate type "
                                               + templateType);
        }
        return this;
    }
    
    /**
     * Add a replacement value without a key to the StringTemplate.
     * This is only possible if the StringTemplate is of type LABEL.
     *
     * @param value The replacement value.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate add(String value) {
        if (templateType == TemplateType.LABEL) {
            addReplacement(new StringTemplate(value, TemplateType.NAME));
        } else {
            throw new IllegalArgumentException("Cannot add a single string to StringTemplate type "
                                               + templateType);
        }
        return this;
    }

    /**
     * Add a new key and replacement to the StringTemplate.  The
     * replacement must be a proper name.  This is only possible if the
     * StringTemplate is of type TEMPLATE.
     *
     * @param key The key to add.
     * @param value The corresponding replacement.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate addName(String key, String value) {
        if (templateType == TemplateType.TEMPLATE) {
            addKey(key);
            addReplacement(new StringTemplate(Messages.nameKey(value), TemplateType.KEY));
        } else {
            throw new IllegalArgumentException("Cannot add key-value pair to StringTemplate type "
                                               + templateType);
        }
        return this;
    }

    /**
     * Add a new key and replacement to the StringTemplate.  The
     * replacement must be a proper name.  This is only possible if the
     * StringTemplate is of type TEMPLATE.
     *
     * @param key The key to add.
     * @param object The corresponding value.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate addName(String key, Identifiable object) {
        if (templateType == TemplateType.TEMPLATE) {
            addKey(key);
            addReplacement(new StringTemplate(Messages.nameKey(object.getId()), TemplateType.KEY));
        } else {
            throw new IllegalArgumentException("Cannot add key-value pair to StringTemplate type "
                                               + templateType);
        }
        return this;
    }

    /**
     * Add a replacement value without a key to the StringTemplate.
     * The replacement must be a proper name.  This is only possible
     * if the StringTemplate is of type LABEL.
     *
     * @param value The replacement value.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate addName(String value) {
        if (templateType == TemplateType.LABEL) {
            addReplacement(new StringTemplate(value, TemplateType.NAME));
        } else {
            throw new IllegalArgumentException("Cannot add a single string to StringTemplate type "
                                               + templateType);
        }
        return this;
    }

    /**
     * Add a key and an integer value to replace it to this StringTemplate.
     *
     * @param key The key to add.
     * @param amount The integer value.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate addAmount(String key, Number amount) {
        add(key, amount.toString());
        return this;
    }

    /**
     * Add a key and a StringTemplate to replace it to this StringTemplate.
     *
     * @param key The key to add.
     * @param template The template value.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate addStringTemplate(String key, StringTemplate template) {
        if (templateType == TemplateType.TEMPLATE) {
            addKey(key);
            addReplacement(template);
        } else {
            throw new IllegalArgumentException("Cannot add a key-template pair to a StringTemplate type "
                                               + templateType);
        }
        return this;
    }

    /**
     * Add a StringTemplate to this LABEL StringTemplate.
     *
     * @param template The replacement <code>StringTemplate</code>.
     * @return This <code>StringTemplate</code>.
     */
    public StringTemplate addStringTemplate(StringTemplate template) {
        if (templateType == TemplateType.LABEL) {
            addReplacement(template);
        } else {
            throw new IllegalArgumentException("Cannot add a StringTemplate to StringTemplate type "
                                               + templateType);
        }
        return this;
    }


    // Override Object

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof StringTemplate) {
            StringTemplate t = (StringTemplate)o;
            if (!super.equals(o)
                || this.templateType != t.templateType
                || !Utils.equals(defaultId, t.defaultId)) return false;
            if (templateType == TemplateType.LABEL) {
                if ((replacements == null) != (t.replacements == null))
                    return false;
                if (replacements != null) {
                    if (replacements.size() == t.replacements.size()) {
                        for (int index = 0; index < replacements.size(); index++) {
                            if (!replacements.get(index).equals(t.replacements.get(index))) {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                }
            } else if (templateType == TemplateType.TEMPLATE) {
                if ((keys == null) != (t.keys == null)
                    || (replacements == null) != (t.replacements == null))
                    return false;
                if (keys != null && replacements != null) {
                    if (keys.size() == t.keys.size()
                        && replacements.size() == t.replacements.size()
                        && keys.size() == replacements.size()) {
                        for (int index = 0; index < replacements.size(); index++) {
                            if (!keys.get(index).equals(t.keys.get(index))
                                || !replacements.get(index).equals(t.replacements.get(index))) {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + templateType.ordinal();
        hash = 31 * hash + Utils.hashCode(defaultId);
        if (templateType == TemplateType.LABEL) {
            if (replacements != null) {
                for (StringTemplate replacement : replacements) {
                    hash = 31 * hash + Utils.hashCode(replacement);
                }
            }
        } else if (templateType == TemplateType.TEMPLATE) {
            if (keys != null && replacements != null) {
                for (int index = 0; index < keys.size(); index++) {
                    hash = 31 * hash + Utils.hashCode(keys.get(index));
                    hash = 31 * hash + Utils.hashCode(replacements.get(index));
                }
            }
        }
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(templateType).append(": ");
        switch (templateType) {
        case LABEL:
            if (replacements == null) {
                sb.append(getId());
            } else {
                for (StringTemplate object : replacements) {
                    sb.append(object).append(getId());
                }
            }
            break;
        case TEMPLATE:
            sb.append(getId());
            if (defaultId != null) {
                sb.append(" (").append(defaultId).append(")");
            }
            sb.append(" [");
            for (int index = 0; index < keys.size(); index++) {
                sb.append("[").append(keys.get(index)).append(": ")
                    .append(replacements.get(index)).append("]");
            }
            sb.append("]");
            break;
        case KEY:
            sb.append(getId());
            if (defaultId != null) {
                sb.append(" (").append(defaultId).append(")");
            }
            break;
        case NAME:
        default:
            sb.append(getId());
            break;
        }
        return sb.toString();
    }
    
    public String eval() {
    	return Messages.message(this);
    }
}
