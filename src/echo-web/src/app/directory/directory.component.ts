import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Podcast } from '../podcast/shared/podcast.model';
import {PodcastService} from '../podcast/shared/podcast.service';

@Component({
  selector: 'app-directory',
  templateUrl: './directory.component.html',
  styleUrls: ['./directory.component.css']
})
export class DirectoryComponent implements OnInit {

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
