import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Room, RoomEvent, VideoPresets, Track, RemoteTrack, RemoteTrackPublication, LocalTrack, RemoteParticipant } from 'livekit-client';

@Component({
  selector: 'app-televisit-room',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="televisit-container d-flex flex-column vh-100 bg-dark">
      <!-- Header -->
      <div class="header bg-dark text-white py-2 px-4 d-flex align-items-center justify-content-between border-bottom border-secondary">
        <div class="d-flex align-items-center gap-3">
          <span class="badge" [class.bg-success]="connectionState === 'connected'" [class.bg-warning]="connectionState === 'connecting'" [class.bg-info]="connectionState === 'demo'" [class.bg-danger]="connectionState === 'disconnected'">
            <i class="bi" [class.bi-wifi]="connectionState === 'connected'" [class.bi-hourglass-split]="connectionState === 'connecting'" [class.bi-info-circle]="connectionState === 'demo'" [class.bi-wifi-off]="connectionState === 'disconnected'"></i>
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
      <div class="flex-grow-1 d-flex position-relative overflow-hidden">
        <!-- Remote video (full size) -->
        <div class="remote-video-container flex-grow-1 d-flex align-items-center justify-content-center bg-secondary bg-opacity-25">
          <div #remoteVideoContainer class="w-100 h-100 d-flex align-items-center justify-content-center">
            <div *ngIf="!hasRemoteVideo" class="text-center text-white">
              <i class="bi bi-person-video fs-1 d-block mb-3"></i>
              <p class="mb-0">In attesa del collegamento dell'altro partecipante...</p>
              <small class="text-muted">La videochiamata inizierà automaticamente</small>
            </div>
          </div>
        </div>

        <!-- Local video (small overlay) -->
        <div class="local-video-container position-absolute bottom-0 end-0 m-3" style="width: 240px; height: 180px;">
          <div #localVideoContainer class="w-100 h-100 rounded overflow-hidden bg-dark border border-secondary">
            <div *ngIf="!hasLocalVideo" class="w-100 h-100 d-flex align-items-center justify-content-center">
              <i class="bi bi-camera-video-off text-muted fs-4"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- Controls -->
      <div class="controls bg-dark py-3 border-top border-secondary">
        <div class="d-flex justify-content-center gap-3">
          <button class="btn rounded-circle p-3" [class.btn-outline-light]="!isMuted" [class.btn-danger]="isMuted" (click)="toggleMute()" title="Microfono" [disabled]="connectionState === 'demo'">
            <i class="bi" [class.bi-mic-fill]="!isMuted" [class.bi-mic-mute-fill]="isMuted"></i>
          </button>
          <button class="btn rounded-circle p-3" [class.btn-outline-light]="!isVideoOff" [class.btn-warning]="isVideoOff" (click)="toggleVideo()" title="Videocamera" [disabled]="connectionState === 'demo'">
            <i class="bi" [class.bi-camera-video-fill]="!isVideoOff" [class.bi-camera-video-off-fill]="isVideoOff"></i>
          </button>
          <button class="btn btn-danger rounded-circle p-3" (click)="endCall()" title="Termina chiamata">
            <i class="bi bi-telephone-x-fill"></i>
          </button>
        </div>
      </div>

      <!-- Demo mode overlay -->
      <div *ngIf="showDemoMessage" class="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-dark bg-opacity-75" style="z-index: 1000;">
        <div class="card bg-dark text-white border-info" style="max-width: 450px;">
          <div class="card-body text-center">
            <i class="bi bi-info-circle text-info fs-1 d-block mb-3"></i>
            <h5 class="card-title">Ambiente di sviluppo</h5>
            <p class="card-text">
              La funzionalità di videoconferenza non è disponibile in ambiente locale.<br>
              Per testare le videochiamate, utilizza un ambiente di staging o produzione con connettività WebRTC completa.
            </p>
            <button class="btn btn-outline-light" (click)="goBack()">Torna alla lista</button>
          </div>
        </div>
      </div>

      <!-- Loading overlay -->
      <div *ngIf="isLoading" class="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-dark bg-opacity-75" style="z-index: 1000;">
        <div class="text-center text-white">
          <div class="spinner-border text-primary mb-3" role="status"></div>
          <p class="mb-0">Connessione in corso...</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .televisit-container {
      background: #1a1a1a;
    }
    .remote-video-container video {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
    .local-video-container video {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transform: scaleX(-1);
    }
    .btn.rounded-circle {
      width: 56px;
      height: 56px;
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
  showDemoMessage = false;

  isMuted = false;
  isVideoOff = false;
  hasLocalVideo = false;
  hasRemoteVideo = false;
  duration = 0;

  private room: Room | null = null;
  private durationInterval: ReturnType<typeof setInterval> | null = null;
  private startTime: Date | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  get connectionStateLabel(): string {
    switch (this.connectionState) {
      case 'connected': return 'Connesso';
      case 'connecting': return 'Connessione...';
      case 'demo': return 'Demo';
      default: return 'Disconnesso';
    }
  }

  ngOnInit(): void {
    const params = this.route.snapshot.queryParams;
    this.televisitId = params['id'] || '';
    const token = params['token'] || '';
    const livekitUrl = params['url'] || '';
    this.roomName = params['room'] || '';

    if (!token || !livekitUrl || !this.roomName) {
      this.showDemoMessage = true;
      this.connectionState = 'demo';
      return;
    }

    this.connectToRoom(livekitUrl, token);
  }

  ngOnDestroy(): void {
    this.disconnect();
    if (this.durationInterval) {
      clearInterval(this.durationInterval);
    }
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
      console.warn('Connessione WebRTC non disponibile in ambiente locale:', err);
      this.connectionState = 'demo';
      this.showDemoMessage = true;
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
    if (!this.room) return;
    this.isMuted = !this.isMuted;
    this.room.localParticipant.setMicrophoneEnabled(!this.isMuted);
  }

  toggleVideo(): void {
    if (!this.room) return;
    this.isVideoOff = !this.isVideoOff;
    this.room.localParticipant.setCameraEnabled(!this.isVideoOff);
    this.hasLocalVideo = !this.isVideoOff;
  }

  endCall(): void {
    this.disconnect();
    this.goBack();
  }

  goBack(): void {
    this.disconnect();
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
