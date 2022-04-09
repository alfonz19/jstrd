package strd.jstrd.configuration;

import strd.jstrd.exception.InvalidSteamDeckConfigurationException;

import java.io.InputStream;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ConfigurationParserTest {

    private final ConfigurationParser underTest = new ConfigurationParser();

    @Test
    public void testValidConfigurationHavingSinglePage() {
        InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/singlePageConfiguration.json");
        StreamDeckConfiguration configuration = underTest.parse(is);
        MatcherAssert.assertThat(configuration, Matchers.notNullValue());
    }

    @Test
    public void testValidConfigurationHavingMultiplePages() {
        InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/multiPageConfiguration.json");
        StreamDeckConfiguration configuration = underTest.parse(is);
        MatcherAssert.assertThat(configuration, Matchers.notNullValue());
    }

    @Test
    public void testA() {
        Assertions.assertThrows(InvalidSteamDeckConfigurationException.class, ()-> {
            InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/incorrectPageConfiguration.json");
            underTest.parse(is);
        });
    }
}
