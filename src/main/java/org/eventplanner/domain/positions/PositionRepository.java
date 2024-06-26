package org.eventplanner.domain.positions;

import org.eventplanner.domain.positions.models.Position;
import org.eventplanner.domain.positions.models.Position;

import java.util.List;

public interface PositionRepository {
    List<Position> findAll();
}
