import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Podcast } from '../podcast';
import { PodcastService } from '../podcast.service';

@Component({
  selector: 'app-podcast-detail',
  templateUrl: './podcast-detail.component.html',
  styleUrls: ['./podcast-detail.component.css']
})
export class PodcastDetailComponent implements OnInit {

  @Input() podcast: Podcast;

  constructor(private route: ActivatedRoute,
              private podcastService: PodcastService,
              private location: Location) { }

  ngOnInit() {
    this.getPodcast();
  }

  getPodcast(): void {
    const id = this.route.snapshot.paramMap.get('id');
    // this.echoId = id;
    this.podcastService.get(id)
      .subscribe(podcast => this.podcast = podcast);
  }

  goBack(): void {
    this.location.back();
  }

}
