import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Result } from '../result';
import { SearchService } from '../search.service';
import {of} from 'rxjs/observable/of';
import {ResultWrapper} from '../resultwrapper';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  results: Result[];
  selectedResult: Result;
  query: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private searchService: SearchService) { }

  ngOnInit() {
    const q = this.route.snapshot.queryParamMap.get('query');
    // if (!q.trim()) {
    this.foo(q);
    // }
  }

  onSelect(result: Result): void {
    this.selectedResult = result;
  }

  foo(q: string): void {
    console.log('query=' + q)
    this.query = q;
    this.searchService.search(q)
      .subscribe(response => this.results = response.results);
  }

  search(query: string): void {
    // this.router.navigate(['/search?query=']);
    /*
    this.route.navigate( [
      'SearchComponent', { query: query
      }]);
      */
    if (this.query !== query) {
      const navigationExtras = {
        queryParams: { 'query': query }
      };

      // Navigate to the login page with extras
      this.router.navigate(['/search'], navigationExtras);

      this.foo(query);
    }



    // TODO delete code afterwars, and do it on load instead
    /*
    this.query = query;
    this.searchService.search(query)
      .subscribe(response => this.results = response.results);
      */
  }

}
