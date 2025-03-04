package ru.practicum.ewm.compilation.mapper;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CompilationMapperTest {

    private CompilationMapper compilationMapper;

    @BeforeEach
    void setUp() {
        compilationMapper = new CompilationMapper();
    }

    @Test
    void shouldReturnNullWhenMappingNullCompilationToDto() {
        MatcherAssert.assertThat(compilationMapper.mapToDto((Compilation) null), is(nullValue()));
    }

    @Test
    void shouldReturnNullWhenMappingNullDtoListToDto() {
        assertThat(compilationMapper.mapToDto((List<Compilation>) null), is(nullValue()));
    }
}