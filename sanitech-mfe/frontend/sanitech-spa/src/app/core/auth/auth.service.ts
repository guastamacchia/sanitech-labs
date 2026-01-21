import { Injectable, Optional } from '@angular/core';
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

const mockStorageKey = 'sanitech.mock.auth';

interface MockAuthState {
  isAuthenticated: boolean;
  role: string;
  displayName: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private mockState: MockAuthState | null = null;

  constructor(@Optional() private oauthService?: OAuthService) {
    if (environment.mockAuth) {
      this.mockState = this.loadMockState();
      return;
    }
    this.oauth.configure(authConfig);
    this.oauth.setupAutomaticSilentRefresh();
  }

  async loadDiscovery(): Promise<void> {
    if (environment.mockAuth) {
      return;
    }
    await this.oauth.loadDiscoveryDocumentAndTryLogin();
  }

  login(): void {
    if (environment.mockAuth) {
      this.updateMockState({ isAuthenticated: true });
      return;
    }
    this.oauth.initCodeFlow();
  }

  logout(): void {
    if (environment.mockAuth) {
      this.updateMockState({ isAuthenticated: false });
      return;
    }
    this.oauth.logOut();
  }

  get accessToken(): string {
    if (environment.mockAuth) {
      return '';
    }
    return this.oauth.getAccessToken();
  }

  get isAuthenticated(): boolean {
    if (environment.mockAuth) {
      return this.mockState?.isAuthenticated ?? false;
    }
    return this.oauth.hasValidAccessToken();
  }

  get identityClaims(): Record<string, unknown> {
    if (environment.mockAuth) {
      if (!this.isAuthenticated) {
        return {};
      }
      return {
        name: this.mockState?.displayName ?? 'Utente Sanitech',
        realm_access: {
          roles: this.mockState?.role ? [this.mockState.role] : []
        }
      };
    }
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

  get isMockEnabled(): boolean {
    return environment.mockAuth;
  }

  get roleLabel(): string {
    if (environment.mockAuth) {
      const role = this.mockState?.role;
      if (!role) {
        return '';
      }
      const match = this.mockProfiles.find((item) => item.role === role);
      return match?.label ?? role;
    }
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

  get mockRole(): string {
    return this.mockState?.role ?? 'ROLE_PATIENT';
  }

  get mockProfiles(): Array<{ role: string; label: string; displayName: string }> {
    return [
      { role: 'ROLE_PATIENT', label: 'Paziente', displayName: 'Anna Conti' },
      { role: 'ROLE_DOCTOR', label: 'Medico', displayName: 'Dr. Marco Bianchi' },
      { role: 'ROLE_ADMIN', label: 'Amministratore', displayName: 'Elena Guidi' }
    ];
  }

  signInWithCredentials(email: string, password: string, role: string): boolean {
    if (!environment.mockAuth) {
      this.login();
      return true;
    }
    if (!email.trim() || !password.trim()) {
      return false;
    }
    this.selectMockProfile(role);
    return true;
  }

  selectMockProfile(role: string): void {
    if (!environment.mockAuth) {
      return;
    }
    const profile = this.mockProfiles.find((item) => item.role === role) ?? this.mockProfiles[0];
    this.updateMockState({
      role: profile.role,
      displayName: profile.displayName,
      isAuthenticated: true
    });
  }

  private loadMockState(): MockAuthState {
    const raw = localStorage.getItem(mockStorageKey);
    if (raw) {
      try {
        return JSON.parse(raw) as MockAuthState;
      } catch {
        localStorage.removeItem(mockStorageKey);
      }
    }
    const initial = {
      isAuthenticated: false,
      role: 'ROLE_PATIENT',
      displayName: 'Anna Conti'
    };
    localStorage.setItem(mockStorageKey, JSON.stringify(initial));
    return initial;
  }

  private updateMockState(partial: Partial<MockAuthState>): void {
    if (!this.mockState) {
      this.mockState = this.loadMockState();
    }
    this.mockState = { ...this.mockState, ...partial };
    localStorage.setItem(mockStorageKey, JSON.stringify(this.mockState));
  }

  private get oauth(): OAuthService {
    if (!this.oauthService) {
      throw new Error('OAuthService non configurato');
    }
    return this.oauthService;
  }
}
