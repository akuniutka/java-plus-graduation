package ru.practicum.ewm.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterIdAndEventIdAndStatusNotLike(long userId, long eventId, RequestState status);

    List<Request> findAllByRequesterId(long userId);

    List<Request> findAllByEventId(long eventId);

    List<Request> findAllByEventIdAndIdIn(long eventId, List<Long> ids);

    List<Request> findAllByEventIdAndStatus(long eventId, RequestState status);

    @Query("select r.eventId as id, count(r.id) as requests from Request r "
            + "where r.eventId in :ids and r.status = :status group by r.eventId")
    List<RequestStats> getRequestStats(@Param("ids") Set<Long> ids, @Param("status") RequestState status);

    interface RequestStats {

        long getId();

        long getRequests();
    }
}
