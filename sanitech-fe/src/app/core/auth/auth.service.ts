import { Injectable, Optional, OnDestroy } from '@angular/core';
import { AuthConfig, OAuthService, OAuthErrorEvent } from 'angular-oauth2-oidc';
import { filter } from 'rxjs/operators';
import { environment } from '@env/environment';

const redirectBase = window.location.origin;

const SILENT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minuti

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
  useSilentRefresh: false,
  silentRefreshRedirectUri: `${redirectBase}/silent-refresh.html`,
  timeoutFactor: 0.75
};

@Injectable({
  providedIn: 'root'
})
export class AuthService implements OnDestroy {
  private authError: OAuthErrorEvent | null = null;
  private silentRefreshTimerId: ReturnType<typeof setInterval> | null = null;

  constructor(@Optional() private oauthService?: OAuthService) {
    this.oauth.configure(authConfig);
    this.setupErrorHandling();
    this.startSilentRefreshTimer();
  }

  ngOnDestroy(): void {
    this.stopSilentRefreshTimer();
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
    this.stopSilentRefreshTimer();
    this.clearClientState();
    this.oauth.logOut();
  }

  get accessToken(): string {
    return this.oauth.getAccessToken();
  }

  /**
   * Tenta di rinnovare l'access token usando il refresh token.
   * @returns Promise<boolean> - true se il rinnovo è riuscito, false altrimenti
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

  /**
   * Verifica se è disponibile un refresh token per rinnovare la sessione.
   */
  get hasRefreshToken(): boolean {
    return !!this.oauth.getRefreshToken();
  }

  /**
   * Tenta di rinnovare la sessione: prima prova il refresh token,
   * poi verifica se il token è ancora valido.
   * Usato dall'auth guard prima di redirigere al login.
   */
  async tryRefreshLogin(): Promise<boolean> {
    if (this.oauth.hasValidAccessToken()) {
      return true;
    }
    if (this.hasRefreshToken) {
      return this.refreshToken();
    }
    return false;
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

  /**
   * Avvia un timer che rinnova silenziosamente il token ogni 5 minuti.
   * In questo modo il token viene sempre rinnovato prima della scadenza,
   * evitando che l'utente venga disconnesso.
   */
  private startSilentRefreshTimer(): void {
    this.stopSilentRefreshTimer();
    this.silentRefreshTimerId = setInterval(() => {
      if (this.hasRefreshToken) {
        this.refreshToken().then((success) => {
          if (!environment.production) {
            console.log(`[AuthService] Silent refresh ${success ? 'riuscito' : 'fallito'}`);
          }
        });
      }
    }, SILENT_REFRESH_INTERVAL_MS);
  }

  private stopSilentRefreshTimer(): void {
    if (this.silentRefreshTimerId !== null) {
      clearInterval(this.silentRefreshTimerId);
      this.silentRefreshTimerId = null;
    }
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
      // Ignora errori di accesso allo storage
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
