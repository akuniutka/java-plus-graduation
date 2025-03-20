package ru.practicum.ewm.aggregator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.aggregator.model.UserScore;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.model.ActionTypeWeight;

@Mapper
public interface UserActionMapper {

    @Mapping(target = "score", source = "actionType")
    UserScore mapToUserScore(UserActionAvro userActionAvro);

    default float mapActionTypeToScore(ActionTypeAvro actionType) {
        return ActionTypeWeight.from(actionType);
    }
}
