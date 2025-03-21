package ru.practicum.ewm.analyzer.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.message.InteractionsCountRequestProto;
import ru.practicum.ewm.analyzer.message.RecommendedEventProto;
import ru.practicum.ewm.analyzer.message.SimilarEventsRequestProto;
import ru.practicum.ewm.analyzer.message.UserPredictionsRequestProto;
import ru.practicum.ewm.analyzer.service.RecommendationsControllerGrpc;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class AnalyzerClient {

    @GrpcClient("analyzer-service")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getRecommendationsForUser(final long userId, final int maxResults) {
        final UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        final Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);

        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getNewSimilarEvents(final long requesterId, final long sampleEventId,
            final int maxResults
    ) {
        final SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setUserId(requesterId)
                .setEventId(sampleEventId)
                .setMaxResults(maxResults)
                .build();

        final Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);

        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(final Collection<Long> eventIds) {
        final InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        final Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);

        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(final Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }

}
