package it.sanitech.directory.utilities;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageableUtilsTest {

    @Test
    void shouldClampPageAndSizeWithinBounds() {
        Pageable pageable = PageableUtils.pageRequest(-1, 1_000, 50, Sort.by("id"));

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(50);
    }

    @Test
    void shouldUseProvidedValuesWhenValid() {
        Pageable pageable = PageableUtils.pageRequest(2, 20, 50, Sort.by("lastName"));

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("lastName")).isNotNull();
    }
}
