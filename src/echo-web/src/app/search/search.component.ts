import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { Result } from '../core/services/search/result.model';
import { SearchService } from '../core/services/search/search.service';
import { DomainService } from '../core/services/domain.service';
import {of} from 'rxjs/observable/of';
import {ResultWrapper} from '../core/services/search/resultwrapper.model';

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
    const q = this.route.snapshot.queryParamMap.get('q');
    const p = this.route.snapshot.queryParamMap.get('p');
    const s = this.route.snapshot.queryParamMap.get('s');

    this.currPage = (p) ? Number(p) : 1;
    this.currSize = (s) ? Number(s) : this.DEFAULT_SIZE;

    this.search(q);
  }

  search(query: string): void {

    if (query) {
      const q = query;
      const p = this.currPage;
      const s = this.currSize;

      this.query = q; // TODO hier wird scheinbar das textfeld in der UI nicht richtig befüllt, wenn man die seite nur per URL param aufruft
      this.searchService.search(q, p, s)
        .subscribe(response => {

          this.currPage  = response.currPage;
          this.maxPage   = response.maxPage;
          this.totalHits = response.totalHits;
          this.results   = response.results;

          this.pages = new Array(this.maxPage).fill(0 ).map((x, i) => i + 1);

        });
    }
  }

  onEnter(query: string): void {

    // TODO set the currPage via the paging-navbar
    // this.currPage = (this.currPage) ? this.currPage : 1;

    this.currPage = 1; // query has changed, so we need to reset the page counter!

    const navigationExtras = {
      queryParams: { 'q': query, 'p' : this.currPage, 's': this.currSize }
    };

    // Navigate to the search page with extras
    this.router.navigate(['/search'], navigationExtras);

    this.search(query);

  }

  navigate(result: Result): void {
    let pre;
    if (result.docType === 'podcast') {
      pre = '/p/';
    } else if (result.docType === 'episode') {
      pre = '/e/';
    } else {
      console.log('Unknown docType : ' + result.docType);
    }
    this.router.navigate([pre + result.echoId]);
  }

}
