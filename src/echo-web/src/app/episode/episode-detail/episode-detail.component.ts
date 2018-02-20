import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Episode } from '../shared/episode.model';
import { EpisodeService } from '../shared/episode.service';
import { DomainService } from '../../domain.service';

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
    // this.initPlyrPlayer();
  }

  initPlyrPlayer(): void {
    const plyrJS = 'plyr.setup("#plyr-audio");';
    const el = document.createElement('script');
    el.appendChild(document.createTextNode(plyrJS));
    document.body.appendChild(el);
  }

  initPodlovePlayer(): void {
    const podlovePlayerJS = `podlovePlayer('#podlove-player',{
      "duration" : "${this.episode.itunesDuration}",
      "audio" : [{
        "url" : "${this.episode.enclosureUrl}",
        "size" : ${this.episode.enclosureLength},
        "mimeType" :"${this.episode.enclosureType}"
      }],
      "theme" : {
        "main" : "#ffffff",
        "highlight" : "#0785ff"
      },
      "visibleComponents": [
        "tabChapters",
        "tabAudio",
        "progressbar",
        "controlSteppers",
        "controlChapters"
      ]});`;
    // console.log(podlovePlayerJS);
    const el = document.createElement('script');
    el.appendChild(document.createTextNode(podlovePlayerJS));
    document.body.appendChild(el);
  }

  getEpisode(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.episodeService.get(id)
      .subscribe(episode => {
        this.episode = episode;
        this.initPodlovePlayer();
      });
  }

  goBack(): void {
    this.location.back();
  }

}
