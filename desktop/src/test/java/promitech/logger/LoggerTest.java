package promitech.logger;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class AAA {
}

class BBB {
}

class CCC {
}

class LoggerTest {

    
    @Test
    void canFilterLogMessagesDependsFromClassName() throws Exception {
        // given
        Properties prop = new Properties();
        prop.put("defaultRoot", "INFO, promitech.logger.NullAppender");
        prop.put("promitech.logger.A", "INFO, promitech.logger.ListLineAppender");
        prop.put("promitech.logger.B", "DEBUG, promitech.logger.ListLineAppender");
        
        ConfigurationFileProvider configurationFileProvider = new ConfigurationFileProvider(prop);
        configurationFileProvider.load(LoggerFactory.instance().getConfiguration());
        
        Logger loggerA = LoggerFactory.create(AAA.class);
        Logger loggerB = LoggerFactory.create(BBB.class);
        Logger loggerC = LoggerFactory.create(CCC.class);

        // when
        loggerA.info("test info params %s %s", "one", "two");
        loggerB.debug("test debug params %s %s", "ten", "eleven");
        loggerC.debug("default logger level");

        // then
        ListLineAppender appender = LoggerFactory.instance().appender(ListLineAppender.class);
        
        assertThat(appender.getLines())
            .containsOnly(
                "test info params one two",
                "test debug params ten eleven"
            );
    }

    @Test
    void throwExceptionWhenCanNotRecognizeAppender() throws Exception {
        // given
        Properties prop = new Properties();
        prop.put("defaultRoot", "INFO, java.lang.Integer");
        
        ConfigurationFileProvider provider = new ConfigurationFileProvider(prop);

        try {
            // when
            provider.create();
            fail("should throw exception");
        } catch (IllegalArgumentException e) {
            // then
            assertThat(e.getMessage())
                .startsWith("class java.lang.Integer is not assignable for");
        }
    }
    
    
}
