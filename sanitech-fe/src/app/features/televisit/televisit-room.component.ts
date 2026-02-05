import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Room, RoomEvent, VideoPresets, Track, RemoteTrack, RemoteTrackPublication, LocalTrack, RemoteParticipant } from 'livekit-client';

@Component({
  selector: 'app-televisit-room',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <!-- Modal backdrop -->
    <div class="modal-backdrop-custom"></div>

    <!-- Modal container -->
    <div class="modal-container">
      <div class="televisit-modal d-flex flex-column bg-dark rounded-3 overflow-hidden">
        <!-- Header -->
        <div class="header bg-dark text-white py-2 px-4 d-flex align-items-center justify-content-between border-bottom border-secondary">
          <div class="d-flex align-items-center gap-3">
            <span class="badge" [class.bg-success]="connectionState === 'connected'" [class.bg-warning]="connectionState === 'connecting'" [class.bg-info]="connectionState === 'demo'" [class.bg-danger]="connectionState === 'disconnected'">
              <i class="bi" [class.bi-wifi]="connectionState === 'connected'" [class.bi-hourglass-split]="connectionState === 'connecting'" [class.bi-wifi]="connectionState === 'demo'" [class.bi-wifi-off]="connectionState === 'disconnected'"></i>
              {{ connectionStateLabel }}
            </span>
            <span *ngIf="roomName" class="text-muted small">Room: {{ roomName }}</span>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="text-muted small" *ngIf="duration">{{ formatDuration(duration) }}</span>
            <button class="btn btn-outline-light btn-sm" (click)="goBack()" title="Esci">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
        </div>

        <!-- Main content -->
        <div class="flex-grow-1 d-flex position-relative overflow-hidden" style="min-height: 0;">
          <!-- Remote video (full size) -->
          <div class="remote-video-container flex-grow-1 d-flex align-items-center justify-content-center bg-secondary bg-opacity-25">
            <div #remoteVideoContainer class="w-100 h-100 d-flex align-items-center justify-content-center">
              <div *ngIf="!hasRemoteVideo" class="text-center text-white px-3">
                <div class="waiting-animation mb-4">
                  <i class="bi bi-person-video display-1 d-block"></i>
                  <div class="pulse-ring"></div>
                </div>
                <h4 class="mb-2">In attesa di altri partecipanti...</h4>
                <p class="text-light fs-6 mb-0">La videochiamata inizierà automaticamente quando un partecipante si collegherà</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Bottom bar with local video and controls -->
        <div class="bottom-bar bg-dark border-top border-secondary d-flex align-items-center justify-content-between px-4" style="height: 120px; flex-shrink: 0;">
          <!-- Local video (left) -->
          <div class="local-video-wrapper" style="width: 160px; height: 100px;">
            <div #localVideoContainer class="w-100 h-100 rounded overflow-hidden bg-secondary border border-secondary">
              <div *ngIf="!hasLocalVideo" class="w-100 h-100 d-flex align-items-center justify-content-center">
                <i class="bi bi-camera-video-off text-muted fs-5"></i>
              </div>
            </div>
          </div>

          <!-- Controls (center) -->
          <div class="d-flex justify-content-center align-items-center gap-3">
            <button class="control-btn" [class.active]="!isMuted" [class.muted]="isMuted" (click)="toggleMute()" [title]="isMuted ? 'Attiva microfono' : 'Muta microfono'">
              <i class="bi" [class.bi-mic-fill]="!isMuted" [class.bi-mic-mute-fill]="isMuted"></i>
            </button>
            <button class="control-btn" [class.active]="!isVideoOff" [class.video-off]="isVideoOff" (click)="toggleVideo()" [title]="isVideoOff ? 'Attiva video' : 'Disattiva video'">
              <i class="bi" [class.bi-camera-video-fill]="!isVideoOff" [class.bi-camera-video-off-fill]="isVideoOff"></i>
            </button>
            <button class="control-btn end-call" (click)="endCall()" title="Termina chiamata">
              <i class="bi bi-telephone-x-fill"></i>
            </button>
          </div>

          <!-- Spacer (right) -->
          <div style="width: 160px;"></div>
        </div>

        <!-- Loading overlay -->
        <div *ngIf="isLoading" class="loading-overlay">
          <div class="text-center text-white">
            <div class="spinner-border text-primary mb-3" role="status"></div>
            <p class="mb-0">Connessione in corso...</p>
          </div>
        </div>

      </div>
    </div>
  `,
  styles: [`
    .modal-backdrop-custom {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(2px);
      z-index: 1040;
    }
    .modal-container {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1050;
      padding: 5%;
    }
    .televisit-modal {
      width: 85%;
      height: 85%;
      max-width: 1400px;
      max-height: 900px;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
      position: relative;
    }
    .remote-video-container video {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
    .local-video-wrapper video {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transform: scaleX(-1);
    }

    /* Control buttons */
    .control-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 56px;
      height: 56px;
      border-radius: 50%;
      border: 2px solid rgba(255, 255, 255, 0.4);
      background: rgba(255, 255, 255, 0.15);
      color: white;
      cursor: pointer;
      transition: all 0.2s ease;
      font-size: 1.25rem;
    }
    .control-btn:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: scale(1.05);
    }
    .control-btn.active {
      border-color: #28a745;
      background: rgba(40, 167, 69, 0.3);
    }
    .control-btn.muted {
      border-color: #dc3545;
      background: rgba(220, 53, 69, 0.4);
    }
    .control-btn.video-off {
      border-color: #ffc107;
      background: rgba(255, 193, 7, 0.4);
    }
    .control-btn.end-call {
      border-color: #dc3545;
      background: #dc3545;
    }
    .control-btn.end-call:hover {
      background: #c82333;
    }

    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1060;
    }

    .waiting-animation {
      position: relative;
      display: inline-block;
    }
    .waiting-animation .pulse-ring {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 120px;
      height: 120px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      animation: pulse 2s ease-out infinite;
    }
    @keyframes pulse {
      0% {
        transform: translate(-50%, -50%) scale(0.8);
        opacity: 1;
      }
      100% {
        transform: translate(-50%, -50%) scale(1.5);
        opacity: 0;
      }
    }
  `]
})
export class TelevisitRoomComponent implements OnInit, OnDestroy {
  @ViewChild('localVideoContainer', { static: true }) localVideoContainer!: ElementRef<HTMLDivElement>;
  @ViewChild('remoteVideoContainer', { static: true }) remoteVideoContainer!: ElementRef<HTMLDivElement>;

  roomName = '';
  televisitId = '';
  connectionState: 'disconnected' | 'connecting' | 'connected' | 'demo' = 'disconnected';
  isLoading = false;
  cameraError = false;
  showPermissionPrompt = false;

  isMuted = false;
  isVideoOff = false;
  hasLocalVideo = false;
  hasRemoteVideo = false;
  duration = 0;

  private room: Room | null = null;
  private durationInterval: ReturnType<typeof setInterval> | null = null;
  private startTime: Date | null = null;
  private localStream: MediaStream | null = null;
  private isSimulatedMode = false;
  private pendingToken = '';
  private pendingUrl = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  get connectionStateLabel(): string {
    switch (this.connectionState) {
      case 'connected': return 'Connesso';
      case 'connecting': return 'Connessione...';
      case 'demo': return 'Connesso';
      default: return 'Disconnesso';
    }
  }

  ngOnInit(): void {
    const params = this.route.snapshot.queryParams;
    this.televisitId = params['id'] || '';
    const token = params['token'] || '';
    const livekitUrl = params['url'] || '';
    this.roomName = params['room'] || 'demo-room';

    // Salva i parametri per dopo
    this.pendingToken = token;
    this.pendingUrl = livekitUrl;

    // Avvia direttamente la connessione
    if (!token || !livekitUrl) {
      this.startSimulatedMode();
    } else {
      this.connectToRoom(livekitUrl, token);
    }
  }

  /**
   * Richiede i permessi per fotocamera e microfono e avvia la connessione.
   */
  requestPermissions(): void {
    this.showPermissionPrompt = false;

    if (!this.pendingToken || !this.pendingUrl) {
      // Modalità simulata: mostra loading per 2 secondi, poi attiva la webcam locale
      this.startSimulatedMode();
      return;
    }

    this.connectToRoom(this.pendingUrl, this.pendingToken);
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.stopLocalStream();
    if (this.durationInterval) {
      clearInterval(this.durationInterval);
    }
  }

  /**
   * Modalità simulata per ambiente di sviluppo.
   * Mostra "Connessione in corso..." per 2 secondi, poi attiva la webcam locale.
   */
  private async startSimulatedMode(): Promise<void> {
    this.isLoading = true;
    this.connectionState = 'connecting';
    this.isSimulatedMode = true;

    // Attendi 2 secondi per simulare la connessione
    await new Promise(resolve => setTimeout(resolve, 1000));

    this.isLoading = false;
    this.connectionState = 'demo';

    // Attiva la webcam locale
    await this.startLocalCamera();

    // Avvia il timer della durata
    this.startTime = new Date();
    this.durationInterval = setInterval(() => {
      if (this.startTime) {
        this.duration = Math.floor((Date.now() - this.startTime.getTime()) / 1000);
      }
    }, 1000);
  }

  /**
   * Avvia la webcam locale (senza LiveKit).
   * Se la webcam non è disponibile, continua comunque nella stanza senza video locale.
   */
  private async startLocalCamera(): Promise<void> {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        video: { width: 1280, height: 720 },
        audio: true
      });

      const videoElement = document.createElement('video');
      videoElement.srcObject = this.localStream;
      videoElement.autoplay = true;
      videoElement.muted = true; // Muta l'audio locale per evitare eco
      videoElement.playsInline = true;
      videoElement.style.width = '100%';
      videoElement.style.height = '100%';
      videoElement.style.objectFit = 'cover';
      videoElement.style.transform = 'scaleX(-1)';

      this.localVideoContainer.nativeElement.innerHTML = '';
      this.localVideoContainer.nativeElement.appendChild(videoElement);
      this.hasLocalVideo = true;
      this.cameraError = false;

    } catch (err) {
      console.warn('Webcam non disponibile, continuo senza video locale:', err);
      // Non bloccare - continua nella stanza senza video locale
      this.hasLocalVideo = false;
      this.isVideoOff = true;
      this.cameraError = false; // Non mostrare errore, semplicemente non c'è video
    }
  }

  /**
   * Ferma lo stream locale della webcam.
   */
  private stopLocalStream(): void {
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
    }
  }

  /**
   * Riprova ad accedere alla webcam.
   */
  retryCamera(): void {
    this.cameraError = false;
    this.startLocalCamera();
  }

  private async connectToRoom(url: string, token: string): Promise<void> {
    this.isLoading = true;
    this.connectionState = 'connecting';

    try {
      this.room = new Room({
        adaptiveStream: true,
        dynacast: true,
        videoCaptureDefaults: {
          resolution: VideoPresets.h720.resolution
        }
      });

      this.room.on(RoomEvent.TrackSubscribed, this.handleTrackSubscribed.bind(this));
      this.room.on(RoomEvent.TrackUnsubscribed, this.handleTrackUnsubscribed.bind(this));
      this.room.on(RoomEvent.Disconnected, this.handleDisconnected.bind(this));
      this.room.on(RoomEvent.ParticipantConnected, this.handleParticipantConnected.bind(this));
      this.room.on(RoomEvent.ParticipantDisconnected, this.handleParticipantDisconnected.bind(this));

      await this.room.connect(url, token);
      this.connectionState = 'connected';

      await this.room.localParticipant.enableCameraAndMicrophone();
      this.hasLocalVideo = true;

      const localVideoTrack = this.room.localParticipant.getTrackPublication(Track.Source.Camera)?.track;
      if (localVideoTrack) {
        this.attachTrack(localVideoTrack, this.localVideoContainer.nativeElement, true);
      }

      this.startTime = new Date();
      this.durationInterval = setInterval(() => {
        if (this.startTime) {
          this.duration = Math.floor((Date.now() - this.startTime.getTime()) / 1000);
        }
      }, 1000);

    } catch (err) {
      console.warn('Connessione WebRTC non disponibile, avvio modalità simulata:', err);
      // Fallback alla modalità simulata
      await this.startSimulatedMode();
    } finally {
      this.isLoading = false;
    }
  }

  private handleTrackSubscribed(track: RemoteTrack, publication: RemoteTrackPublication, participant: RemoteParticipant): void {
    if (track.kind === Track.Kind.Video) {
      this.hasRemoteVideo = true;
      this.attachTrack(track, this.remoteVideoContainer.nativeElement, false);
    } else if (track.kind === Track.Kind.Audio) {
      this.attachTrack(track, this.remoteVideoContainer.nativeElement, false);
    }
  }

  private handleTrackUnsubscribed(track: RemoteTrack): void {
    if (track.kind === Track.Kind.Video) {
      this.hasRemoteVideo = false;
      track.detach();
    }
  }

  private handleDisconnected(): void {
    this.connectionState = 'disconnected';
  }

  private handleParticipantConnected(participant: RemoteParticipant): void {
    console.log('Participant connected:', participant.identity);
  }

  private handleParticipantDisconnected(participant: RemoteParticipant): void {
    console.log('Participant disconnected:', participant.identity);
    this.hasRemoteVideo = false;
  }

  private attachTrack(track: LocalTrack | RemoteTrack, container: HTMLElement, isLocal: boolean): void {
    const element = track.attach();
    if (element instanceof HTMLVideoElement) {
      element.style.width = '100%';
      element.style.height = '100%';
      element.style.objectFit = isLocal ? 'cover' : 'contain';
      if (isLocal) {
        element.style.transform = 'scaleX(-1)';
      }
    }
    container.appendChild(element);
  }

  toggleMute(): void {
    this.isMuted = !this.isMuted;

    if (this.isSimulatedMode) {
      // Modalità simulata: muta/smuta le tracce audio (se disponibili)
      if (this.localStream) {
        this.localStream.getAudioTracks().forEach(track => {
          track.enabled = !this.isMuted;
        });
      }
      // In ogni caso aggiorna lo stato visivo
    } else if (this.room) {
      // Modalità LiveKit
      this.room.localParticipant.setMicrophoneEnabled(!this.isMuted);
    }
  }

  toggleVideo(): void {
    this.isVideoOff = !this.isVideoOff;

    if (this.isSimulatedMode) {
      // Modalità simulata: attiva/disattiva le tracce video (se disponibili)
      if (this.localStream) {
        this.localStream.getVideoTracks().forEach(track => {
          track.enabled = !this.isVideoOff;
        });
        this.hasLocalVideo = !this.isVideoOff;
      }
      // Se non c'è stream, aggiorna comunque lo stato visivo del pulsante
    } else if (this.room) {
      // Modalità LiveKit
      this.room.localParticipant.setCameraEnabled(!this.isVideoOff);
      this.hasLocalVideo = !this.isVideoOff;
    }
  }

  endCall(): void {
    this.disconnect();
    this.goBack();
  }

  goBack(): void {
    this.disconnect();
    this.stopLocalStream();
    this.router.navigate(['/portal/admin/televisit']);
  }

  private disconnect(): void {
    if (this.room) {
      this.room.disconnect();
      this.room = null;
    }
    this.connectionState = 'disconnected';
    this.hasLocalVideo = false;
    this.hasRemoteVideo = false;
  }

  formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
}
