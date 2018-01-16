import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchComponent } from './search/search.component';
import { PodcastDetailComponent } from './podcast-detail/podcast-detail.component';
import { EpisodeDetailComponent } from './episode-detail/episode-detail.component';

const routes: Routes = [
  { path: 'search', component: SearchComponent },
  { path: 'search?query=:q', component: SearchComponent },
  { path: '', redirectTo: '/search', pathMatch: 'full' },
  { path: 'p/:id', component: PodcastDetailComponent },
  { path: 'e/:id', component: EpisodeDetailComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
