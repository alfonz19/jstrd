package strd.jstrd.util;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public class ServiceLoaderUtil {
    //hide me!
    private ServiceLoaderUtil() {
    }

    public static <T> Optional<T> getLibrary(Class<T> libraryInterface, Class<? extends T> specificClass) {
        return loadInstances(libraryInterface)
                .filter(e -> e.getClass().equals(specificClass))
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
        ServiceLoader<T> load = ServiceLoader.load(libraryInterface);
        return StreamSupport.stream(load.spliterator(), false);
    }

    private static <T> void assertInterfaceClass(Class<T> libraryInterface) {
        if (!libraryInterface.isInterface()) {
            throw new IllegalArgumentException("Parameter `libraryInterface` should be interface");
        }
    }
}
