package ru.practicum.ewm.aggregator.mapper;

import ru.practicum.ewm.aggregator.model.UserScore;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionMapper {

    UserScore mapToUserScore(UserActionAvro userActionAvro);
}
