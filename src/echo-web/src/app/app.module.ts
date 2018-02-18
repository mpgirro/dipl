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
import { EpisodesRichlistComponent } from './episodes-richlist/episodes-richlist.component';
import { EpisodesTablelistComponent } from './episodes-tablelist/episodes-tablelist.component';
import { NavbarComponent } from './navbar/navbar.component';

@NgModule({
  declarations: [
    AppComponent,
    PodcastDetailComponent,
    EpisodeDetailComponent,
    SearchComponent,
    PodcastDirectoryComponent,
    EpisodeTeaserComponent,
    EpisodesRichlistComponent,
    EpisodesTablelistComponent,
    NavbarComponent
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
