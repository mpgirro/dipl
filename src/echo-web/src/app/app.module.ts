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

import { AppRoutingModule } from './/app-routing.module';
import { HttpClientModule } from '@angular/common/http'

@NgModule({
  declarations: [
    AppComponent,
    PodcastDetailComponent,
    EpisodeDetailComponent,
    SearchComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [SearchService, PodcastService, EpisodeService],
  bootstrap: [AppComponent]
})
export class AppModule { }
