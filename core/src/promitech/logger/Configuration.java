package promitech.logger;

import java.util.ArrayList;
import java.util.List;

class Configuration {
    
    private final List<Config> configs = new ArrayList<Config>();
    private Config defConfig = new Config(Level.DEBUG, ConsoleAppender.class);

    public void config(String prefix, Level level, Class<? extends Appender> appenderClass) {
        configs.add(new Config(prefix, level, appenderClass));
    }

    public void config(String prefix, Level level) {
        configs.add(new Config(prefix, level, defConfig.getAppenderClass()));
    }
    
    public void configDef(Level info, Class<? extends Appender> appenderClass) {
        defConfig = new Config(info, appenderClass);
    }

    public void configDef(Level info) {
        defConfig = new Config(info, ConsoleAppender.class);
    }
    
    public Config configForClass(Class<?> clazz) {
        Config clazzConf = null;
        for (Config conf : configs) {
            if (conf.isAccessible(clazz)) {
                clazzConf = conf;
            }
        }
        if (clazzConf == null) {
            clazzConf = defConfig;
        }
        return clazzConf;
    }

    public void reset() {
        configs.clear();
        defConfig = new Config(Level.DEBUG, ConsoleAppender.class);
    }
}
