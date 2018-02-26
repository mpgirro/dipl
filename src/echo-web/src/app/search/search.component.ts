import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, ParamMap} from '@angular/router';

import { Result } from '../result.model';
import { SearchService } from '../search.service';
import { DomainService } from '../domain.service';
import {of} from 'rxjs/observable/of';
import {ResultWrapper} from '../resultwrapper.model';
import {Observable} from 'rxjs/Observable';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  DEFAULT_SIZE = 20;
  currSize: number;

  currPage: number;
  maxPage: number;
  pages: number[];

  totalHits: number;

  results: Result[];
  query: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private searchService: SearchService,
              private domainService: DomainService) { }

  ngOnInit() {
    this.route.paramMap
      .switchMap((params: ParamMap) => {
        const q = params.get('q');
        const p = params.get('p');
        const s = params.get('s');
        this.query = q;
        this.currPage = (p) ? Number(p) : 1;
        this.currSize = (s) ? Number(s) : this.DEFAULT_SIZE;
        return this.searchService.search(this.query, this.currPage, this.currSize);
      }).subscribe(response => this.onSearchResponse(response));
  }

  search(query: string): void {

    if (query) {
      const q = query;
      const p = this.currPage;
      const s = this.currSize;

      this.query = q;
      this.searchService.search(q, p, s)
        .subscribe(response => this.onSearchResponse(response));
    }
  }

  onSearchResponse(response: ResultWrapper) {
    this.currPage  = response.currPage;
    this.maxPage   = response.maxPage;
    this.totalHits = response.totalHits;
    this.results   = response.results;

    this.pages = new Array(this.maxPage).fill(0 ).map((x, i) => i + 1);
  }

  onEnter(query: string): void {

    // TODO set the currPage via the paging-navbar
    // this.currPage = (this.currPage) ? this.currPage : 1;

    this.query = query;
    this.currPage = 1; // query has changed, so we need to reset the page counter!

    /* TODO delete
    const navigationExtras = {
      queryParams: { 'q': query, 'p' : this.currPage, 's': this.currSize }
    };
    */

    // Navigate to the search page with extras
    this.router.navigate(['/search', { 'q': query, 'p' : this.currPage, 's': this.currSize }]);

    this.search(query);

  }

  navigate(result: Result): void {
    let pre;
    if (result.docType === 'podcast') {
      pre = '/p';
    } else if (result.docType === 'episode') {
      pre = '/e';
    } else {
      console.log('Unknown docType : ' + result.docType);
    }
    this.router.navigate([pre,  result.echoId]);
  }

}
