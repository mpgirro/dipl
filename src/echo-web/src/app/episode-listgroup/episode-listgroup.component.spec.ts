import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeListgroupComponent } from './episode-listgroup.component';

describe('EpisodeListgroupComponent', () => {
  let component: EpisodeListgroupComponent;
  let fixture: ComponentFixture<EpisodeListgroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeListgroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeListgroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
