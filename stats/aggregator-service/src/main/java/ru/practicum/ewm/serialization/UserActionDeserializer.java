package ru.practicum.ewm.serialization;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.serialization.BaseAvroDeserializer;

public class UserActionDeserializer extends BaseAvroDeserializer<UserActionAvro> {

    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
