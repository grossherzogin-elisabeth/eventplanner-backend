package org.eventplanner.webapp.positions;

import org.eventplanner.webapp.positions.models.Position;

import java.util.List;

public interface PositionRepository {
    List<Position> findAll();
}
