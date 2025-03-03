package ru.practicum.ewm.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.client.EventClient;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.exception.ParameterValidationException;
import ru.practicum.ewm.user.client.UserClient;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserClient userClient;
    private final SubscriptionRepository subscriptionRepository;
    private final EventClient eventClient;

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
        if (filter.rangeStart() != null && filter.rangeEnd() != null
                && filter.rangeEnd().isBefore(filter.rangeStart())) {
            throw new ParameterValidationException("rangeEnd", "must be after or equal to 'rangeStart'",
                    filter.rangeEnd());
        }
        requireUserExist(subscriberId);
        final List<Long> initiatorIds = subscriptionRepository.findTargetIdsBySubscriberId(subscriberId);
        if (initiatorIds.isEmpty()) {
            return List.of();
        }
        final InternalEventFilter internalFilter = InternalEventFilter.builder()
                .onlyPublished(true)
                .users(initiatorIds)
                .text(filter.text())
                .categories(filter.categories())
                .paid(filter.paid())
                .rangeStart(filter.rangeStart())
                .rangeEnd(filter.rangeEnd())
                .onlyAvailable(filter.onlyAvailable())
                .sort(mapSort(filter.sort()))
                .from(filter.from())
                .size(filter.size())
                .build();
        return eventClient.findAll(internalFilter);
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

    private InternalEventFilter.Sort mapSort(final EventFilter.Sort sort) {
        return switch (sort) {
            case EVENT_DATE -> InternalEventFilter.Sort.EVENT_DATE;
            case VIEWS -> InternalEventFilter.Sort.VIEWS;
            case null -> null;
        };
    }
}
