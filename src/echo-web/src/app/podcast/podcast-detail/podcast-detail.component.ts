import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Podcast } from '../shared/podcast.model';
import { Episode } from '../../episode/shared/episode.model';
import { PodcastService } from '../shared/podcast.service';
import { DomainService } from '../../domain.service';
import {Feed} from '../shared/feed.model';

@Component({
  selector: 'app-podcast-detail',
  templateUrl: './podcast-detail.component.html',
  styleUrls: ['./podcast-detail.component.css']
})
export class PodcastDetailComponent implements OnInit {

  // TODO warum habe ich hier @Input davor stehen?
  @Input() podcast: Podcast;
  @Input() episodes: Array<Episode>;
  feeds: Feed[];

  HIGHLIGHT_COLOR = '#007bff';

  constructor(private route: ActivatedRoute,
              private podcastService: PodcastService,
              private domainService: DomainService,
              private location: Location) { }

  ngOnInit() {
    this.getPodcast();
  }

  initPodloveSubscribeButton(): void {
    let feedsArr = '';
    this.feeds.forEach(f => {
      feedsArr += `{
        "url" : "${f.url}"
      },`;
    });
    feedsArr += '';

    const podloveSubscribeButtonJS = `
      window.podcastData = {
        "title" : "${this.podcast.title}",
        "description" : "${this.podcast.description}",
        "cover" : "${this.podcast.itunesImage}",
        "feeds" : [ ${feedsArr} ]
      }`;

    const buttom = document.getElementById('podlove-button');

    const el1 = document.createElement('script');
    el1.appendChild(document.createTextNode(podloveSubscribeButtonJS));
    buttom.appendChild(el1);

    const el2 = document.createElement('script');
    el2.setAttribute('class', 'podlove-subscribe-button');
    el2.setAttribute('src', '/assets/podlove/subscribe-button/javascripts/app.js');
    el2.setAttribute('data-language', 'en');
    el2.setAttribute('data-size', 'small');
    el2.setAttribute('data-json-data', 'podcastData');
    el2.setAttribute('data-color', this.HIGHLIGHT_COLOR);
    el2.setAttribute('data-format', 'square');
    el2.setAttribute('data-style', 'frameless');
    buttom.appendChild(el2);

    const el3 = document.createElement('noscript');
    el3.appendChild(document.createTextNode(`<a href="${this.podcast.link}">Subscribe to feed</a>`));
    buttom.appendChild(el3);
  }

  getPodcast(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.podcastService.get(id)
      .subscribe(podcast => {
        this.podcast = podcast;
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
    this.podcastService.getFeeds(id)
      .subscribe(feeds => {
        this.feeds = feeds;
        this.initPodloveSubscribeButton();
      });
  }

}
