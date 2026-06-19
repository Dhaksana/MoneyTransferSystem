import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: 'API_BASE_URL', useValue: 'http://localhost:8080/api/v1' }
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login successfully', () => {
    const mockResponse = {
      authenticated: true,
      token: 'test-token',
      user: { id: 'ACC123', name: 'TestUser', role: 'USER' }
    };

    service.login('testuser', 'Test@123').subscribe(result => {
      expect(result).toBeTrue();
      expect(localStorage.getItem('auth_token')).toBe('test-token');
      expect(localStorage.getItem('auth_flag')).toBe('1');
      expect(localStorage.getItem('user_name')).toBe('TestUser');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'testuser', password: 'Test@123' });
    req.flush(mockResponse);
  });

  it('should fail login with invalid credentials', () => {
    service.login('wrong', 'wrong').subscribe({
      error: (err) => {
        expect(err.message).toBe('Invalid credentials');
      }
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/login');
    req.flush({ authenticated: false, message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
  });

  it('should register successfully', () => {
    const mockResponse = {
      authenticated: true,
      token: 'reg-token',
      user: { id: 'ACC456', name: 'NewUser', role: 'USER' }
    };

    service.register('newuser', 'Pass@123', 'New User').subscribe(result => {
      expect(result).toBeTrue();
      expect(localStorage.getItem('auth_token')).toBe('reg-token');
    });

    const req = httpMock.expectOne(r => r.url === 'http://localhost:8080/api/v1/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should clear state on logout', () => {
    localStorage.setItem('auth_token', 'token');
    localStorage.setItem('auth_flag', '1');
    localStorage.setItem('user_name', 'User');
    localStorage.setItem('user_id', 'ACC123');
    localStorage.setItem('user_role', 'USER');

    service.logout();

    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(localStorage.getItem('auth_flag')).toBeNull();
    expect(service.isLoggedInSync()).toBeFalse();
  });

  it('should expose token, userName, role, userId', () => {
    localStorage.setItem('auth_token', 'mytoken');
    localStorage.setItem('user_name', 'Alice');
    localStorage.setItem('user_role', 'ADMIN');
    localStorage.setItem('user_id', 'ACC999');

    expect(service.token).toBe('mytoken');
    expect(service.userName).toBe('Alice');
    expect(service.role).toBe('ADMIN');
    expect(service.userId).toBe('ACC999');
  });

  it('should return defaults when no user data stored', () => {
    expect(service.token).toBeNull();
    expect(service.userName).toBe('User');
    expect(service.role).toBe('USER');
    expect(service.userId).toBeNull();
  });

  it('should reflect login state via isLoggedIn$', (done) => {
    service.isLoggedIn$.subscribe(loggedIn => {
      if (loggedIn) {
        expect(service.isLoggedInSync()).toBeTrue();
        done();
      }
    });

    const mockResponse = {
      authenticated: true,
      token: 'token',
      user: { id: 'ACC1', name: 'Test', role: 'USER' }
    };

    service.login('test', 'Pass@123').subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/login');
    req.flush(mockResponse);
  });
});
