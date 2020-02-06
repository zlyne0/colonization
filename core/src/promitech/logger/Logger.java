package promitech.logger;

public class Logger {

    private Level level;
    private Appender appender;

    public Logger(Level level, Appender appender) {
        this.level = level;
        this.appender = appender;
    }
    
    public boolean isInfo() {
        return level.isInfoMsg();
    }
    
    public boolean isDebug() {
        return level.isDebugMsg();
    }
    
    public void info(String str, Object ... params) {
        if (isInfo()) {
            appender.line(String.format(str, params));
        }
    }

    public void debug(String str, Object ... parms) {
        if (isDebug()) {
            appender.line(String.format(str, parms));
        }
    }
}
