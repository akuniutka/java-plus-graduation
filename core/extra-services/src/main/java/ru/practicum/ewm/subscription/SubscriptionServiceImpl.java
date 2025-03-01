package ru.practicum.ewm.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.event.*;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserClient userClient;
    private final SubscriptionRepository subscriptionRepository;
    private final EventService eventService;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public void subscribe(long subscriberId, long targetId) {
        if (subscriberId == targetId) {
            throw new NotPossibleException("User cannot subscribe to himself");
        }
        if (subscriptionRepository.existsBySubscriberIdAndTargetId(subscriberId, targetId)) {
            throw new NotPossibleException("Subscription already exists");
        }
        requireUserExist(subscriberId);
        requireUserExist(targetId);
        final Subscription subscription = new Subscription();
        subscription.setSubscriberId(subscriberId);
        subscription.setTargetId(targetId);
        subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEvents(long subscriberId, EventFilter filter) {
        requireUserExist(subscriberId);
        final List<Long> initiatorIds = subscriptionRepository.findTargetIdsBySubscriberId(subscriberId);
        final EventFilter filterWithInitiators = filter.toBuilder().users(initiatorIds).build();
        final List<Event> events = eventService.get(filterWithInitiators);
        return eventMapper.mapToDto(events);
    }

    @Transactional
    @Override
    public void unsubscribe(long subscriberId, long targetId) {
        if (subscriberId == targetId) {
            throw new NotPossibleException("User cannot unsubscribe from himself");
        }
        requireUserExist(subscriberId);
        requireUserExist(targetId);
        final Subscription subscription = subscriptionRepository.findBySubscriberIdAndTargetId(subscriberId, targetId)
                .orElseThrow(() -> new NotFoundException(Subscription.class, Set.of(subscriberId, targetId)));
        subscriptionRepository.delete(subscription);
    }

    private void requireUserExist(final long userId) {
        final UserShortDto user = userClient.getById(userId);
        if (user.name() == null) {
            throw new NotFoundException("User", userId);
        }
    }
}
