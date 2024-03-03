package org.eventplanner.webapp.positions;

import org.eventplanner.webapp.config.Permission;
import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.exceptions.NotImplementedException;
import org.eventplanner.webapp.positions.models.Position;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(@Autowired PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getPosition(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_POSITIONS);

        return this.positionRepository.findAll();
    }

    public Position createPosition(@NonNull SignedInUser signedInUser, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        throw new NotImplementedException("Positions are still hard coded in this version");
    }

    public Position updatePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        throw new NotImplementedException("Positions are still hard coded in this version");
    }

    public void deletePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        throw new NotImplementedException("Positions are still hard coded in this version");
    }
}
