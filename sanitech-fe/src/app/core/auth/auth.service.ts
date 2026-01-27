import { Injectable, Optional } from '@angular/core';
import { AuthConfig, OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

const redirectBase = window.location.origin;

const authConfig: AuthConfig = {
  issuer: `${environment.keycloakUrl}/realms/${environment.realm}`,
  clientId: environment.clientId,
  scope: environment.scope,
  responseType: 'code',
  redirectUri: redirectBase,
  postLogoutRedirectUri: redirectBase,
  strictDiscoveryDocumentValidation: false,
  showDebugInformation: !environment.production,
  requireHttps: environment.production
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(@Optional() private oauthService?: OAuthService) {
    this.oauth.configure(authConfig);
    this.oauth.setupAutomaticSilentRefresh();
  }

  async loadDiscovery(): Promise<void> {
    await this.oauth.loadDiscoveryDocumentAndTryLogin();
  }

  login(): void {
    this.oauth.initCodeFlow();
  }

  logout(): void {
    this.oauth.logOut();
  }

  get accessToken(): string {
    return this.oauth.getAccessToken();
  }

  get isAuthenticated(): boolean {
    return this.oauth.hasValidAccessToken();
  }

  get identityClaims(): Record<string, unknown> {
    return (this.oauth.getIdentityClaims() as Record<string, unknown>) ?? {};
  }

  get displayName(): string {
    return (this.identityClaims['name'] as string) || (this.identityClaims['preferred_username'] as string) || 'Utente';
  }

  hasRole(role: string): boolean {
    const realmAccess = this.identityClaims['realm_access'] as { roles?: string[] } | undefined;
    const roles = realmAccess?.roles ?? [];
    return roles.includes(role);
  }

  get roleLabel(): string {
    const realmAccess = this.identityClaims['realm_access'] as { roles?: string[] } | undefined;
    const role = realmAccess?.roles?.[0];
    if (!role) {
      return '';
    }
    if (role === 'ROLE_PATIENT') {
      return 'Paziente';
    }
    if (role === 'ROLE_DOCTOR') {
      return 'Medico';
    }
    if (role === 'ROLE_ADMIN') {
      return 'Amministratore';
    }
    return role;
  }

  private get oauth(): OAuthService {
    if (!this.oauthService) {
      throw new Error('OAuthService non configurato');
    }
    return this.oauthService;
  }
}
