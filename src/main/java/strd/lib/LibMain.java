package strd.lib;

import reactor.core.publisher.Flux;
import strd.lib.hid.HidLibrary;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibMain {
    private static final Logger log = LoggerFactory.getLogger(LibMain.class);

    public static void main(String[] args) {
        new LibMain().run();
    }

    private void run() {
        ServiceLoader<HidLibrary> load = ServiceLoader.load(HidLibrary.class);
        StreamSupport.stream(load.spliterator(), false)
                .findFirst()
                .ifPresentOrElse(this::run, errorMessage("No HID library found, Unable to proceed."));
    }

    private void run(HidLibrary hidLibrary) {
        StreamDeckFactory factory = new StreamDeckFactory(hidLibrary);
        List<HidLibrary.StreamDeckInfo> streamDeckDevices = factory.findStreamDeckDevices();


        String foundStreamDeckDevicesString = streamDeckDevices.stream()
                .map(HidLibrary.StreamDeckInfo::toString)
                .collect(Collectors.joining("\n",
                        "-----All found streamdeck devices-----\n",
                        "--------------------------------------\n"));
        System.out.println(foundStreamDeckDevicesString);


        streamDeckDevices.stream()
                .filter(e -> e.getSerialNumberString().equals("DL49K1A69132"))
                .findFirst()
                .ifPresentOrElse(streamDeckInfo -> testRunMyStreamDeck(factory, streamDeckInfo),
                        errorMessage("Unable to find selected streamdeck."));
    }

    private void testRunMyStreamDeck(StreamDeckFactory factory, HidLibrary.StreamDeckInfo streamDeckInfo) {
        WaitUntilNotTerminated waitUntilNotTerminated = new WaitUntilNotTerminated(250);

        try (StreamDeck streamDeck = factory.openConnection(streamDeckInfo)) {
            streamDeck.addButtonsStateUpdatedListener(new StreamDeck.ButtonStateListener.Adapter() {
                @Override
                public void buttonStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo,
                                               int buttonIndex,
                                               boolean buttonState) {
                    log.info("Button {} {}", buttonIndex, buttonState ? "pressed" : "released");
                    if (buttonIndex == streamDeck.getKeyCount() - 1 && buttonState) {
                        waitUntilNotTerminated.terminate();
                    } else if (buttonIndex == streamDeck.getKeyCount() - 2 && buttonState) {
                        streamDeck.resetDevice();
                    } else if (buttonIndex >= 5 && buttonIndex <= 9) {
                        streamDeck.setBrightness((buttonIndex - 4)*10);
                    } else if (buttonIndex == 10) {
                        streamDeck.screenOff();
                    } else if (buttonIndex == 11) {
                        streamDeck.screenOn();
                    } else if (buttonIndex == 12) {
                        streamDeck.setBrightness(100);
                    }
                }
            });

            System.out.println("StreamDeck serial version(A): "+streamDeck.getStreamDeckInfo().getSerialNumberString());
            System.out.println("StreamDeck serial version(B): "+streamDeck.getSerialNumber());

            waitUntilNotTerminated.start();
        }
    }

    private Runnable errorMessage(String message) {
        return () -> System.err.println(message);
    }



    public Consumer<List<Integer>> consumer;

    public Flux<Integer> createNumberSequence() {
        return Flux.create(sink -> LibMain.this.consumer = items -> items.forEach(sink::next));
    }
}
