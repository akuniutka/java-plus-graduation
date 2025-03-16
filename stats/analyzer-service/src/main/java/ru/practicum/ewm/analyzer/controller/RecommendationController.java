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
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.service.RecommendationsControllerGrpc;
import ru.practicum.ewm.analyzer.service.UserScoreService;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final UserScoreService userScoreService;
    private final RecommendedEventMapper mapper;

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
