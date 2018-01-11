import { Component, OnInit } from '@angular/core';
import { Result } from '../result';
import { SearchService } from '../search.service';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit {

  query: string;
  results: Result[];
  selectedResult: Result;

  constructor(private searchService: SearchService) { }

  ngOnInit() {
  }

  onSelect(result: Result): void {
    this.selectedResult = result;
  } 

  search(): void {
    this.searchService.search(this.query)
      //.subscribe(response => this.results = response.results); // TODO so schaut das dann im HTTP wrapper aus
      .subscribe(response => this.results = response.results);
  }

}
