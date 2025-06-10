package at.codemaestro.mapper.util;

import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;


public interface BasePageableMapper<P, E> {
    @Mapping(target = "totalElements", source = "totalElements")
    P mapPageable(Page<E> page);
}
