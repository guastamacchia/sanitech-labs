export const environment = {
  production: false,
  name: 'local',
  gatewayUrl: 'http://localhost:8080',
  keycloakUrl: 'http://localhost:8081',
  realm: 'sanitech',
  clientId: 'sanitech-spa',
  scope: 'openid profile email offline_access',
  recaptchaSiteKey: '' // Configura qui la site key reCAPTCHA v3
};
