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

  DEFAULT_SIZE = 20;
  currSize: int;

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

    if (!p) {
      this.currPage = 1;
    } else {
      this.currPage = p;
    }

    if (!s) {
      this.currSize = this.DEFAULT_SIZE;
    } else {
      this.currSize = s;
    }

    // if (!q.trim()) {
    this.foo(q, this.currPage, this.currSize);
    // }
  }

  onSelect(result: Result): void {
    this.selectedResult = result;
  }

  foo(query: string, page: int, size: int): void {

    if (query) {
      const q = query;

      // use a juggling-check (==) to test for both null and undefined
      const p = (page == null) ? this.currPage : page;
      const s = (size == null) ? this.currSize : size;

      console.log('GET /search for: query=' + q + ', page=' + p + ', size=' + s)
      this.query = q; // TODO hier wird scheinbar das textfeld in der UI nicht richtig befüllt, wenn man die seite nur per URL param aufruft
      this.searchService.search(q, p, s)
        .subscribe(response => {

          this.currPage  = response.currPage;
          this.maxPage   = response.maxPage;
          this.totalHits = response.totalHits;
          this.results   = response.results;
          console.log('Received resultWrapper');
          console.log('currPage=' + this.currPage);
          console.log('maxPage=' + this.maxPage);
          console.log('totalHits=' + this.totalHits);
          console.log('results.length=' + this.results.length);
        });

    }

  }

  search(query: string): void {
    // this.router.navigate(['/search?query=']);
    /*
    this.route.navigate( [
      'SearchComponent', { query: query
      }]);
      */
    console.log('received search request: query=' + query + ' & page=' + this.currPage + ' & and size=' + this.currSize);

    if (this.query !== query) {

      const navigationExtras = {
        queryParams: { 'query': query, 'page' : this.currPage, 'size': this.currSize }
      };

      // Navigate to the search page with extras
      // TODO do this only if we are not already on this page
      this.router.navigate(['/search'], navigationExtras);

      this.foo(query, this.currPage, this.currSize);
    }



    // TODO delete code afterwars, and do it on load instead
    /*
    this.query = query;
    this.searchService.search(query)
      .subscribe(response => this.results = response.results);
      */
  }

}
