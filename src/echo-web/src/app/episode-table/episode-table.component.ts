import {Component, Input, OnInit} from '@angular/core';
import {Episode} from '../episode';

@Component({
  selector: 'app-episode-table',
  templateUrl: './episode-table.component.html',
  styleUrls: ['./episode-table.component.css']
})
export class EpisodeTableComponent implements OnInit {

  @Input() episodes: Array<Episode>;

  constructor() { }

  ngOnInit() {
  }

}
