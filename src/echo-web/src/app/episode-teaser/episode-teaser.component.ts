import {Component, Input, OnInit} from '@angular/core';

import { Episode } from '../episode';

@Component({
  selector: 'app-episode-teaser',
  templateUrl: './episode-teaser.component.html',
  styleUrls: ['./episode-teaser.component.css']
})
export class EpisodeTeaserComponent implements OnInit {

  @Input() episode: Episode;

  constructor() { }

  ngOnInit() {
  }

}