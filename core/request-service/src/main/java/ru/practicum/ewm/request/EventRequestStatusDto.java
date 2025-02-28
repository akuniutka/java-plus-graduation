package ru.practicum.ewm.request;

import java.util.List;

public record EventRequestStatusDto(

        List<RequestDto> confirmedRequests,
        List<RequestDto> rejectedRequests
) {

}
