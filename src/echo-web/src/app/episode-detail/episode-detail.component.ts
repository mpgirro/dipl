import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Episode } from '../episode';
import { EpisodeService } from '../episode.service';
import { DomainService } from '../domain.service';

@Component({
  selector: 'app-episode-detail',
  templateUrl: './episode-detail.component.html',
  styleUrls: ['./episode-detail.component.css']
})
export class EpisodeDetailComponent implements OnInit {

  @Input() episode: Episode;

  constructor(private route: ActivatedRoute,
              private episodeService: EpisodeService,
              private domainService: DomainService,
              private location: Location) { }

  ngOnInit() {
    this.getEpisode();
    this.initPlyr();
  }

  initPlyr(): void {
    const el = document.createElement('script');
    el.appendChild(document.createTextNode('plyr.setup("#plyr-audio");'));
    document.body.appendChild(el);
  }

  getEpisode(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.episodeService.get(id)
      .subscribe(episode => {
        this.episode = episode;
      });
  }

  goBack(): void {
    this.location.back();
  }

}
