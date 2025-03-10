package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select h.app as app, h.uri as uri, count(h.ip) as hits from EndpointHit h"
            + " where h.timestamp between :start and :end group by h.app, h.uri"
            + " order by hits desc")
    List<ViewStats> getHits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select h.app as app, h.uri as uri, count(h.ip) as hits from EndpointHit h"
            + " where h.uri in (:uris) and h.timestamp between :start and :end group by h.app, h.uri"
            + " order by hits desc")
    List<ViewStats> getHits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query("select h.app as app, h.uri as uri, count(distinct(h.ip)) as hits from EndpointHit h"
            + " where h.timestamp between :start and :end group by h.app, h.uri"
            + " order by hits desc")
    List<ViewStats> getUniqueHits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select h.app as app, h.uri as uri, count(distinct(h.ip)) as hits from EndpointHit h"
            + " where h.uri in (:uris) and h.timestamp between :start and :end group by h.app, h.uri"
            + " order by hits desc")
    List<ViewStats> getUniqueHits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);
}
