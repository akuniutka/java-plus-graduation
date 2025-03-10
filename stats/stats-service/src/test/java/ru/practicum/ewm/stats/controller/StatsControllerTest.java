package ru.practicum.ewm.stats.controller;

import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.practicum.ewm.stats.ViewStatsDto;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.model.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;
import ru.practicum.ewm.stats.util.LogListener;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static ru.practicum.ewm.stats.util.TestUtils.END;
import static ru.practicum.ewm.stats.util.TestUtils.ENDPOINT;
import static ru.practicum.ewm.stats.util.TestUtils.START;
import static ru.practicum.ewm.stats.util.TestUtils.assertLogs;
import static ru.practicum.ewm.stats.util.TestUtils.equalTo;
import static ru.practicum.ewm.stats.util.TestUtils.makeTestEndpointHit;
import static ru.practicum.ewm.stats.util.TestUtils.makeTestEndpointHitDto;
import static ru.practicum.ewm.stats.util.TestUtils.makeTestViewStats;
import static ru.practicum.ewm.stats.util.TestUtils.makeTestViewStatsDto;

class StatsControllerTest {

    private static final LogListener logListener = new LogListener(StatsController.class);

    private AutoCloseable openMocks;

    @Mock
    private StatsService mockService;

    @Mock
    private StatsMapper mockMapper;

    @Captor
    private ArgumentCaptor<List<ViewStats>> viewStatsCaptor;

    private InOrder inOrder;

    private StatsController controller;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        inOrder = Mockito.inOrder(mockService, mockMapper);
        logListener.startListen();
        logListener.reset();
        controller = new StatsController(mockService, mockMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        logListener.stopListen();
        Mockito.verifyNoMoreInteractions(mockService, mockMapper);
        openMocks.close();
    }

    @Test
    void testAddEndpointHit() throws JSONException, IOException {
        when(mockMapper.mapToEndpointHit(makeTestEndpointHitDto())).thenReturn(makeTestEndpointHit().withNoId());

        controller.addEndpointHit(makeTestEndpointHitDto());

        inOrder.verify(mockMapper).mapToEndpointHit(makeTestEndpointHitDto());
        inOrder.verify(mockService).addEndpointHit(makeTestEndpointHit().withNoId());
        assertLogs(logListener.getEvents(), "add_endpoint_hit.json", getClass());
    }

    @Test
    void testGetViewStats() throws JSONException, IOException {
        when(mockService.getViewStats(START, END, List.of(ENDPOINT), false)).thenReturn(List.of(makeTestViewStats()));
        when(mockMapper.mapToDto(anyList())).thenReturn(List.of(makeTestViewStatsDto()));

        final List<ViewStatsDto> actual = controller.getViewStats(START, END, List.of(ENDPOINT), false);

        inOrder.verify(mockService).getViewStats(START, END, List.of(ENDPOINT), false);
        inOrder.verify(mockMapper).mapToDto(viewStatsCaptor.capture());
        assertThat(viewStatsCaptor.getValue(), contains(equalTo(makeTestViewStats())));
        assertThat(actual, contains(makeTestViewStatsDto()));
        assertLogs(logListener.getEvents(), "get_view_stats.json", getClass());
    }

    @Test
    void testGetViewStatsWhenNoUris() throws JSONException, IOException {
        when(mockService.getViewStats(START, END, null, false)).thenReturn(List.of(makeTestViewStats()));
        when(mockMapper.mapToDto(anyList())).thenReturn(List.of(makeTestViewStatsDto()));

        final List<ViewStatsDto> actual = controller.getViewStats(START, END, null, false);

        inOrder.verify(mockService).getViewStats(START, END, null, false);
        inOrder.verify(mockMapper).mapToDto(viewStatsCaptor.capture());
        assertThat(viewStatsCaptor.getValue(), contains(equalTo(makeTestViewStats())));
        assertThat(actual, contains(makeTestViewStatsDto()));
        assertLogs(logListener.getEvents(), "get_view_stats_no_uris.json", getClass());
    }
}