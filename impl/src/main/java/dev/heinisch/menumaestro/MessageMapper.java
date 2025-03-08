package dev.heinisch.menumaestro;

import dev.heinisch.menumaestro.model.Message;
import dev.heinisch.menumaestro.api.dto.MessageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageDto entityToDto(Message message);
}
