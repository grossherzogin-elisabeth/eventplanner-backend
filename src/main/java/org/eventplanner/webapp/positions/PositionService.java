package org.eventplanner.webapp.positions;

import org.eventplanner.webapp.positions.models.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(@Autowired PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getPosition() {
        return this.positionRepository.findAll();
    }
}
