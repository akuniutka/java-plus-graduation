package ru.practicum.ewm.collector.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import ru.practicum.ewm.collector.message.ActionTypeProto;
import ru.practicum.ewm.collector.message.UserActionProto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Mapper
public interface UserActionMapper {

    UserActionAvro mapToAvro(UserActionProto userActionProto);

    default ActionTypeAvro mapActionType(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new AssertionError();
        };
    }

    default Instant mapTimestamp(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
