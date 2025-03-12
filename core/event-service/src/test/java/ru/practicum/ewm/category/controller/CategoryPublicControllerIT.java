package ru.practicum.ewm.category.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.configuration.ClockConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.ewm.util.TestUtils.loadJson;

@WebMvcTest(controllers = CategoryPublicController.class)
@ContextConfiguration(classes = ClockConfig.class)
class CategoryPublicControllerIT {

    private static final String BASE_PATH = "/categories";

    @MockBean
    private CategoryService mockService;

    @MockBean
    private CategoryMapper mockMapper;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        Mockito.reset(mockService, mockMapper);
    }

    @AfterEach
    void tearDown() {
        Mockito.verifyNoMoreInteractions(mockService, mockMapper);
    }

    @Test
    void whenGetAtBaseEndpointAndFromNegativeAndSizeZero_ThenInvokeControllerExceptionHandler() throws Exception {
        final String responseBody = loadJson("find_all_with_negative_from_and_zero_size.json", getClass());

        mvc.perform(get(BASE_PATH)
                        .param("from", "-1")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(responseBody, true));
    }
}