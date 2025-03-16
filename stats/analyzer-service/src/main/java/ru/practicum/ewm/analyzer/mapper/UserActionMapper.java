package ru.practicum.ewm.analyzer.mapper;

import ru.practicum.ewm.analyzer.model.UserScore;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionMapper {

    UserScore mapToUserScore(UserActionAvro action);
}
