package org.eventplanner.webapp.utils;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ObjectUtils {
    public static <T> @NonNull T orElse(@Nullable T  nullable, @NonNull T fallback) {
        if (nullable == null) {
            return fallback;
        }
        return nullable;
    }

    public static <T, E> @Nullable T mapNullable(@Nullable E  nullable, @NonNull Mapper<E, T> mapper) {
        if (nullable == null) {
            return null;
        }
        return mapper.map(nullable);
    }

    public static <T, E> @NonNull T mapNullable(@Nullable E  nullable, @NonNull Mapper<E, T> mapper, @NonNull T fallback) {
        if (nullable == null) {
            return fallback;
        }
        return mapper.map(nullable);
    }

    public static <T, E> @Nullable List<T> mapNullable(@Nullable List<E> nullable, Mapper<E, T> mapper) {
        if (nullable == null) {
            return null;
        }
        return nullable.stream().map(mapper::map).toList();
    }

    public static <T, E> @NonNull List<T> mapNullable(@Nullable List<E> nullable, @NonNull Mapper<E, T> mapper, @NonNull List<T> fallback) {
        if (nullable == null) {
            return fallback;
        }
        return nullable.stream().map(mapper::map).toList();
    }

    public interface Mapper<I, O> {
        O map(I i);
    }
}
