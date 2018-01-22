import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PodcastDirectoryComponent } from './podcast-directory.component';

describe('PodcastDirectoryComponent', () => {
  let component: PodcastDirectoryComponent;
  let fixture: ComponentFixture<PodcastDirectoryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PodcastDirectoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PodcastDirectoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
