import { Injectable, Optional } from '@angular/core';
import { AuthConfig, OAuthService, OAuthErrorEvent } from 'angular-oauth2-oidc';
import { filter } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

const redirectBase = window.location.origin;

const authConfig: AuthConfig = {
  issuer: `${environment.keycloakUrl}/realms/${environment.realm}`,
  clientId: environment.clientId,
  scope: environment.scope,
  responseType: 'code',
  redirectUri: redirectBase,
  postLogoutRedirectUri: redirectBase,
  strictDiscoveryDocumentValidation: true,
  showDebugInformation: !environment.production,
  requireHttps: environment.production,
  useSilentRefresh: true,
  silentRefreshRedirectUri: `${redirectBase}/silent-refresh.html`
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authError: OAuthErrorEvent | null = null;

  constructor(@Optional() private oauthService?: OAuthService) {
    this.oauth.configure(authConfig);
    this.setupErrorHandling();
    this.oauth.setupAutomaticSilentRefresh();
  }

  private setupErrorHandling(): void {
    this.oauth.events
      .pipe(filter((event): event is OAuthErrorEvent => event instanceof OAuthErrorEvent))
      .subscribe((error) => {
        this.authError = error;
        if (!environment.production) {
          console.warn('OAuth error:', error.type, error.params);
        }
      });
  }

  get lastError(): OAuthErrorEvent | null {
    return this.authError;
  }

  clearError(): void {
    this.authError = null;
  }

  async loadDiscovery(): Promise<boolean> {
    try {
      await this.oauth.loadDiscoveryDocumentAndTryLogin();
      return true;
    } catch (error) {
      if (!environment.production) {
        console.warn('Failed to load OAuth discovery document:', error);
      }
      return false;
    }
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

  /**
   * Attempts to refresh the access token using the refresh token.
   * @returns Promise<boolean> - true if refresh was successful, false otherwise
   */
  async refreshToken(): Promise<boolean> {
    try {
      const result = await this.oauth.refreshToken();
      return !!result && this.oauth.hasValidAccessToken();
    } catch (error) {
      if (!environment.production) {
        console.warn('Token refresh failed:', error);
      }
      return false;
    }
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
      const keysToRemove = Object.keys(localStorage).filter(
        (key) =>
          key.startsWith('access_token') ||
          key.startsWith('id_token') ||
          key.startsWith('refresh_token') ||
          key.startsWith('nonce') ||
          key.startsWith('PKCE_verifier') ||
          key.includes('session_state') ||
          key.includes('expires_at')
      );
      keysToRemove.forEach((key) => localStorage.removeItem(key));
    } catch {
      // Ignore storage access errors
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
