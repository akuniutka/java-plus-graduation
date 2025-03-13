package ru.practicum.ewm.collector.mapper;

import ru.practicum.ewm.collector.message.UserActionProto;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionMapper {

    UserActionAvro mapToAvro(UserActionProto userActionProto);
}
