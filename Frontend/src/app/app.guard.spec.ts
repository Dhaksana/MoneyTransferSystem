import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, provideRouter } from '@angular/router';
import { AuthService } from './services/auth.service';

import { authGuard, adminGuard } from './app.guard';

describe('authGuard', () => {
  let authServiceStub: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authServiceStub = jasmine.createSpyObj('AuthService', ['isLoggedInSync']);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceStub },
        provideRouter([])
      ]
    });
  });

  it('should return true for logged-in user', () => {
    authServiceStub.isLoggedInSync.and.returnValue(true);
    const route = {} as ActivatedRouteSnapshot;
    const state = { url: '/profile' } as RouterStateSnapshot;
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(result).toBeTrue();
  });

  it('should return false for anonymous user', () => {
    authServiceStub.isLoggedInSync.and.returnValue(false);
    const route = {} as ActivatedRouteSnapshot;
    const state = { url: '/profile' } as RouterStateSnapshot;
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(result).toBeFalse();
  });
});

describe('adminGuard', () => {
  let authServiceStub: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authServiceStub = jasmine.createSpyObj('AuthService', ['isLoggedInSync'], { role: 'USER' });
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceStub },
        provideRouter([])
      ]
    });
  });

  it('should return true for admin user', () => {
    authServiceStub.isLoggedInSync.and.returnValue(true);
    Object.defineProperty(authServiceStub, 'role', { get: () => 'ADMIN' });
    const result = TestBed.runInInjectionContext(() => adminGuard(null!, null!));
    expect(result).toBeTrue();
  });

  it('should return false for non-admin user', () => {
    authServiceStub.isLoggedInSync.and.returnValue(true);
    Object.defineProperty(authServiceStub, 'role', { get: () => 'USER' });
    const result = TestBed.runInInjectionContext(() => adminGuard(null!, null!));
    expect(result).toBeFalse();
  });

  it('should return false for anonymous user', () => {
    authServiceStub.isLoggedInSync.and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => adminGuard(null!, null!));
    expect(result).toBeFalse();
  });
});
