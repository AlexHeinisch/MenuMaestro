package dev.heinisch.template.service;

import dev.heinisch.template.MessageMapper;
import dev.heinisch.template.model.Message;
import dev.heinisch.template.repository.MessageRepository;
import dev.heinisch.template.api.dto.MessageDto;
import dev.heinisch.template.api.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public MessageDto createMessage(String text) {
        Message m = new Message();
        m.setText(text);
        messageRepository.persist(m);
        return messageMapper.entityToDto(m);
    }

    @Override
    public Page<MessageDto> getMessages(Pageable pageable) {
        return messageRepository.findAll(pageable)
                .map(messageMapper::entityToDto);
    }
}
