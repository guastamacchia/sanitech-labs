package it.sanitech.directory.utilities;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Set;

class SortUtilsTest {

    @Test
    void safeSort_ignores_null_and_blank_entries() {
        Sort sort = SortUtils.safeSort(new String[]{null, " ", "lastName,desc"}, Set.of("lastName", "firstName"), "id");

        Assertions.assertThat(sort.stream().toList())
                .containsExactly(new Sort.Order(Sort.Direction.DESC, "lastName"));
    }

    @Test
    void safeSort_falls_back_to_default_when_no_valid_entries() {
        Sort sort = SortUtils.safeSort(new String[]{null, "   "}, Set.of("id"), "id");

        Assertions.assertThat(sort).isEqualTo(Sort.by("id").ascending());
    }
}
