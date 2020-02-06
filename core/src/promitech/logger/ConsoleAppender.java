package promitech.logger;

public class ConsoleAppender implements Appender {

    @Override
    public void line(String line) {
        System.out.println(line);
    }

}
