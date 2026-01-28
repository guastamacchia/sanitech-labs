package db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class V7__sync_keycloak_users extends BaseJavaMigration {

    private static final int PAGE_SIZE = 100;

    @Override
    public void migrate(Context context) throws Exception {
        KeycloakConfig config = KeycloakConfig.fromEnv();
        if (!config.isComplete()) {
            return;
        }

        String token = obtainToken(config);
        if (token == null || token.isBlank()) {
            return;
        }

        List<KeycloakUser> users = fetchUsers(config, token);
        if (users.isEmpty()) {
            return;
        }

        Set<KeycloakUser> doctorUsers = new HashSet<>();
        Set<KeycloakUser> patientUsers = new HashSet<>();
        for (KeycloakUser user : users) {
            if (user.email() == null || user.email().isBlank()) {
                continue;
            }
            List<String> roles = fetchRealmRoles(config, token, user.id());
            if (hasRole(roles, "DOCTOR")) {
                doctorUsers.add(user);
            }
            if (hasRole(roles, "PATIENT")) {
                patientUsers.add(user);
            }
        }

        syncDatabase(context.getConnection(), doctorUsers, patientUsers);
    }

    private void syncDatabase(Connection connection, Set<KeycloakUser> doctorUsers, Set<KeycloakUser> patientUsers)
            throws SQLException {
        Long defaultDepartmentId = fetchSingleId(connection, "SELECT id FROM departments ORDER BY id LIMIT 1");
        Long defaultSpecializationId = fetchSingleId(connection, "SELECT id FROM specializations ORDER BY id LIMIT 1");

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TEMP TABLE IF NOT EXISTS temp_keycloak_doctors(email VARCHAR(200))");
            statement.execute("CREATE TEMP TABLE IF NOT EXISTS temp_keycloak_patients(email VARCHAR(200))");
            statement.execute("TRUNCATE TABLE temp_keycloak_doctors");
            statement.execute("TRUNCATE TABLE temp_keycloak_patients");
        }

        try (PreparedStatement insertDoctor = connection.prepareStatement(
                "INSERT INTO doctors (first_name, last_name, email, phone, department_id, specialization_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT (email) DO UPDATE SET " +
                        "first_name = EXCLUDED.first_name, " +
                        "last_name = EXCLUDED.last_name, " +
                        "phone = EXCLUDED.phone"
        );
             PreparedStatement updateDoctor = connection.prepareStatement(
                     "UPDATE doctors SET first_name = ?, last_name = ?, phone = ? WHERE email = ?"
             );
             PreparedStatement insertPatient = connection.prepareStatement(
                     "INSERT INTO patients (first_name, last_name, email, phone) " +
                             "VALUES (?, ?, ?, ?) " +
                             "ON CONFLICT (email) DO UPDATE SET " +
                             "first_name = EXCLUDED.first_name, " +
                             "last_name = EXCLUDED.last_name, " +
                             "phone = EXCLUDED.phone"
             );
             PreparedStatement insertDoctorEmail = connection.prepareStatement(
                     "INSERT INTO temp_keycloak_doctors(email) VALUES (?)"
             );
             PreparedStatement insertPatientEmail = connection.prepareStatement(
                     "INSERT INTO temp_keycloak_patients(email) VALUES (?)"
             )
        ) {
            boolean canInsertDoctor = defaultDepartmentId != null && defaultSpecializationId != null;

            for (KeycloakUser doctor : doctorUsers) {
                insertDoctorEmail.setString(1, doctor.email());
                insertDoctorEmail.executeUpdate();
                if (canInsertDoctor) {
                    insertDoctor.setString(1, safeValue(doctor.firstName()));
                    insertDoctor.setString(2, safeValue(doctor.lastName()));
                    insertDoctor.setString(3, doctor.email());
                    insertDoctor.setString(4, doctor.phone());
                    insertDoctor.setLong(5, defaultDepartmentId);
                    insertDoctor.setLong(6, defaultSpecializationId);
                    insertDoctor.executeUpdate();
                } else {
                    updateDoctor.setString(1, safeValue(doctor.firstName()));
                    updateDoctor.setString(2, safeValue(doctor.lastName()));
                    updateDoctor.setString(3, doctor.phone());
                    updateDoctor.setString(4, doctor.email());
                    updateDoctor.executeUpdate();
                }
            }

            for (KeycloakUser patient : patientUsers) {
                insertPatientEmail.setString(1, patient.email());
                insertPatientEmail.executeUpdate();
                insertPatient.setString(1, safeValue(patient.firstName()));
                insertPatient.setString(2, safeValue(patient.lastName()));
                insertPatient.setString(3, patient.email());
                insertPatient.setString(4, patient.phone());
                insertPatient.executeUpdate();
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM doctors WHERE email NOT IN (SELECT email FROM temp_keycloak_doctors)");
            statement.executeUpdate("DELETE FROM patients WHERE email NOT IN (SELECT email FROM temp_keycloak_patients)");
        }
    }

    private Long fetchSingleId(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return null;
        }
    }

    private String obtainToken(KeycloakConfig config) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        String tokenUrl = config.serverUrl + config.tokenPath.replace("{realm}", config.realm);
        String body = "grant_type=client_credentials" +
                "&client_id=" + urlEncode(config.clientId) +
                "&client_secret=" + urlEncode(config.clientSecret);
        HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> payload = mapper.readValue(response.body(), Map.class);
        Object token = payload.get("access_token");
        return token == null ? null : token.toString();
    }

    private List<KeycloakUser> fetchUsers(KeycloakConfig config, String token) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        ObjectMapper mapper = new ObjectMapper();
        List<KeycloakUser> users = new ArrayList<>();
        int first = 0;
        while (true) {
            String url = config.serverUrl + "/admin/realms/" + config.realm + "/users?first=" + first + "&max=" + PAGE_SIZE;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                break;
            }
            KeycloakUser[] page = mapper.readValue(response.body(), KeycloakUser[].class);
            if (page.length == 0) {
                break;
            }
            for (KeycloakUser user : page) {
                if (user != null) {
                    users.add(user);
                }
            }
            if (page.length < PAGE_SIZE) {
                break;
            }
            first += PAGE_SIZE;
        }
        return users;
    }

    private List<String> fetchRealmRoles(KeycloakConfig config, String token, String userId)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String url = config.serverUrl + "/admin/realms/" + config.realm + "/users/" + userId + "/role-mappings/realm";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            return List.of();
        }
        List<Map<String, Object>> roles = mapper.readValue(response.body(), List.class);
        List<String> names = new ArrayList<>();
        for (Map<String, Object> role : roles) {
            Object name = role.get("name");
            if (name != null) {
                names.add(name.toString());
            }
        }
        return names;
    }

    private boolean hasRole(List<String> roles, String roleName) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        String normalized = roleName.toUpperCase(Locale.ROOT);
        for (String role : roles) {
            if (role == null) {
                continue;
            }
            String candidate = role.toUpperCase(Locale.ROOT);
            if (candidate.equals(normalized) || candidate.equals("ROLE_" + normalized)) {
                return true;
            }
        }
        return false;
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record KeycloakUser(
            String id,
            String email,
            String firstName,
            String lastName,
            Map<String, List<String>> attributes
    ) {
        String phone() {
            if (attributes == null) {
                return null;
            }
            List<String> phones = attributes.get("phone");
            if (phones == null || phones.isEmpty()) {
                return null;
            }
            String phone = phones.get(0);
            return phone == null || phone.isBlank() ? null : phone.trim();
        }
    }

    private static class KeycloakConfig {
        private final String serverUrl;
        private final String realm;
        private final String clientId;
        private final String clientSecret;
        private final String tokenPath;

        private KeycloakConfig(String serverUrl, String realm, String clientId, String clientSecret, String tokenPath) {
            this.serverUrl = serverUrl;
            this.realm = realm;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.tokenPath = tokenPath;
        }

        static KeycloakConfig fromEnv() {
            String scheme = getEnv("OAUTH2_SCHEME");
            String host = getEnv("OAUTH2_HOST");
            String port = getEnv("OAUTH2_PORT");
            String adminUrl = getEnv("KEYCLOAK_ADMIN_URL");
            String serverUrl = adminUrl;
            if (serverUrl == null || serverUrl.isBlank()) {
                if (scheme != null && host != null && port != null) {
                    serverUrl = scheme + "://" + host + ":" + port;
                }
            }
            String realm = valueOr(getEnv("KEYCLOAK_ADMIN_REALM"), getEnv("OAUTH2_REALM"));
            String clientId = getEnv("KEYCLOAK_ADMIN_CLIENT_ID");
            String clientSecret = getEnv("KEYCLOAK_ADMIN_CLIENT_SECRET");
            String tokenPath = valueOr(getEnv("KEYCLOAK_ADMIN_TOKEN_PATH"),
                    "/realms/{realm}/protocol/openid-connect/token");
            return new KeycloakConfig(serverUrl, realm, clientId, clientSecret, tokenPath);
        }

        boolean isComplete() {
            return !isBlank(serverUrl) && !isBlank(realm) && !isBlank(clientId) && !isBlank(clientSecret);
        }

        private static String getEnv(String key) {
            return System.getenv(key);
        }

        private static String valueOr(String value, String fallback) {
            return isBlank(value) ? fallback : value;
        }

        private static boolean isBlank(String value) {
            return value == null || value.isBlank();
        }
    }
}
