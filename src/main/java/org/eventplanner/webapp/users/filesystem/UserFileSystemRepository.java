package org.eventplanner.webapp.users.filesystem;

import org.eventplanner.webapp.users.UserRepository;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.eventplanner.webapp.utils.Crypto;
import org.eventplanner.webapp.utils.FileSystemJsonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// TODO encrypt user data
@Repository
public class UserFileSystemRepository implements UserRepository {

    private final FileSystemJsonRepository<UserDetailsJsonEntity> fs;
    private final Crypto crypto;

    public UserFileSystemRepository(
        @Value("${custom.data-directory}") String dataDirectory,
        @Value("${custom.data-encryption-password}") String password
    ) {
        var directory = new File(dataDirectory + "/users");
        this.fs = new FileSystemJsonRepository<>(UserDetailsJsonEntity.class, directory);
        this.crypto = new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", password);
    }

    @Override
    public @NonNull List<UserDetails> findAll() {
        return fs.findAll().stream()
            .map((json) -> json.toDomain(crypto))
            .sorted(Comparator.comparing(UserDetails::fullName))
            .toList();
    }

    @Override
    public @NonNull Optional<UserDetails> findByKey(@NonNull UserKey key) {
        return fs.findByKey(key.value())
            .map((json) -> json.toDomain(crypto));
    }

    @Override
    public @NonNull Optional<UserDetails> findByAuthKey(@Nullable AuthKey key) {
        // TODO every inefficient
        if (key == null) {
            return Optional.empty();
        }
        var all = findAll();
        return all.stream().filter(it -> key.equals(it.authKey())).findFirst();
    }

    @Override
    public @NonNull Optional<UserDetails> findByEmail(@Nullable String email) {
        if (email == null) {
            return Optional.empty();
        }
        var all = findAll();
        return all.stream().filter(it -> email.equals(it.email())).findFirst();
    }

    @Override
    public @NonNull Optional<UserDetails> findByName(@NonNull String firstName, @NonNull String lastName) {
        var all = findAll();
        return all.stream()
            .filter(it -> it.lastName().equalsIgnoreCase(lastName))
            .filter(it -> it.firstName().equalsIgnoreCase(firstName)
                || (it.secondName() != null && (it.firstName() + " " + it.secondName()).equalsIgnoreCase(firstName)))
            .findFirst();
    }

    @Override
    public @NonNull UserDetails create(@NonNull UserDetails user) {
        fs.save(user.key().value(), UserDetailsJsonEntity.fromDomain(user, crypto));
        return user;
    }

    @Override
    public @NonNull UserDetails update(@NonNull UserDetails user) {
        fs.save(user.key().value(), UserDetailsJsonEntity.fromDomain(user, crypto));
        return user;
    }

    @Override
    public void deleteAll() {
        fs.deleteAll();
    }
}