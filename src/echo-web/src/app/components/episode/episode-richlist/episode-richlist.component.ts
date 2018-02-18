import {Component, Input, OnInit} from '@angular/core';
import {Episode} from '../../../episode';

@Component({
  selector: 'app-episode-richlist',
  templateUrl: './episode-richlist.component.html',
  styleUrls: ['./episode-richlist.component.css']
})
export class EpisodeRichlistComponent implements OnInit {

  @Input() episodes: Array<Episode>;

  constructor() { }

  ngOnInit() {
  }

}
