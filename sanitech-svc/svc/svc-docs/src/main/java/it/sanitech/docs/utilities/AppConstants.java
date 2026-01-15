package it.sanitech.docs.utilities;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Collezione di costanti applicative specifiche del microservizio Docs.
 */
@UtilityClass
public class AppConstants {

    /**
     * Path REST esposti dal microservizio.
     */
    @UtilityClass
    public static class ApiPath {
        public static final String API = "/api";
        public static final String DOCS = API + "/docs";
        public static final String ADMIN_DOCS = API + "/admin/docs";
    }

    /**
     * Sorting: whitelist dei campi ordinabili esposti via API.
     */
    @UtilityClass
    public static class SortField {
        public static final Set<String> DOCS_ALLOWED = Set.of(
                "id",
                "createdAt",
                "fileName",
                "sizeBytes",
                "documentType",
                "departmentCode"
        );
        public static final String DOCS_DEFAULT = "createdAt";
    }

    /**
     * Proprietà custom del microservizio.
     */
    @UtilityClass
    public static class ConfigKeys {
        @UtilityClass
        public static class S3 {
            public static final String PREFIX = "sanitech.docs.s3";
        }
    }

    /**
     * Claim JWT specifici del microservizio.
     */
    @UtilityClass
    public static class Security {
        public static final String CLAIM_PATIENT_ID = "pid";
    }

    /**
     * Problem Details specifici del dominio docs.
     */
    @UtilityClass
    public static class Problem {
        public static final String TYPE_CONSENT_REQUIRED = "https://sanitech.it/problems/consent-required";
    }

    /**
     * Messaggi specifici del dominio docs.
     */
    @UtilityClass
    public static class ErrorMessage {
        public static final String ERR_CONSENT_REQUIRED = "Consenso mancante";
    }
}
