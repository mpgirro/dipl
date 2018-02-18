import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Podcast } from './podcast';
import { Episode } from './episode';
import { ArrayWrapper } from './arraywrapper';
import {catchError, tap} from 'rxjs/operators';
import {of} from 'rxjs/observable/of';

@Injectable()
export class PodcastService {

  constructor(private http: HttpClient) { }

  get(echoId: string): Observable<Podcast> {

    console.log('requesting get podcast from backend with echoId: ' + echoId);
    return this.http.get<Podcast>(`/api/podcast/${echoId}`).pipe(
      tap(_ => console.log(`found podcast matching "${echoId}"`)),
      catchError(this.handleError<Podcast>('getPodcast', new Podcast()))
    );
  }

  getEpisodes(echoId: string): Observable<Array<Episode>> {
    console.log(`GET /api/podcast/${echoId}/episodes`);
    return this.http.get<Array<Episode>>(`/api/podcast/${echoId}/episodes`).pipe(
      tap(_ => console.log(`found episodes for podcast matching "${echoId}"`)),
      catchError(this.handleError<Array<Episode>>('getPodcastEpisodes', new Array<Episode>()))
    );
  }

  getAll(): Observable<ArrayWrapper<Podcast>> {
    console.log(`GET /api/podcast`);
    return this.http.get<ArrayWrapper<Podcast>>(`/api/podcast`).pipe(
      tap(_ => console.log(`found all podcasts`)),
      catchError(this.handleError<ArrayWrapper<Podcast>>('allPodcasts', new ArrayWrapper<Podcast>()))
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
