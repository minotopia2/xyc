package io.github.xxyy.common.sql.builder;

import com.google.common.collect.Lists;
import io.github.xxyy.common.sql.builder.annotation.SqlValueCache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Provides static utility methods for {@link io.github.xxyy.common.sql.builder.SqlValueHolder} and implementations.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29.3.14
 */
public class SqlHolders {
    /**
     * Adds all annotated SqlHolders of a class to a set and populates the fields with the
     * {@link io.github.xxyy.common.sql.builder.SqlValueHolder} implementation corresponding to the annotated {@link io.github.xxyy.common.sql.builder.annotation.SqlValueCache#type()}.
     *
     * @param clazz            Class to process
     * @param accessorInstance Instance of clazz used to access and set fields.
     *                         May be null if, and only if all {@link io.github.xxyy.common.sql.builder.annotation.SqlValueCache} annotated fields are static.
     * @param dataSource       Optional DataSource that can be used by SqlValueHolders to acquire data.
     * @return SqlHolders of {@code clazz}.
     * @throws java.lang.NullPointerException   If an {@link io.github.xxyy.common.sql.builder.annotation.SqlValueCache} annotated field is encountered and {@code accessorInstance} is null.
     * @throws java.lang.IllegalAccessException If an annotated field could not be accessed.
     * @see #processClassStructure(Class)
     * @deprecated Use {@link io.github.xxyy.common.sql.builder.SqlHolders.CacheBuilder}.
     */
    @NonNull
    @Deprecated
    public static Set<SqlValueHolder<?>> processClass(@NonNull Class<?> clazz, @Nullable Object accessorInstance,
                                                      @Nullable SqlValueHolder.DataSource dataSource) throws IllegalAccessException {
        Set<SqlValueHolder<?>> result = new LinkedHashSet<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(SqlValueCache.class)) {
                SqlValueCache annotation = field.getAnnotation(SqlValueCache.class);

                if (accessorInstance == null && !Modifier.isStatic(field.getModifiers())) {
                    throw new NullPointerException(String.format("Encountered a non-static field marked for processing, but no accessor instance given! (At field %s)", field.getName()));
                }

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                result.add(annotation.type().createHolder(field, annotation, accessorInstance, dataSource));
            }
        }

        return result;
    }

    /**
     * Discovers {@link io.github.xxyy.common.sql.builder.annotation.SqlValueCache} annotated class members and saves them to a Set.
     *
     * @param clazz Class to process
     * @return {@link io.github.xxyy.common.sql.builder.annotation.SqlValueCache}s of {@code clazz}.
     */
    @NonNull
    public static CacheBuilder processClassStructure(@NonNull Class<?> clazz) {
        CacheBuilder builder = new CacheBuilder();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(SqlValueCache.class)) {
                builder.put(field);
            }
        }

        return builder;
    }

    public static void updateFromResultSet(@NonNull Collection<SqlValueHolder<?>> holders, @NonNull ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        List<String> availableNames = Lists.newLinkedList();
        int columnCount = rsmd.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            availableNames.add(rsmd.getColumnName(i));
        }

        for (SqlValueHolder holder : holders) { //Seems like the only solution that supports wildcards
            if (holder != null && availableNames.contains(holder.getColumnName()) && holder.supportsOverride()) {
                holder.processResultSet(resultSet);
            }
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class CacheBuilder {
        private Map<Field, SqlValueCache> targetFields;

        protected void put(@NonNull Field field) {
            if (targetFields == null) {
                targetFields = new HashMap<>();
            }

            SqlValueCache annotation = field.getAnnotation(SqlValueCache.class);
            assert annotation != null;

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            this.targetFields.put(field, annotation);
        }

        @NonNull
        public List<SqlValueHolder<?>> build(@Nullable Object accessorInstance, @Nullable SqlValueHolder.DataSource dataSource) throws IllegalAccessException {
            List<SqlValueHolder<?>> result = new ArrayList<>(targetFields.size());

            for (Map.Entry<Field, SqlValueCache> entry : targetFields.entrySet()) {
                SqlValueCache annotation = entry.getValue();
                Field field = entry.getKey();

                if (accessorInstance == null && !Modifier.isStatic(field.getModifiers())) {
                    throw new NullPointerException(String.format("Encountered a non-static field marked for processing, but no accessor instance given! (At field %s)", field.getName()));
                }

                result.add(annotation.type().createHolder(field, annotation, accessorInstance, dataSource));
            }

            return result;
        }
    }
}
