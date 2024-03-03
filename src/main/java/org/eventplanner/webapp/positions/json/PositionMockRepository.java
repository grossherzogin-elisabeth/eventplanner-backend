package org.eventplanner.webapp.positions.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.eventplanner.webapp.positions.PositionRepository;
import org.eventplanner.webapp.positions.models.Position;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class PositionMockRepository implements PositionRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ResourceLoader resourceLoader;
    private final Gson gson = new GsonBuilder().create();

    public PositionMockRepository(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public List<Position> findAll() {
        var resource = this.resourceLoader.getResource("classpath:data/positions.json");
        try(InputStream in = resource.getInputStream()) {
            var reader = new InputStreamReader(in);
            var type = TypeToken.getParameterized(ArrayList.class, PositionJsonEntity.class).getType();
            var json = (List<PositionJsonEntity>) gson.fromJson(reader, type);
            return json.stream()
                    .map(it -> new Position(
                            new PositionKey(it.key()),
                            it.name(),
                            it.color()
                    ))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to read positions from json file", e);
        }
        return Collections.emptyList();
    }
}
