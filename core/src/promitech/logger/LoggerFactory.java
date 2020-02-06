package promitech.logger;

import java.util.HashMap;
import java.util.Map;

public class LoggerFactory {

    private final Map<Class<? extends Appender>, Appender> appenders = new HashMap<Class<? extends Appender>, Appender>();
    private Configuration configuration = new ConfigurationFileProvider().create();
    
    private static LoggerFactory instance = new LoggerFactory();

    public static LoggerFactory instance() {
        return instance;
    }
    
    public static Logger create(Class<?> clazz) {
        return instance.createLogger(clazz);
    }

    public Logger createLogger(Class<?> clazz) {
        Config clazzConf = configuration.configForClass(clazz);
        return new Logger(clazzConf.getLevel(), appender(clazzConf.getAppenderClass()));
    }
    
    public <T extends Appender> T appender(Class<T> appenderClass) {
        Appender appender = appenders.get(appenderClass);
        if (appender == null) {
            try {
                appender = appenderClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            appenders.put(appenderClass, appender);
        }
        return (T)appender;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
