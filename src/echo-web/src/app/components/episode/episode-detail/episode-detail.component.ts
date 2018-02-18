import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Episode } from '../../../core/episode';
import { EpisodeService } from '../../../core/episode.service';
import { DomainService } from '../../../core/domain.service';

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
    // this.initPlyr();
  }

  initPlyr(): void {
    const plyrJS = 'plyr.setup("#plyr-audio");';
    const el = document.createElement('script');
    el.appendChild(document.createTextNode(plyrJS));
    document.body.appendChild(el);
  }

  initPodlove(): void {
    const configuration = '{' +
//      '"poster" : "' + this.episode.itunesImage + '", ' +
      '"duration" : "' + this.episode.itunesDuration + '",' +
      '"audio" : [{' +
        '"url" : "' + this.episode.enclosureUrl + '",' +
      //        'size: 93260000,\n' +
      //        'title: \'Audio MP4\'\n' +
        '"mimeType" :"' + this.episode.enclosureType + '"' +
      '}],' +
      '"theme" : {' +
        '"main" : "#ffffff",' +
        '"highlight" : "#0785ff"' +
      '},' +
      '"visibleComponents": [' +
        '"tabChapters",' +
        '"tabAudio",' +
        '"progressbar",' +
        '"controlSteppers",' +
        '"controlChapters"' +
      ']' +
      '}';
    // console.log('configuration:');
    // console.log(configuration);

    const podloveJS = 'podlovePlayer(\'#podlove-player\',' + configuration + ');';
    /*
    let iframe_window = iframe.contentWindow;
    iframe_window.document.getElementById('iFrameResizer0').height = '0';
    */
    const el = document.createElement('script');
    el.appendChild(document.createTextNode(podloveJS));
    document.body.appendChild(el);
  }

  getEpisode(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.episodeService.get(id)
      .subscribe(episode => {
        this.episode = episode;
        this.initPodlove();
      });
  }

  goBack(): void {
    this.location.back();
  }

}
