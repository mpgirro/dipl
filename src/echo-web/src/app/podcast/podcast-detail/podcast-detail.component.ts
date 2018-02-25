import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Podcast } from '../shared/podcast.model';
import { Episode } from '../../episode/shared/episode.model';
import { PodcastService } from '../shared/podcast.service';
import { DomainService } from '../../domain.service';

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

  initPodloveButton(): void {
    // TODO we do not yet pass any feed information from the backend to the frontend
    const podloveButtonJS = `
        <script>
            window.podcastData={
              "title" : "foobar",
              "subtitle" : "foobar",
              "description" : "foobar",
              "cover" : "",
              "feeds" : [{
                "type" : "audio",
                "format" : "mp3",
                "url" : "http://example.com"
              }]
            }
        </script>
        <script class="podlove-subscribe-button"
            src="https://cdn.podlove.org/subscribe-button/javascripts/app.js"
            data-language="${this.podcast.language}"
            data-size="medium"
            data-json-data="podcastData"
            data-color="#469cd1"
            data-format="square"
            data-style="frameless">
        </script>
        <noscript>
            <a href="${this.podcast.link}">Subscribe to feed</a>
        </noscript>`;
  }

  getPodcast(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.podcastService.get(id)
      .subscribe(podcast => {
        this.podcast = podcast;
        this.initPodloveButton();
      });
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
