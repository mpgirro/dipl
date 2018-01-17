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

    this.currPage = (p) ? p : 1;
    this.currSize = (s) ? s : this.DEFAULT_SIZE;

    this.search(q);
  }

  onSelect(result: Result): void {
    this.selectedResult = result;
  }

  search(query: string): void {

    if (query) {
      const q = query;
      const p = this.currPage;
      const s = this.currSize;

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

  onEnter(query: string): void {

    // TODO set the currPage via the paging-navbar
    this.currPage = (this.currPage) ? this.currPage : 1;

    console.log('onEnter: query=' + query + ' & page=' + this.currPage + ' & and size=' + this.currSize);

    if (this.query !== query) {

      this.currPage = 1; // query has changed, so we need to reset the page counter!

      const navigationExtras = {
        queryParams: { 'query': query, 'page' : this.currPage, 'size': this.currSize }
      };

      // Navigate to the search page with extras
      this.router.navigate(['/search'], navigationExtras);

      this.search(query);
    } else {

      // TODO here we need to do stuff if the query has NOT changed, but the page-pointer was changed in the paging-navbar

    }
  }

}
