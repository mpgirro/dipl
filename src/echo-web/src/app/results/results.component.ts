import { Component, OnInit } from '@angular/core';
import { Result } from '../result';
import { RESULTS } from '../mock-results';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit {

  query: string;
//  results: Result[];
  results: Result[] = RESULTS;
  selectedResult: Result;

  constructor() { }

  ngOnInit() {
  }

  onSelect(result: Result): void {
    this.selectedResult = result;
  } 

}
