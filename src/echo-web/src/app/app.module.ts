import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

import { SearchService } from './search.service';
import { SearchComponent } from './search/search.component';

import { PodcastService } from './podcast.service';
import { PodcastDetailComponent } from './podcast-detail/podcast-detail.component';

import { EpisodeService } from './episode.service';
import { EpisodeDetailComponent } from './episode-detail/episode-detail.component';

import { DomainService } from './domain.service';

import { AppRoutingModule } from './/app-routing.module';
import { HttpClientModule } from '@angular/common/http';

import { Angular2FontawesomeModule } from 'angular2-fontawesome/angular2-fontawesome';
import { PodcastDirectoryComponent } from './podcast-directory/podcast-directory.component';
import { EpisodeTeaserComponent } from './episode-teaser/episode-teaser.component';
import { EpisodeTableComponent } from './episode-table/episode-table.component';
import { EpisodeListgroupComponent } from './episode-listgroup/episode-listgroup.component';
import { EpisodeTablelistComponent } from './episode-tablelist/episode-tablelist.component';

@NgModule({
  declarations: [
    AppComponent,
    PodcastDetailComponent,
    EpisodeDetailComponent,
    SearchComponent,
    PodcastDirectoryComponent,
    EpisodeTeaserComponent,
    EpisodeTableComponent,
    EpisodeListgroupComponent,
    EpisodeTablelistComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    Angular2FontawesomeModule
  ],
  providers: [SearchService, PodcastService, EpisodeService, DomainService],
  bootstrap: [AppComponent]
})
export class AppModule { }
