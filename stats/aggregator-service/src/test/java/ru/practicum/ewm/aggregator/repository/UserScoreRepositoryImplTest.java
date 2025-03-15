package ru.practicum.ewm.aggregator.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class UserScoreRepositoryImplTest {

    private static final long USER_A = 1001L;
    private static final long USER_B = 1002L;
    private static final long EVENT_A = 101L;
    private static final long EVENT_B = 102L;
    private static final float ZERO_SCORE = 0.0f;
    private static final float VIEW_SCORE = 0.4f;
    private static final float REGISTER_SCORE = 0.8f;

    private UserScoreRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserScoreRepositoryImpl();
    }

    @Test
    void whenUserNotExist_ThenReturnZeroScore() {

        final float score = repository.getByUserIdAndEventId(USER_A, EVENT_A);

        assertThat(score, equalTo(0.0f));
    }

    @Test
    void whenEventNotExist_ThenReturnZeroScore() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);

        final float score = repository.getByUserIdAndEventId(USER_A, EVENT_B);

        assertThat(score, equalTo(ZERO_SCORE));
    }

    @Test
    void whenUserScoreForEventNotExist_ThenReturnZeroScore() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);
        repository.save(USER_B, EVENT_B, REGISTER_SCORE);

        final float score = repository.getByUserIdAndEventId(USER_A, EVENT_B);

        assertThat(score, equalTo(ZERO_SCORE));
    }

    @Test
    void whenUserScoreForEventScoreExist_ThenReturnIt() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);

        final float score = repository.getByUserIdAndEventId(USER_A, EVENT_A);

        assertThat(score, equalTo(VIEW_SCORE));
    }

    @Test
    void whenUserScoreForSeveralEvents_ThenTheyNotOverlap() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);
        repository.save(USER_A, EVENT_B, REGISTER_SCORE);

        final float scoreA = repository.getByUserIdAndEventId(USER_A, EVENT_A);
        final float scoreB = repository.getByUserIdAndEventId(USER_A, EVENT_B);

        assertThat(scoreA, equalTo(VIEW_SCORE));
        assertThat(scoreB, equalTo(REGISTER_SCORE));
    }

    @Test
    void whenSeveralUserScoresForEvent_ThenTheyNotOverlap() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);
        repository.save(USER_B, EVENT_A, REGISTER_SCORE);

        final float scoreA = repository.getByUserIdAndEventId(USER_A, EVENT_A);
        final float scoreB = repository.getByUserIdAndEventId(USER_B, EVENT_A);

        assertThat(scoreA, equalTo(VIEW_SCORE));
        assertThat(scoreB, equalTo(REGISTER_SCORE));
    }

    @Test
    void whenOverwriteExistingScore_ThenReturnNewValue() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);
        repository.save(USER_A, EVENT_A, REGISTER_SCORE);

        final float score = repository.getByUserIdAndEventId(USER_A, EVENT_A);

        assertThat(score, equalTo(REGISTER_SCORE));
    }

    @Test
    void whenUserNotExist_ThenReturnEmptySetOfEvents() {

        final Set<Long> events = repository.findEventIdByUserId(USER_A);

        assertThat(events.isEmpty(), is(true));
    }

    @Test
    void whenUserScoreForSeveralEvents_ThenReturnCorrectSetOfEvents() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);
        repository.save(USER_A, EVENT_B, REGISTER_SCORE);

        final Set<Long> events = repository.findEventIdByUserId(USER_A);

        assertThat(events, containsInAnyOrder(EVENT_A, EVENT_B));
    }

    @Test
    void whenSeveralUserScoresForSeveralEvents_ThenSetOfEventsNotOverlap() {
        repository.save(USER_A, EVENT_A, VIEW_SCORE);
        repository.save(USER_B, EVENT_B, REGISTER_SCORE);

        final Set<Long> eventsA = repository.findEventIdByUserId(USER_A);
        final Set<Long> eventsB = repository.findEventIdByUserId(USER_B);

        assertThat(eventsA, containsInAnyOrder(EVENT_A));
        assertThat(eventsB, containsInAnyOrder(EVENT_B));
    }
}