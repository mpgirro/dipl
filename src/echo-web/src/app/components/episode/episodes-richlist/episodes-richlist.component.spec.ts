import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodesRichlistComponent } from './episodes-richlist.component';

describe('EpisodesRichlistComponent', () => {
  let component: EpisodesRichlistComponent;
  let fixture: ComponentFixture<EpisodesRichlistComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodesRichlistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodesRichlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
