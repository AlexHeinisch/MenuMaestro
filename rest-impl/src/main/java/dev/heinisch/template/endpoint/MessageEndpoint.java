package dev.heinisch.template.endpoint;

import dev.heinisch.template.api.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.MessagesApi;
import org.openapitools.model.MessageCreateRequest;
import org.openapitools.model.MessageResponse;
import org.openapitools.model.PaginatedMessageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MessageEndpoint implements MessagesApi {

    private final MessageService messageService;
    private final RestEntitiesMapper restEntitiesMapper;
    @Override
    public ResponseEntity<MessageResponse> createMessage(MessageCreateRequest message) {
        log.info("POST /messages [request-body: {}]", message);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(restEntitiesMapper.dtoToResponseObject(messageService.createMessage(message.getText())));
    }

    @Override
    public ResponseEntity<PaginatedMessageResponse> getAllMessages(Integer page, Integer size, List<String> sort, Pageable pageable) {
        log.info("GET /messages [page: {}, size: {}, sort: {}]", page, size, sort);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(restEntitiesMapper.dtoToResponseObject(messageService.getMessages(pageable)));
    }


}
