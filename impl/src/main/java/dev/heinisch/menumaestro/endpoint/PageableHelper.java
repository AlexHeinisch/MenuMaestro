package dev.heinisch.menumaestro.endpoint;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageableHelper {
    private static final int DEFAULT_PAGE_SIZE = 20;

    public static Pageable orDefault(Pageable pageable) {
        return pageable.isPaged() ? pageable : PageRequest.of(0, DEFAULT_PAGE_SIZE);
    }
}
