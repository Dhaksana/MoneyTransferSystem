import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BankingApiService } from './banking-api.service';

describe('BankingApiService', () => {
  let service: BankingApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        BankingApiService,
        { provide: 'API_BASE_URL', useValue: 'http://localhost:8080/api/v1' }
      ]
    });
    service = TestBed.inject(BankingApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get balance', () => {
    service.getBalance('ACC123').subscribe(balance => {
      expect(balance).toBe(1500.50);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/accounts/ACC123');
    expect(req.request.method).toBe('GET');
    req.flush({ balance: 1500.50 });
  });

  it('should handle null balance', () => {
    service.getBalance('ACC123').subscribe(balance => {
      expect(balance).toBeNull();
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/accounts/ACC123');
    req.flush({});
  });

  it('should get transaction history', () => {
    const mockHistory = [
      { transactionId: 1, fromAccountId: 'ACC1', toAccountId: 'ACC2', amount: 500, status: 'SUCCESS', failureReason: null, createdOn: '2024-01-01T10:00:00' }
    ];

    service.getHistoryByAccount('ACC1').subscribe(items => {
      expect(items.length).toBe(1);
      expect(items[0].amount).toBe(500);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/transfers/history/ACC1');
    req.flush(mockHistory);
  });

  it('should check account existence', () => {
    service.accountExists('ACC123').subscribe(exists => {
      expect(exists).toBeTrue();
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/accounts/exists/ACC123');
    req.flush(true);
  });

  it('should transfer money successfully', () => {
    const mockResponse = { transactionId: 1, status: 'SUCCESS', message: 'Transfer completed' };

    service.transfer('ACC1', 'ACC2', 500).subscribe(res => {
      expect(res.status).toBe('SUCCESS');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/transfers');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.fromAccountId).toBe('ACC1');
    expect(req.request.body.toAccountId).toBe('ACC2');
    expect(req.request.body.amount).toBe(500);
    expect(req.request.headers.has('Idempotency-Key')).toBeTrue();
    req.flush(mockResponse);
  });

  it('should transfer using account numbers', () => {
    const mockResponse = { transactionId: 2, status: 'SUCCESS', message: 'Done' };

    service.transfer('NUM001', 'NUM002', 250, undefined, true).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/v1/transfers');
    expect(req.request.body.fromAccountNumber).toBe('NUM001');
    expect(req.request.body.toAccountNumber).toBe('NUM002');
    expect(req.request.body.fromAccountId).toBeUndefined();
    req.flush(mockResponse);
  });

  it('should get beneficiaries', () => {
    const mockBeneficiaries = [
      { id: 1, ownerAccountId: 'ACC1', beneficiaryName: 'Bob', beneficiaryAccountNumber: 'ACC5', favorite: false }
    ];

    service.getBeneficiaries().subscribe(b => {
      expect(b.length).toBe(1);
      expect(b[0].beneficiaryName).toBe('Bob');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/beneficiaries');
    req.flush(mockBeneficiaries);
  });

  it('should save beneficiary (create)', () => {
    const ben = { ownerAccountId: 'ACC1', beneficiaryName: 'Bob', beneficiaryAccountNumber: 'ACC5', favorite: false };

    service.saveBeneficiary(ben).subscribe(() => {});

    const req = httpMock.expectOne('http://localhost:8080/api/v1/beneficiaries');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, ...ben });
  });

  it('should save beneficiary (update)', () => {
    const ben = { id: 1, ownerAccountId: 'ACC1', beneficiaryName: 'Bob', beneficiaryAccountNumber: 'ACC5', favorite: true };

    service.saveBeneficiary(ben).subscribe(() => {});

    const req = httpMock.expectOne('http://localhost:8080/api/v1/beneficiaries/1');
    expect(req.request.method).toBe('PUT');
    req.flush(ben);
  });

  it('should delete beneficiary', () => {
    service.deleteBeneficiary(1).subscribe(() => {});

    const req = httpMock.expectOne('http://localhost:8080/api/v1/beneficiaries/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should get reward summary', () => {
    service.getRewardSummary().subscribe(s => {
      expect(s.currentPoints).toBe(25);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/rewards/summary');
    req.flush({ currentPoints: 25, lifetimePoints: 100 });
  });

  it('should get notifications', () => {
    service.getNotifications().subscribe(n => {
      expect(n.length).toBe(2);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/notifications');
    req.flush([{ id: 1, message: 'Test', read: false }, { id: 2, message: 'Test2', read: true }]);
  });

  it('should get unread count', () => {
    service.getUnreadCount().subscribe(r => {
      expect(r.count).toBe(5);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/notifications/unread-count');
    req.flush({ count: 5 });
  });

  it('should mark notification read', () => {
    service.markNotificationRead(1).subscribe(() => {});

    const req = httpMock.expectOne('http://localhost:8080/api/v1/notifications/1/read');
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should search audit logs', () => {
    const mockPage = { content: [{ id: 1, action: 'LOGIN_SUCCESS', username: 'admin', details: '', ipAddress: '', timestamp: '' }], totalElements: 1, totalPages: 1, number: 0 };

    service.searchAuditLogs({ action: 'LOGIN', page: 0, size: 20 }).subscribe(r => {
      expect(r.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('/admin/audit-logs/search'));
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should export audit logs', () => {
    service.exportAuditLogs({}).subscribe(logs => {
      expect(Array.isArray(logs)).toBeTrue();
    });

    const req = httpMock.expectOne(r => r.url.includes('/admin/audit-logs/export'));
    req.flush([]);
  });

  it('should delete audit log', () => {
    service.deleteAuditLog(1).subscribe(() => {});

    const req = httpMock.expectOne('http://localhost:8080/api/v1/admin/audit-logs/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
