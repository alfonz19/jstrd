package strd.jstrd.configuration;

import strd.jstrd.exception.InvalidSteamDeckConfigurationException;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ConfigurationParserTest {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationParserTest.class);

    private final ConfigurationParser underTest = new ConfigurationParser();

    @Test
    void testSampleSinglePageConfiguration() {
        InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/sample/sampleSinglePageConfiguration.json");
        StreamDeckConfiguration configuration = underTest.parse(is);
        assertThat(configuration, notNullValue());
    }

    @Test
    void testSampleMultiPageConfiguration() {
        InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/sample/multiPageConfiguration.json");
        StreamDeckConfiguration configuration = underTest.parse(is);
        assertThat(configuration, notNullValue());
    }

    //------------------------------------------------------------------------------------------------------------------

    @Test
    void testIncorrectSpecificationButtonsAndContaihersAtTheSameTime() throws IOException {
        try (InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/cannot-specify-both-buttons-and-containers.json")) {
            assertThrows(InvalidSteamDeckConfigurationException.class, () -> underTest.parse(is));
        }
    }

    @Test
    void testCannotHaveUnspecifiedLayout() throws IOException {
        InvalidSteamDeckConfigurationException thrown;
        try (InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/cannot-have-unspecified-layout.json")) {
            thrown = assertThrows(InvalidSteamDeckConfigurationException.class, () -> underTest.parse(is));
        }

        assertThat(thrown.getValidationResult().size(), is(1));
        assertThat(thrown.getValidationResult().iterator().next().getMessage(),
                is(StreamDeckConfiguration.DeviceConfiguration.LAYOUT_MUST_BE_SPECIFIED));
    }

    @Test
    void testCannotHaveUnspecifiedSerialNumber() throws IOException {
        try (InputStream is = StreamDeckConfiguration.class.getResourceAsStream("/cannot-have-unspecified-serial-number.json")) {
            InvalidSteamDeckConfigurationException thrown =
                    assertThrows(InvalidSteamDeckConfigurationException.class, () -> underTest.parse(is));

            if (!thrown.getValidationResult().isEmpty()) {
                log.debug("violations: {}", thrown.getValidationResult());
            }
            assertThat(thrown.getValidationResult().size(), is(1));
            assertThat(thrown.getValidationResult().iterator().next().getMessage(),
                    is(StreamDeckConfiguration.DeviceConfiguration.SERIAL_VERSION_MUST_BE_SET));
        }
    }


}
