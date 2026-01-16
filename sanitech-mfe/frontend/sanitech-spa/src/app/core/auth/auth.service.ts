import { Injectable } from '@angular/core';
import { AuthConfig, OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

const authConfig: AuthConfig = {
  issuer: `${environment.keycloakUrl}/realms/${environment.realm}`,
  clientId: environment.clientId,
  scope: environment.scope,
  responseType: 'code',
  strictDiscoveryDocumentValidation: false,
  showDebugInformation: !environment.production,
  requireHttps: environment.production
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private oauthService: OAuthService) {
    this.oauthService.configure(authConfig);
    this.oauthService.setupAutomaticSilentRefresh();
  }

  async loadDiscovery(): Promise<void> {
    await this.oauthService.loadDiscoveryDocumentAndTryLogin();
  }

  login(): void {
    this.oauthService.initCodeFlow();
  }

  logout(): void {
    this.oauthService.logOut();
  }

  get accessToken(): string {
    return this.oauthService.getAccessToken();
  }

  get isAuthenticated(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  get identityClaims(): Record<string, unknown> {
    return (this.oauthService.getIdentityClaims() as Record<string, unknown>) ?? {};
  }

  get displayName(): string {
    return (this.identityClaims['name'] as string) || (this.identityClaims['preferred_username'] as string) || 'Utente';
  }

  hasRole(role: string): boolean {
    const realmAccess = this.identityClaims['realm_access'] as { roles?: string[] } | undefined;
    const roles = realmAccess?.roles ?? [];
    return roles.includes(role);
  }
}
