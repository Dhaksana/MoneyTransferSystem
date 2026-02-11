import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionHistroyComponent } from './transaction-histroy.component';

describe('TransactionHistroyComponent', () => {
  let component: TransactionHistroyComponent;
  let fixture: ComponentFixture<TransactionHistroyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionHistroyComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TransactionHistroyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
