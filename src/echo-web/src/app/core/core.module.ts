import { NgModule, Optional, SkipSelf } from '@angular/core';
import { throwIfAlreadyLoaded } from './module-import.guard';
import { LoggingService } from './helpers/logging/logging.service';
import { PodcastService } from './services/podcast/podcast.service';
import { EpisodeService } from './services/episode/episode.service';
import { SearchService } from './services/search/search.service';
import { DomainService } from './services/domain.service';

@NgModule({
  providers: [
    LoggingService,
    PodcastService,
    EpisodeService,
    SearchService,
    DomainService
  ],
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    throwIfAlreadyLoaded(parentModule, 'CoreModule');
  }
}
