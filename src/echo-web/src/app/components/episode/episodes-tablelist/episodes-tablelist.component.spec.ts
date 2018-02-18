import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodesTablelistComponent } from './episodes-tablelist.component';

describe('EpisodesTablelistComponent', () => {
  let component: EpisodesTablelistComponent;
  let fixture: ComponentFixture<EpisodesTablelistComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodesTablelistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodesTablelistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
