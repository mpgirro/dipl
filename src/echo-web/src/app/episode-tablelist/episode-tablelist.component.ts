import {Component, Input, OnInit} from '@angular/core';
import {Episode} from '../episode';

@Component({
  selector: 'app-episode-tablelist',
  templateUrl: './episode-tablelist.component.html',
  styleUrls: ['./episode-tablelist.component.css']
})
export class EpisodeTablelistComponent implements OnInit {

  @Input() episodes: Array<Episode>;
  isCollapsed: boolean = false;

  constructor() { }

  ngOnInit() {
  }

}
