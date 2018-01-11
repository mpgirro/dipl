import { Injectable } from '@angular/core';

import { Result } from './result';
import { ResultWrapper } from './resultwrapper';
import { RESULTS } from './mock-results'; // TODO delete

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of'; // TODO brauch ich eh nimma, oder?
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';

@Injectable()
export class SearchService {

  private searchUrl = '/api/search';  // URL to web api

  constructor(private http: HttpClient) { }

  search(query: string): Observable<ResultWrapper> {
    if (!query.trim()) {
      // if not search term, return empty result array.
      return of(new ResultWrapper());
    }
    console.log("requesting search result from backend for query: " + query)
    return this.http.get<ResultWrapper>(`/api/search?query=${query}`).pipe(
      tap(_ => console.log(`found results matching "${query}"`)),
      catchError(this.handleError<ResultWrapper>('search', new ResultWrapper()))
    );
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      //this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}
