package promitech.logger;

import java.util.ArrayList;
import java.util.List;

public class ListLineAppender implements Appender {

    private final List<String> lines = new ArrayList<String>();

    public List<String> getLines() {
        return lines;
    }

    @Override
    public void line(String line) {
        this.lines.add(line);
    }
    
}
