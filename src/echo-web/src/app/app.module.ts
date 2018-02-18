import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

import { SearchComponent } from './search/search.component';
import { PodcastDetailComponent } from './components/podcast/podcast-detail/podcast-detail.component';
import { EpisodeDetailComponent } from './components/episode/episode-detail/episode-detail.component';
import { PodcastDirectoryComponent } from './podcast-directory/podcast-directory.component';
import { EpisodeTeaserComponent } from './components/episode/episode-teaser/episode-teaser.component';
import { EpisodeRichlistComponent } from './components/episode/episode-richlist/episode-richlist.component';
import { EpisodeTablelistComponent } from './components/episode/episode-tablelist/episode-tablelist.component';
import { NavbarComponent } from './navbar/navbar.component';

import { CoreModule } from './core/core.module';

import { AppRoutingModule } from './/app-routing.module';
import { HttpClientModule } from '@angular/common/http';

import { Angular2FontawesomeModule } from 'angular2-fontawesome/angular2-fontawesome';


@NgModule({
  declarations: [
    AppComponent,
    PodcastDetailComponent,
    EpisodeDetailComponent,
    SearchComponent,
    PodcastDirectoryComponent,
    EpisodeTeaserComponent,
    EpisodeRichlistComponent,
    EpisodeTablelistComponent,
    NavbarComponent
  ],
  imports: [
    CoreModule,
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    Angular2FontawesomeModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
