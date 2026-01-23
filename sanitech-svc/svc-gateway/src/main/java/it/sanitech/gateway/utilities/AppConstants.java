package it.sanitech.gateway.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Costanti applicative centralizzate per {@code svc-gateway}.
 *
 * <p>
 * Convenzione: le costanti sono organizzate in classi annidate per ambito (Security, Routes, OpenApi, ecc.)
 * per mantenere il codice leggibile e prevenire stringhe “cablare” nei componenti.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    /**
     * Chiavi di configurazione (prefix) usate nelle {@code @ConfigurationProperties}.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigKeys {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Cors {
            /** Prefix del blocco CORS: {@code sanitech.cors.*}. */
            public static final String PREFIX = "sanitech.cors";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Gateway {
            /** Prefix del blocco gateway: {@code sanitech.gateway.*}. */
            public static final String PREFIX = "sanitech.gateway";
        }
    }

    /**
     * Valori di default (fallback) usati in assenza di proprietà esplicite.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ConfigDefaultValue {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Cors {
            public static final boolean ALLOW_CREDENTIALS = false;
            public static final long MAX_AGE_SECONDS = 3600L;
        }
    }

    /**
     * Costanti di sicurezza (claim JWT, prefissi authority, ecc.).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Security {
        public static final String CLAIM_REALM_ACCESS = "realm_access";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPE = "scope";
        public static final String CLAIM_DEPT = "dept";

        public static final String AUTHORITY_PREFIX_ROLE = "ROLE_";
        public static final String AUTHORITY_PREFIX_SCOPE = "SCOPE_";
        public static final String AUTHORITY_PREFIX_DEPT = "DEPT_";
    }

    /**
     * Costanti per routing e nomi “logici” dei servizi downstream.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Services {
        public static final String DIRECTORY = "directory";
        public static final String SCHEDULING = "scheduling";
        public static final String ADMISSIONS = "admissions";
        public static final String CONSENTS = "consents";
        public static final String DOCS = "docs";
        public static final String NOTIFICATIONS = "notifications";
        public static final String AUDIT = "audit";
        public static final String TELEVISIT = "televisit";
        public static final String PAYMENTS = "payments";
        public static final String PRESCRIBING = "prescribing";
    }

    /**
     * Costanti per OpenAPI/Swagger UI centralizzata del gateway.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class OpenApi {
        public static final String MERGED_ENDPOINT = "/openapi/merged";
        public static final String PROXY_ENDPOINT_TEMPLATE = "/openapi/{service}";

        public static final String TITLE_PLATFORM = "Sanitech — Platform API";
        public static final String VERSION_V1 = "v1";
    }

    /**
     * Costanti HTTP comuni.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Http {
        public static final String HEADER_REQUEST_ID = "X-Request-Id";
    }
}
