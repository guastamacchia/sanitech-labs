package it.sanitech.directory.utilities;

import it.sanitech.commons.utilities.SortUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SortUtilsTest {

    private static final Set<String> ALLOWED = Set.of("id", "firstName", "lastName", "email");

    @Test
    void shouldFallbackToDefaultWhenSortIsEmpty() {
        Sort sort = SortUtils.safeSort(null, ALLOWED, "id");

        assertThat(sort).isEqualTo(Sort.by("id").ascending());
    }

    @Test
    void shouldUseOnlyAllowedFieldsAndNormalizeDirection() {
        Sort sort = SortUtils.safeSort(new String[]{"lastName,desc", "email"}, ALLOWED, "id");
        List<Sort.Order> orders = sort.toList();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("lastName");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.get(1).getProperty()).isEqualTo("email");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldFallbackToDefaultWhenFieldIsNotAllowed() {
        Sort sort = SortUtils.safeSort(new String[]{"unknown,desc"}, ALLOWED, "id");

        assertThat(sort.toList()).singleElement().satisfies(order -> {
            assertThat(order.getProperty()).isEqualTo("id");
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
        });
    }
}
