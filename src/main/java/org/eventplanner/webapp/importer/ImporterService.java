package org.eventplanner.webapp.importer;

import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class ImporterService {
    public void importFile(InputStream stream, String filename) {
        File targetFile = new File("/tmp/eventplanner/data/" + filename);
        targetFile.mkdirs();
        try {
            Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
