package ru.practicum.ewm.aggregator.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.model.UserScore;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.model.ActionTypeWeight;

@Component
public class UserActionMapperImpl implements UserActionMapper {

    @Override
    public UserScore mapToUserScore(final UserActionAvro userActionAvro) {
        return UserScore.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .score(ActionTypeWeight.from(userActionAvro.getActionType()))
                .timestamp(userActionAvro.getTimestamp())
                .build();
    }
}
