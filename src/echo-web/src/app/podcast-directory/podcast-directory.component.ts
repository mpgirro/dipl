import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Podcast } from '../core/podcast';
import { PodcastService } from '../core/podcast.service';

@Component({
  selector: 'app-podcast-directory',
  templateUrl: './podcast-directory.component.html',
  styleUrls: ['./podcast-directory.component.css']
})
export class PodcastDirectoryComponent implements OnInit {

  @Input() podcasts: Array<Podcast>;

  constructor(private route: ActivatedRoute,
              private podcastService: PodcastService,
              private location: Location) { }

  ngOnInit() {
    this.getAllPodcasts();
  }

  getAllPodcasts(): void {
    this.podcastService.getAll()
      .subscribe(podcasts => {

        // reverse sort by date
        podcasts.results.sort((a: Podcast, b: Podcast) => {
          if (a.title < b.title) {
            return -1;
          } else if (a.title > b.title) {
            return 1;
          } else {
            return 0;
          }
        });

        this.podcasts = podcasts.results;
    });
  }

  goBack(): void {
    this.location.back();
  }

}
