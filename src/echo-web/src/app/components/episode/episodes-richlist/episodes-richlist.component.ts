import {Component, Input, OnInit} from '@angular/core';
import {Episode} from '../../../episode';

@Component({
  selector: 'app-episodes-richlist',
  templateUrl: './episodes-richlist.component.html',
  styleUrls: ['./episodes-richlist.component.css']
})
export class EpisodesRichlistComponent implements OnInit {

  @Input() episodes: Array<Episode>;

  constructor() { }

  ngOnInit() {
  }

}
