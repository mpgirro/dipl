import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Episode } from './episode';
import {catchError, tap} from 'rxjs/operators';
import {of} from 'rxjs/observable/of';

@Injectable()
export class EpisodeService {

  constructor(private http: HttpClient) { }

  get(echoId: string): Observable<Episode> {

    console.log('requesting get episode from backend with echoId: ' + echoId);
    return this.http.get<Episode>(`/api/episode/${echoId}`).pipe(
      tap(_ => console.log(`found episode matching "${echoId}"`)),
      catchError(this.handleError<Episode>('search', new Episode()))
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
      // this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}