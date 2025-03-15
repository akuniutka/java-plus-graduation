package ru.practicum.ewm.aggregator.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.model.UserAction;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
public class UserActionMapperImpl implements UserActionMapper {

    @Override
    public UserAction mapToUserAction(final UserActionAvro userActionAvro) {
        return UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .actionType(mapActionType(userActionAvro.getActionType()))
                .timestamp(userActionAvro.getTimestamp())
                .build();
    }

    private UserAction.ActionType mapActionType(ActionTypeAvro actionTypeAvro) {
        return switch (actionTypeAvro) {
            case VIEW -> UserAction.ActionType.VIEW;
            case REGISTER -> UserAction.ActionType.REGISTER;
            case LIKE -> UserAction.ActionType.LIKE;
        };
    }
}
