package strd.jstrd.util;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public class ServiceLoaderUtil {
    //hide me!
    private ServiceLoaderUtil() {
    }

    //TODO MMUCHA: rename to getService, all methods here.
    public static <T> Optional<T> getLibrary(Class<T> libraryInterface, String specificClassName) {
        Predicate<T> specificNameFilter = e -> e.getClass().getName().equals(specificClassName);
        return getLibrary(libraryInterface, specificNameFilter);
    }

    public static <T> Optional<T> getLibrary(Class<T> libraryInterface, Predicate<T> predicate) {
        return loadInstances(libraryInterface)
                .filter(predicate)
                .findFirst();
    }

    public static <T> Optional<T> getLibrary(Class<T> libraryInterface) {
        return loadInstances(libraryInterface).findFirst();
    }

    public static <T> Set<String> getAvailableLibraries(Class<T> libraryInterface) {
        return loadInstances(libraryInterface).map(e -> e.getClass().getName()).collect(Collectors.toSet());
    }

    private static <T> Stream<T> loadInstances(Class<T> libraryInterface) {
        assertInterfaceClass(libraryInterface);
        return ServiceLoader.load(libraryInterface).
                stream()
                .map(ServiceLoader.Provider::get);
    }

    private static <T> void assertInterfaceClass(Class<T> libraryInterface) {
        if (!libraryInterface.isInterface()) {
            throw new IllegalArgumentException("Parameter `libraryInterface` should be interface");
        }
    }
}
