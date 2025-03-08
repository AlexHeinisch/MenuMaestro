package dev.heinisch.template.api.service;

import dev.heinisch.template.api.dto.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageDto createMessage(String text);
    Page<MessageDto> getMessages(Pageable pageable);
}
