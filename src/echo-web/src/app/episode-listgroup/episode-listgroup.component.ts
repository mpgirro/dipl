import {Component, Input, OnInit} from '@angular/core';
import {Episode} from '../episode';

@Component({
  selector: 'app-episode-listgroup',
  templateUrl: './episode-listgroup.component.html',
  styleUrls: ['./episode-listgroup.component.css']
})
export class EpisodeListgroupComponent implements OnInit {

  @Input() episodes: Array<Episode>;

  constructor() { }

  ngOnInit() {
  }

}
