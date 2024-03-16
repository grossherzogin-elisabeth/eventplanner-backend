package org.eventplanner.webapp.positions.filesystem;

import org.apache.commons.io.FileUtils;
import org.eventplanner.webapp.positions.PositionRepository;
import org.eventplanner.webapp.positions.models.Position;
import org.eventplanner.webapp.utils.FileSystemJsonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Repository
public class PositionFileSystemRepository implements PositionRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final FileSystemJsonRepository<PositionJsonEntity> fs;

    public PositionFileSystemRepository(@Value("${custom.data-directory}") String dataDirectory, ResourceLoader resourceLoader) {
        var dir = new File(dataDirectory + "/positions");
        if (!dir.exists()) {
            try {
                var resource = resourceLoader.getResource("classpath:data/positions");
                if (resource.exists()) {
                    var sourceDir = resource.getFile();
                    FileUtils.copyDirectory(sourceDir, dir);
                }
            } catch (IOException e) {
                log.error("Failed to copy bundled positions into data directory", e);
            }
        }
        this.fs = new FileSystemJsonRepository<>(PositionJsonEntity.class, dir);
    }

    @Override
    public List<Position> findAll() {
        return fs.findAll().stream().map(PositionJsonEntity::toDomain).toList();
    }
}
