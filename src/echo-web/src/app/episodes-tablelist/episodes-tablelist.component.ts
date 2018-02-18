import {Component, Input, OnChanges} from '@angular/core';
import {Episode} from '../episode';

@Component({
  selector: 'app-episodes-tablelist',
  templateUrl: './episodes-tablelist.component.html',
  styleUrls: ['./episodes-tablelist.component.css']
})
export class EpisodesTablelistComponent implements OnChanges {

  @Input() episodes: Array<Episode>;
  isCollapsed: boolean[];

  constructor() { }

  ngOnChanges() {
    this.isCollapsed = new Array(this.episodes.length); // will be default init with false values
  }

}
