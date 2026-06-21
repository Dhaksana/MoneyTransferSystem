import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withXhr } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TransferMoneyComponent } from './transfer-money.component';
import { BankingApiService } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';
import { of } from 'rxjs';

describe('TransferMoneyComponent', () => {
  let component: TransferMoneyComponent;
  let fixture: ComponentFixture<TransferMoneyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransferMoneyComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(withXhr()),
        provideRouter([]),
        { provide: 'API_BASE_URL', useValue: 'http://localhost:8080/api/v1' },
        { provide: AuthService, useValue: { userId: '1', userName: 'Test', token: 'x' } },
        { provide: BankingApiService, useValue: { getBeneficiaries: () => of([]), transfer: () => of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TransferMoneyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
