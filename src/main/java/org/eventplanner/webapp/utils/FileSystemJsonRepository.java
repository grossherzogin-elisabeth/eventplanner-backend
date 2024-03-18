package org.eventplanner.webapp.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FileSystemJsonRepository<E> {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String directory;
    private final Class<E> entityClass;

    public FileSystemJsonRepository(Class<E> e, File directory) {
        this.entityClass = e;
        this.directory = directory.getPath();
    }

    public @NonNull List<E> findAll() {
        var dir = new File(directory);
        return readAllFromDirectory(dir);
    }

    public @NonNull Optional<E> findByKey(@NonNull String key) {
        var file = new File(directory + "/"  + key + ".json");
        return readFromFile(file);
    }

    public @NonNull E save(@NonNull String key, @NonNull E entity) {
        var file = new File(directory + "/" + key  + ".json");
        writeToFile(file, entity);
        return entity;
    }

    public void deleteByKey(@NonNull String key) {
        var file = new File(directory + "/"  + key + ".json");
        if (!file.exists()) {
            return;
        }
        if (!file.delete()) {
            log.warn("Failed to delete file " + file.getPath());
        }
    }

    public void deleteAll() {
        var dir = new File(directory);
        deleteAllInDirectory(dir);
    }

    public void deleteAllInDirectory(@NonNull File dir) {
        if (!dir.exists()) {
            return;
        }
        var files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (!file.delete()) {
                log.warn("Failed to delete file " + file.getPath());
            }
        }
    }

    public @NonNull List<E> readAllFromDirectory(@NonNull File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }
        var files = dir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        var entities = new ArrayList<E>();
        for (File file : files) {
            if (file.isDirectory()) {
                entities.addAll(readAllFromDirectory(file));
            } else {
                readFromFile(file).ifPresent(entities::add);
            }
        }
        return entities;
    }

    public @NonNull Optional<E> readFromFile(@NonNull File file) {
        if (!file.exists()) {
            return Optional.empty();
        }
        try(InputStream in = new FileInputStream(file)) {
            var reader = new InputStreamReader(in);
            var entity = gson.fromJson(reader, entityClass);
            return Optional.of(entity);
        } catch (Exception e) {
            log.error("Failed to read user from json file", e);
        }
        return Optional.empty();
    }

    public void writeToFile(@NonNull File file, @NonNull E entity) {
        new File(file.getParent()).mkdirs();
        var json = gson.toJson(entity);
        try {
            Files.writeString(file.toPath(), json);
        } catch (IOException e) {
            log.error("Failed to write event to file");
        }
    }
}
