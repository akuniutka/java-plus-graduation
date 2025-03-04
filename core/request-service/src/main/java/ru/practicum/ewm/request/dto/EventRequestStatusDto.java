package ru.practicum.ewm.request.dto;

import java.util.List;

public record EventRequestStatusDto(

        List<RequestDto> confirmedRequests,
        List<RequestDto> rejectedRequests
) {

}
