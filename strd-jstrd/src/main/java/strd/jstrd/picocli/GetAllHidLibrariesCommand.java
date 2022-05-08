package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.CliMessages;
import strd.jstrd.util.CliUtil;
import strd.jstrd.util.ServiceLoaderUtil;
import strd.lib.spi.hid.HidLibrary;

import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Prints all available HID libraries",
        usageHelpWidth = 120,
        name = "get-all-hid-libraries",
        aliases = "gahl")
public class GetAllHidLibrariesCommand extends GlobalCommandParent implements Callable<Integer> {

    @Override
    public Integer call() {
        Set<String> availableLibraries = ServiceLoaderUtil.getAvailableLibraries(HidLibrary.class);
        if (availableLibraries.isEmpty()) {
            CliMessages.printErrorUnableToFindAnyHidLibrary();
            return 1;
        } else {
            CliUtil.printList("All available HID libraries", availableLibraries);
            return 0;
        }
    }
}
