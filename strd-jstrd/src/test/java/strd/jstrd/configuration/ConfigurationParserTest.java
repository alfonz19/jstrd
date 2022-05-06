package strd.jstrd.configuration;

import strd.jstrd.exception.InvalidSteamDeckConfigurationException;

import javax.validation.ConstraintViolation;
import java.io.InputStream;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ConfigurationParserTest {

    private final ConfigurationParser underTest = new ConfigurationParser();

    @Test
    public void testValidConfigurationHavingSinglePage() {
        InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/singlePageConfiguration.json");
        StreamDeckConfiguration configuration = underTest.parse(is);
        assertThat(configuration, Matchers.notNullValue());
    }

    @Test
    public void testValidConfigurationHavingMultiplePages() {
        InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/multiPageConfiguration.json");
        StreamDeckConfiguration configuration = underTest.parse(is);
        assertThat(configuration, Matchers.notNullValue());
    }

    @Test
    public void testIncorrectSpecificationButtonsAndContaihersAtTheSameTime() {
        assertThrows(InvalidSteamDeckConfigurationException.class, ()-> {
            InputStream is = StreamDeckConfiguration.class.getResourceAsStream(
                    "/cannot-specify-both-buttons-and-containers.json");
            underTest.parse(is);
        });
    }

    @Test
    public void testCannotHaveUnspecifiedLayout() {
        InvalidSteamDeckConfigurationException thrown = assertThrows(InvalidSteamDeckConfigurationException.class,
                () -> {
                    InputStream is = StreamDeckConfiguration.class.getResourceAsStream(
                            "/cannot-have-unspecified-layout.json");
                    underTest.parse(is);
                });

        assertThat(thrown.getValidationResult().size(), is(1));
        assertThat(thrown.getValidationResult().iterator().next().getMessage(),
                is(StreamDeckConfiguration.DeviceConfiguration.LAYOUT_MUST_BE_SPECIFIED));
    }
    @Test
    public void testCannotHaveUnspecifiedSerialNumber() {
        InvalidSteamDeckConfigurationException thrown = assertThrows(InvalidSteamDeckConfigurationException.class,
                () -> {
                    InputStream is = StreamDeckConfiguration.class.getResourceAsStream(
                            "/cannot-have-unspecified-serial-number.json");
                    underTest.parse(is);
                });

        assertThat(thrown.getValidationResult().size(), is(1));
        assertThat(thrown.getValidationResult().iterator().next().getMessage(),
                is(StreamDeckConfiguration.DeviceConfiguration.LAYOUT_MUST_BE_SPECIFIED));
    }
}
