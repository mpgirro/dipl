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

  currPage: int;
  maxPage: int;
  totalHits: int;

  results: Result[];
  selectedResult: Result;
  query: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private searchService: SearchService) { }

  ngOnInit() {
    const q = this.route.snapshot.queryParamMap.get('query');
    const p = this.route.snapshot.queryParamMap.get('page');
    const s = this.route.snapshot.queryParamMap.get('size');
    // if (!q.trim()) {
    this.foo(q, p, s);
    // }
  }

  onSelect(result: Result): void {
    this.selectedResult = result;
  }

  foo(query: string, page: int, size: int): void {

    if (query) {
      const q = query;

      // use a juggling-check (==) to test for both null and undefined
      const p = (page == null) ? 1 : page;
      const s = (size == null) ? 20 : size;

      console.log('GET /search for: query=' + q + ', page=' + p + ', size=' + s)
      this.query = q; // TODO hier wird scheinbar das textfeld in der UI nicht richtig befüllt, wenn man die seite nur per URL param aufruft
      this.searchService.search(q, p, s)
        .subscribe(response => {

          this.currPage = response.currPage;
          this.maxPage = response.maxPage;
          this.totalHits = response.totalHits;
          this.results = response.results;
          console.log('Received resultWrapper');
          console.log('currPage=' + this.currPage);
          console.log('maxPage=' + this.maxPage);
          console.log('totalHits=' + this.totalHits);
          console.log('results.length=' + this.results.length);
        });

    }

  }

  search(query: string, page: int, size: int): void {
    // this.router.navigate(['/search?query=']);
    /*
    this.route.navigate( [
      'SearchComponent', { query: query
      }]);
      */
    console.log('received search request: query=' + query + ' & page=' + page + ' & and size=' + size);

    if (this.query !== query) {

      const p = (page == null) ? 1 : page;
      const s = (size == null) ? 20 : size;

      const navigationExtras = {
        queryParams: { 'query': query, 'page' : p, 'size': s }
      };

      // Navigate to the search page with extras
      // TODO do this only if we are not already on this page
      this.router.navigate(['/search'], navigationExtras);

      this.foo(query, p, s);
    }



    // TODO delete code afterwars, and do it on load instead
    /*
    this.query = query;
    this.searchService.search(query)
      .subscribe(response => this.results = response.results);
      */
  }

}
