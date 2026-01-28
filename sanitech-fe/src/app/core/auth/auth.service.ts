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
    this.clearClientState();
    this.oauth.logOut();
  }

  get accessToken(): string {
    return this.oauth.getAccessToken();
  }

  getAccessTokenClaim(name: string): unknown {
    return this.accessTokenClaims[name];
  }

  get isAuthenticated(): boolean {
    return this.oauth.hasValidAccessToken();
  }

  get identityClaims(): Record<string, unknown> {
    return (this.oauth.getIdentityClaims() as Record<string, unknown>) ?? {};
  }

  private get accessTokenClaims(): Record<string, unknown> {
    return this.decodeJwt(this.oauth.getAccessToken());
  }

  get displayName(): string {
    return (this.identityClaims['name'] as string) || (this.identityClaims['preferred_username'] as string) || 'Utente';
  }

  hasRole(role: string): boolean {
    const realmAccess =
      (this.accessTokenClaims['realm_access'] as { roles?: string[] } | undefined) ||
      (this.identityClaims['realm_access'] as { roles?: string[] } | undefined);
    const roles = realmAccess?.roles ?? [];
    return roles.includes(role);
  }

  get roleLabel(): string {
    const realmAccess =
      (this.accessTokenClaims['realm_access'] as { roles?: string[] } | undefined) ||
      (this.identityClaims['realm_access'] as { roles?: string[] } | undefined);
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

  private clearClientState(): void {
    try {
      localStorage.clear();
    } catch {
      // Ignore storage access errors
    }
    try {
      sessionStorage.clear();
    } catch {
      // Ignore storage access errors
    }
    this.clearCookies();
  }

  private clearCookies(): void {
    const cookieEntries = document.cookie ? document.cookie.split(';') : [];
    const hostname = window.location.hostname;
    const domains = hostname.includes('.') ? [hostname, `.${hostname}`] : [hostname];
    const paths = ['/', '/auth'];

    for (const entry of cookieEntries) {
      const name = entry.split('=')[0]?.trim();
      if (!name) {
        continue;
      }
      for (const path of paths) {
        document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=${path}`;
        for (const domain of domains) {
          document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=${path};domain=${domain}`;
        }
      }
    }
  }

  private decodeJwt(token: string): Record<string, unknown> {
    if (!token) {
      return {};
    }
    const payload = token.split('.')[1];
    if (!payload) {
      return {};
    }
    try {
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(normalized)
          .split('')
          .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
          .join('')
      );
      return JSON.parse(json) as Record<string, unknown>;
    } catch {
      return {};
    }
  }
}
