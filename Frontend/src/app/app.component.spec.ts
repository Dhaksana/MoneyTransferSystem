import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { AuthService } from './services/auth.service';
import { NotificationService } from './services/notification.service';
import { provideRouter } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

describe('AppComponent', () => {
  let authServiceStub: jasmine.SpyObj<AuthService>;
  let notificationServiceStub: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    authServiceStub = jasmine.createSpyObj('AuthService', ['isLoggedInSync', 'logout'], {
      isLoggedIn$: new BehaviorSubject(false),
      userName: 'Guest',
      role: 'USER'
    });

    notificationServiceStub = jasmine.createSpyObj('NotificationService', ['markRead'], {
      notifications$: new BehaviorSubject([]),
      unreadCount: 0
    });

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceStub },
        { provide: NotificationService, useValue: notificationServiceStub }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should have navItems defined', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.navItems.length).toBeGreaterThan(0);
    expect(app.navItems[0].label).toBe('Dashboard');
  });

  it('should call logout and navigate on logout()', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    const routerSpy = spyOn(app.router, 'navigate');
    app.logout();
    expect(authServiceStub.logout).toHaveBeenCalled();
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });
});
