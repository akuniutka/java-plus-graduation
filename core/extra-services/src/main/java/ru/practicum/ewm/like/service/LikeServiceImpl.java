package ru.practicum.ewm.like.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.collector.message.ActionTypeProto;
import ru.practicum.ewm.collector.message.UserActionProto;
import ru.practicum.ewm.collector.service.UserActionControllerGrpc;
import ru.practicum.ewm.event.client.EventClient;
import ru.practicum.ewm.event.dto.EventCondensedDto;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ParameterValidationException;
import ru.practicum.ewm.request.client.RequestClient;
import ru.practicum.ewm.user.client.UserClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {

    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;
    private final Clock clock;

    @GrpcClient("collector-service")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    @Override
    public void add(final long eventId, final long userId) {
        final EventCondensedDto event = getEventById(eventId);
        requireUserExist(userId);
        if (event.state() != EventState.PUBLISHED || event.eventDate().isAfter(LocalDateTime.now(clock))) {
            throw new ParameterValidationException("eventId", "event should be started", eventId);
        }
        if (!requestClient.existsByRequesterIdAndStatusConfirmed(userId)) {
            throw new ParameterValidationException("userId", "user should be participant of event",userId);
        }
        sendLikeToCollector(userId, eventId);
    }

    private EventCondensedDto getEventById(final long eventId) {
        return eventClient.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event", eventId)
        );
    }

    private void requireUserExist(final long userId) {
        if (userClient.existsById(userId)) {
            return;
        }
        throw new NotFoundException("User", userId);
    }

    private void sendLikeToCollector(final long userId, final long eventId) {
        final Instant now = Instant.now(clock);
        final UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_LIKE)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build();
        collectorClient.collectUserAction(userActionProto);
        log.info("Sent user action to collector service: userId = {}, eventId = {}, actionType ={}",
                userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType());
        log.debug("Sent action = {}", userActionProto);
    }
}
