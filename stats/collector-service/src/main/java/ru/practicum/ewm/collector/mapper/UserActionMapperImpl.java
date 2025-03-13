package ru.practicum.ewm.collector.mapper;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.collector.message.ActionTypeProto;
import ru.practicum.ewm.collector.message.UserActionProto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Component
public class UserActionMapperImpl implements UserActionMapper {

    @Override
    public UserActionAvro mapToAvro(final UserActionProto userActionProto) {
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(mapActionType(userActionProto.getActionType()))
                .setTimestamp(mapTimestamp(userActionProto.getTimestamp()))
                .build();
    }

    private ActionTypeAvro mapActionType(final ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new AssertionError();
        };
    }

    private Instant mapTimestamp(final Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
