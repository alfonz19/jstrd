package strd.jstrd.util;

import java.util.Collection;

interface CliOutput {
    void printError(String s);

    void printSuccess(String message);

    void printWarning(String message);

    void printList(String title, Collection<String> items);

    //TODO MMUCHA: support for whole stack.
    void printException(Exception ex);
}
