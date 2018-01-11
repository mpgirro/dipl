import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ResultsComponent }      from './results/results.component';
import { SearchComponent }      from './search/search.component';
import { PodcastDetailComponent }      from './podcast-detail/podcast-detail.component';
import { EpisodeDetailComponent }      from './episode-detail/episode-detail.component';

const routes: Routes = [
  { path: 'results', component: ResultsComponent },
  { path: 'search', component: SearchComponent },
  { path: '', redirectTo: '/results', pathMatch: 'full' },
  { path: 'p/:id', component: PodcastDetailComponent },
  { path: 'e/:id', component: EpisodeDetailComponent }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  declarations: []
})
export class AppRoutingModule { }
