package ru.practicum.ewm.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.model.UserScore;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.model.ActionTypeWeight;

@Component
public class UserActionMapperImpl implements UserActionMapper {

    @Override
    public UserScore mapToUserScore(final UserActionAvro action) {
        if (action == null) {
            return null;
        }
        final UserScore score = new UserScore();
        score.setEventId(action.getEventId());
        score.setUserId(action.getUserId());
        score.setScore(ActionTypeWeight.from(action.getActionType()));
        score.setTimestamp(action.getTimestamp());
        return score;
    }
}
