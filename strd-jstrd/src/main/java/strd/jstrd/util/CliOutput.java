package strd.jstrd.util;

import java.util.Collection;

interface CliOutput {
    void printError(String s);

    void printSuccess(String message);

    void printWarning(String message);

    void printList(String title, Collection<String> items);

    void printException(Throwable ex);
}
