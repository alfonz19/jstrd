package strd.jstrd.util;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ServiceLoaderUtil {
    //hide me!
    private ServiceLoaderUtil() {
    }

    public static <T> Optional<T> getService(Class<T> serviceInterface, String specificClassName) {
        Predicate<T> specificNameFilter = e -> e.getClass().getName().equals(specificClassName);
        return getService(serviceInterface, specificNameFilter);
    }

    public static <T> Optional<T> getService(Class<T> serviceInterface, Predicate<T> predicate) {
        return loadInstances(serviceInterface)
                .filter(predicate)
                .findFirst();
    }

    public static <T> Optional<T> getService(Class<T> serviceInterface) {
        return loadInstances(serviceInterface).findFirst();
    }

    public static <T> Set<String> getAvailableServices(Class<T> serviceInterface) {
        return loadInstances(serviceInterface).map(e -> e.getClass().getName()).collect(Collectors.toSet());
    }

    public static <T> Stream<T> loadInstances(Class<T> serviceInterface) {
        assertInterfaceClass(serviceInterface);
        return ServiceLoader.load(serviceInterface).
                stream()
                .map(ServiceLoader.Provider::get);
    }

    private static <T> void assertInterfaceClass(Class<T> serviceInterface) {
        if (!serviceInterface.isInterface()) {
            throw new IllegalArgumentException("Parameter `serviceInterface` should be interface");
        }
    }
}
