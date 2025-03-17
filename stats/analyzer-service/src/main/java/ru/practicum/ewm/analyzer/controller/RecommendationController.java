package ru.practicum.ewm.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.analyzer.mapper.RecommendedEventMapper;
import ru.practicum.ewm.analyzer.message.InteractionsCountRequestProto;
import ru.practicum.ewm.analyzer.message.RecommendedEventProto;
import ru.practicum.ewm.analyzer.message.SimilarEventsRequestProto;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.service.RecommendationsControllerGrpc;
import ru.practicum.ewm.analyzer.service.SimilarityScoreService;
import ru.practicum.ewm.analyzer.service.UserScoreService;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final UserScoreService userScoreService;
    private final SimilarityScoreService similarityScoreService;
    private final RecommendedEventMapper mapper;

    @Override
    public void getSimilarEvents(
            final SimilarEventsRequestProto request,
            final StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            log.info("Received request for new similar events: userId = {}, eventId = {}, maxResults = {}",
                    request.getUserId(), request.getEventId(), request.getMaxResults());
            log.debug("Similar events request = {}", request);
            final List<RecommendedEvent> events = similarityScoreService.findNewSimilarEvents(request.getUserId(),
                    request.getEventId(), request.getMaxResults());
            final List<RecommendedEventProto> dtos = mapper.mapToDto(events);
            dtos.forEach(responseObserver::onNext);
            log.info("Processed request for new similar events: userId = {}, eventId = {}, maxResults = {}",
                    request.getUserId(), request.getEventId(), request.getMaxResults());
            log.debug("Requested similar events = {}", dtos);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Similar events request processing error: {}\nSimilar events request = {}",
                    e.getMessage(), request, e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(
            final InteractionsCountRequestProto request,
            final StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            log.info("Received request for interactions count: eventId = {}", request.getEventIdList());
            log.debug("Interactions count request = {}", request);
            final List<RecommendedEvent> events = userScoreService.getEventInteractionsCount(request.getEventIdList());
            final List<RecommendedEventProto> dtos = mapper.mapToDto(events);
            dtos.forEach(responseObserver::onNext);
            log.info("Processed request for interactions count: eventId = {}", request.getEventIdList());
            log.debug("Requested interactions counts = {}", dtos);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Interactions count request processing error: {}\nInteractions count request = {}",
                    e.getMessage(), request, e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
