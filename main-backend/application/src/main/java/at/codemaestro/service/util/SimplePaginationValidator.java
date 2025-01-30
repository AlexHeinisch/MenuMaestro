package at.codemaestro.service.util;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SimplePaginationValidator {
    private SimplePaginationValidator() {

    }

    public static void validateSortSupported(Sort sort) {
        if (!sort.equals(Sort.unsorted()) && !sort.equals(Sort.by("name"))) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only sort by name or no explicit sort is supported!");
        }
    }
}
