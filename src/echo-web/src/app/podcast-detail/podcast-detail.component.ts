import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Podcast } from '../podcast';
import { Episode } from '../episode';
import { PodcastService } from '../podcast.service';
import { DomainService } from '../domain.service';

@Component({
  selector: 'app-podcast-detail',
  templateUrl: './podcast-detail.component.html',
  styleUrls: ['./podcast-detail.component.css']
})
export class PodcastDetailComponent implements OnInit {

  // TODO warum habe ich hier @Input davor stehen?
  @Input() podcast: Podcast;
  @Input() episodes: Array<Episode>;

  constructor(private route: ActivatedRoute,
              private podcastService: PodcastService,
              private domainService: DomainService,
              private location: Location) { }

  ngOnInit() {
    this.getPodcast();
  }

  getPodcast(): void {
    const id = this.route.snapshot.paramMap.get('id');
    // this.echoId = id;
    this.podcastService.get(id)
      .subscribe(podcast => this.podcast = podcast);
    this.podcastService.getEpisodes(id)
      .subscribe(episodes => {

        // reverse sort by date
        episodes.sort((a: Episode, b: Episode) => {
          if (a.pubDate > b.pubDate) {
            return -1;
          } else if (a.pubDate < b.pubDate) {
            return 1;
          } else {

            // in case they are the same, sort by name
            if (a.title < b.title) {
              return -1;
            } else if (a.title > b.title) {
              return 1;
            } else {
              return 0;
            }

          }
        });

        this.episodes = episodes;
      });
  }

  goBack(): void {
    this.location.back();
  }

}
