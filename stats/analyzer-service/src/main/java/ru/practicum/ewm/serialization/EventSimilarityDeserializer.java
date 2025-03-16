package ru.practicum.ewm.serialization;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.serialization.BaseAvroDeserializer;

public class EventSimilarityDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {

    public EventSimilarityDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
