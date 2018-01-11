import { Component, OnInit } from '@angular/core';
import { Result } from '../result';
import { SearchService } from '../search.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

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
      .subscribe(response => this.results = response.results);
  }

}
