package org.eventplanner.webapp.importer;

import org.eventplanner.webapp.config.Permission;
import org.eventplanner.webapp.config.SignedInUser;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class ImporterService {
    public void importFile(@NonNull SignedInUser signedInUser, InputStream stream, String filename) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);
        signedInUser.assertHasPermission(Permission.WRITE_USERS);

        File targetFile = new File("/tmp/eventplanner/data/" + filename);
        targetFile.mkdirs();
        try {
            Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
