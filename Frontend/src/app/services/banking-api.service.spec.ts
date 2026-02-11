import { TestBed } from '@angular/core/testing';

import { BankingApiService } from './banking-api.service';

describe('BankingApiService', () => {
  let service: BankingApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BankingApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
