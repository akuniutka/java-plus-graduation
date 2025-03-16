package ru.practicum.ewm.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.client.EventClient;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.exception.ParameterValidationException;
import ru.practicum.ewm.subscription.dto.EventFilter;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.subscription.repository.SubscriptionRepository;
import ru.practicum.ewm.user.client.UserClient;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserClient userClient;
    private final EventClient eventClient;
    private final SubscriptionRepository repository;

    @Override
    public void subscribe(final long subscriberId, final long publisherId) {
        requireUserExist(subscriberId);
        requireUserExist(publisherId);
        if (subscriberId == publisherId) {
            throw new NotPossibleException("User cannot subscribe to himself");
        }
        if (repository.existsBySubscriberIdAndPublisherId(subscriberId, publisherId)) {
            throw new NotPossibleException("Subscription already exists");
        }
        Subscription subscription = new Subscription();
        subscription.setSubscriberId(subscriberId);
        subscription.setPublisherId(publisherId);
        subscription = repository.save(subscription);
        log.info("Added new subscription: id = {}, subscriberId = {}, publisherId = {}", subscription.getId(),
                subscription.getSubscriberId(), subscription.getPublisherId());
        log.debug("Subscription added = {}", subscription);
    }

    @Override
    public List<EventShortDto> getEvents(final long subscriberId, final EventFilter filter) {
        if (filter.rangeStart() != null && filter.rangeEnd() != null
                && filter.rangeEnd().isBefore(filter.rangeStart())) {
            throw new ParameterValidationException("rangeEnd", "must be after or equal to 'rangeStart'",
                    filter.rangeEnd());
        }
        requireUserExist(subscriberId);
        final List<Long> initiatorIds = repository.findPublisherIdsBySubscriberId(subscriberId);
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

    @Override
    public void unsubscribe(final long subscriberId, final long publisherId) {
        requireUserExist(subscriberId);
        requireUserExist(publisherId);
        if (subscriberId == publisherId) {
            throw new NotPossibleException("User cannot unsubscribe from himself");
        }
        final Subscription subscription = repository.findBySubscriberIdAndPublisherId(subscriberId, publisherId)
                .orElseThrow(() -> new NotFoundException(Subscription.class, Set.of(subscriberId, publisherId)));
        repository.delete(subscription);
        log.info("Deleted subscription: id = {}, subscriberId = {}, publisherId = {}", subscription.getId(),
                subscription.getSubscriberId(), subscription.getPublisherId());
    }

    private void requireUserExist(final long userId) {
        if (userClient.existsById(userId)) {
            return;
        }
        throw new NotFoundException("User", userId);
    }

    private InternalEventFilter.Sort mapSort(final EventFilter.Sort sort) {
        return switch (sort) {
            case EVENT_DATE -> InternalEventFilter.Sort.EVENT_DATE;
            case RATING -> InternalEventFilter.Sort.RATING;
            case null -> null;
        };
    }
}
