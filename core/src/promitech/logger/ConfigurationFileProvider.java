package promitech.logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

class ConfigurationFileProvider {

    private static final String DEFAULT_ROOT_PREFIX = "defaultRoot";
    private final String confFileName;
    private final Properties prop;
    private final Map<String, Class<? extends Appender>> appenderClassByName = new HashMap<String, Class<? extends Appender>>(); 

    private Level parsedLevel;
    private Class<? extends Appender> parsedAppenderClass;
    
    ConfigurationFileProvider() {
        this("/logger.conf");
    }
    
    ConfigurationFileProvider(String confFileName) {
        this.confFileName = confFileName;
        this.prop = loadConf();
    }
    
    ConfigurationFileProvider(Properties prop) {
        this.confFileName = "not set, use properties";
        this.prop = prop;
    }
    
    public void load(Configuration conf) {
        conf.reset();
        
        String defaultRoot = (String)prop.get(DEFAULT_ROOT_PREFIX);
        if (defaultRoot != null) {
            parseProperty(defaultRoot);
            if (parsedAppenderClass == null) {
                conf.configDef(parsedLevel);
            } else {
                conf.configDef(parsedLevel, parsedAppenderClass);
            }
        }
        
        for (Entry<Object, Object> propEntry : prop.entrySet()) {
            String classPrefix = (String)propEntry.getKey();
            String classPrefixSetting = (String)propEntry.getValue();
            if (classPrefix.equals(DEFAULT_ROOT_PREFIX)) {
                continue;
            }
            
            parseProperty(classPrefixSetting);
            
            if (parsedAppenderClass == null) {
                conf.config(classPrefix, parsedLevel);
            } else {
                conf.config(classPrefix, parsedLevel, parsedAppenderClass);
            }
        }
    }
    
    public Configuration create() {
        Configuration conf = new Configuration();
        load(conf);
        return conf;
    }
    
    private void parseProperty(String str) {
        parsedLevel = null;
        parsedAppenderClass = null;
        
        String[] levelAppender = str.split(",");
        if (levelAppender.length >= 1) {
            parsedLevel = Level.of(levelAppender[0].trim());
        }
        if (levelAppender.length >= 2) {
            parsedAppenderClass = findAppenderClassByName(levelAppender[1].trim());
        }
    }
    
    private Class<? extends Appender> findAppenderClassByName(String className) {
        Class<? extends Appender> clazz = appenderClassByName.get(className);
        if (clazz != null) {
            return clazz;
        }
        try {
            clazz = (Class<? extends Appender>)Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("can not find appender class by string: " + className, e);
        }
        
        if (!Appender.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("class " + className + " is not assignable for " + Appender.class.getName());
        }
        appenderClassByName.put(className, clazz);
        return clazz;
    }
    
    private Properties loadConf() {
        try {
            InputStream resourceAsStream = ConfigurationFileProvider.class.getResourceAsStream(confFileName);
            if (resourceAsStream == null) {
                throw new IllegalArgumentException("can not open file " + confFileName);
            }
            Properties prop = new Properties();
            prop.load(resourceAsStream);
            return prop;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
