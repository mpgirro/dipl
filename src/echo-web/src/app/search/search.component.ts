import { Component, OnInit } from '@angular/core';
import { Result } from '../result';
import { SearchService } from '../search.service';

import { IconsModule } from '../icons/icons.module';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  results: Result[];
  selectedResult: Result;

  constructor(private searchService: SearchService) { }

  ngOnInit() {
  }

  onSelect(result: Result): void {
    this.selectedResult = result;
  } 

  search(query: string): void {
    this.searchService.search(query)
      .subscribe(response => this.results = response.results);
  }

}
