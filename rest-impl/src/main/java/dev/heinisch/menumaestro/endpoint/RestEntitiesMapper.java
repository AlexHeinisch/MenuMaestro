package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.api.dto.MessageDto;
import org.mapstruct.Mapper;
import org.openapitools.model.MessageResponse;
import org.openapitools.model.PaginatedMessageResponse;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface RestEntitiesMapper {

    MessageResponse dtoToResponseObject(MessageDto dto);

    PaginatedMessageResponse dtoToResponseObject(Page<MessageDto> dto);
}
