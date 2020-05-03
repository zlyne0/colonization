package promitech.logger;

enum Level {
    INFO(true, false),
    DEBUG(true, true); 
    
    private final boolean infoMsg;
    private final boolean debugMsg;
    
    private Level(boolean infoMsg, boolean debugMsg) {
        this.infoMsg = infoMsg;
        this.debugMsg = debugMsg;
    }

    public boolean isInfoMsg() {
        return infoMsg;
    }

    public boolean isDebugMsg() {
        return debugMsg;
    }
    
    public static Level of(String str) {
        str = str.toUpperCase();
        for (Level level : Level.values()) {
            if (level.name().equals(str)) {
                return level;
            }
        }
        throw new IllegalArgumentException("can not find logger level by string: " + str);
    }
}