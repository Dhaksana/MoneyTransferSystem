import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { AppNotification, BankingApiService } from './banking-api.service';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();
  private client?: Client;
  private authSub: Subscription;

  constructor(private api: BankingApiService, private auth: AuthService) {
    console.log('[MTS] NotificationService constructed, subscribing to auth.isLoggedIn$');
    this.authSub = this.auth.isLoggedIn$.subscribe((loggedIn) => {
      if (loggedIn) {
        this.load();
        this.connect();
      } else {
        this.disconnect();
        this.notificationsSubject.next([]);
      }
    });
  }

  get unreadCount() {
    return this.notificationsSubject.value.filter(n => !n.read).length;
  }

  load() {
    this.api.getNotifications().subscribe(items => this.notificationsSubject.next(items));
  }

  markRead(notification: AppNotification) {
    this.api.markNotificationRead(notification.id).subscribe(updated => {
      this.notificationsSubject.next(this.notificationsSubject.value.map(n => n.id === updated.id ? updated : n));
    });
  }

  delete(notification: AppNotification) {
    this.api.deleteNotification(notification.id).subscribe(() => {
      this.notificationsSubject.next(this.notificationsSubject.value.filter(n => n.id !== notification.id));
    });
  }

  private connect() {
    if (this.client?.active) return;
    this.client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      reconnectDelay: 5000,
      onConnect: () => {
        this.client?.subscribe('/user/queue/notifications', message => {
          const notification = JSON.parse(message.body) as AppNotification;
          this.notificationsSubject.next([notification, ...this.notificationsSubject.value]);
        });
      }
    });
    this.client.activate();
  }

  private disconnect() {
    this.client?.deactivate();
  }

  ngOnDestroy(): void {
    this.authSub.unsubscribe();
    this.disconnect();
  }
}
