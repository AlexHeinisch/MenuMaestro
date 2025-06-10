package at.codemaestro.integration_test.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPageableResponse<T> {

    private Long totalElements;
    private Integer totalPages, number, numberOfElements, size;
    private Boolean first, last, empty;
    private ArrayList<T> content;

}
