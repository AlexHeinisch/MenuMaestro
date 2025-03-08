package dev.heinisch.template;

import dev.heinisch.template.model.Message;
import dev.heinisch.template.api.dto.MessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageDto entityToDto(Message message);
}
