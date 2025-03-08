package dev.heinisch.menumaestro.api.service;

import dev.heinisch.menumaestro.api.dto.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageDto createMessage(String text);
    Page<MessageDto> getMessages(Pageable pageable);
}
