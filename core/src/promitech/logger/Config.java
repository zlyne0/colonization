package promitech.logger;

class Config {
    private String prefix;
    private Level level;
    private Class<? extends Appender> appenderClass;
    
    public Config(Level level, Class<? extends Appender> appenderClass) {
        this.prefix = "";
        this.level = level;
        this.appenderClass = appenderClass;
    }

    public Config(String prefix, Level level, Class<? extends Appender> appenderClass) {
        this.prefix = prefix;
        this.level = level;
        this.appenderClass = appenderClass;
    }

    public boolean isAccessible(Class<?> clazz) {
        return clazz.getName().startsWith(prefix);
    }

    public Level getLevel() {
        return level;
    }

    public Class<? extends Appender> getAppenderClass() {
        return appenderClass;
    }
}