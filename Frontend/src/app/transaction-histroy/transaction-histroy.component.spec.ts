import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withXhr } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TransactionHistoryComponent } from './transaction-histroy.component';
import { AuthService } from '../services/auth.service';
import { BankingApiService } from '../services/banking-api.service';
import { of } from 'rxjs';

describe('TransactionHistoryComponent', () => {
  let component: TransactionHistoryComponent;
  let fixture: ComponentFixture<TransactionHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionHistoryComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(withXhr()),
        provideRouter([]),
        { provide: 'API_BASE_URL', useValue: 'http://localhost:8080/api/v1' },
        { provide: AuthService, useValue: { userId: '1', token: 'x' } },
        { provide: BankingApiService, useValue: { getHistoryByAccount: () => of([]) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TransactionHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
