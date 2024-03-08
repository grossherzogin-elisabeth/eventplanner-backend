package org.eventplanner.webapp.users.filesystem;

import org.eventplanner.webapp.users.UserRepository;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.eventplanner.webapp.utils.FileSystemRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.Optional;

// TODO encrypt user data
@Repository
public class UserFileSystemRepository extends FileSystemRepository<UserDetails,  UserJsonEntity> implements UserRepository {

    public UserFileSystemRepository() {
        super(UserJsonEntity.class, new File("/tmp/eventplanner/data/users"));
    }

    @Override
    public Optional<UserDetails> findByKey(UserKey key) {
        return findByKey(key.value());
    }

    @Override
    public @NonNull Optional<UserDetails> findByAuthKey(@NonNull AuthKey key) {
        // TODO every inefficient
        var all = findAll();
        return all.stream().filter(it -> key.equals(it.authKey())).findFirst();
    }

    @Override
    public String getKey(UserDetails domain) {
        return domain.key().value();
    }

    @Override
    public UserJsonEntity mapToEntity(UserDetails domain) {
        return UserJsonEntity.fromDomain(domain);
    }

    @Override
    public UserDetails mapToDomain(UserJsonEntity entity) {
        return entity.toDomain();
    }
}
