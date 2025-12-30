package it.sanitech.notifications.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;

/**
 * Implementazione minimale di RFC 7807 (Problem Details) per API REST.
 *
 * <p>
 * È un payload standard per rappresentare errori applicativi in modo consistente:
 * type/title/status/detail/instance + eventuali estensioni.
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiProblem(
        URI type,
        String title,
        Integer status,
        String detail,
        String instance,
        Object errors
) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI type;
        private String title;
        private Integer status;
        private String detail;
        private String instance;
        private Object errors;

        private Builder() { }

        public Builder type(String type) {
            this.type = type == null ? null : URI.create(type);
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder errors(Object errors) {
            this.errors = errors;
            return this;
        }

        public ApiProblem build() {
            return new ApiProblem(type, title, status, detail, instance, errors);
        }
    }
}
