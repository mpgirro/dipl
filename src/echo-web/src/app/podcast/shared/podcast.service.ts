import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Podcast } from './podcast.model';
import { Episode } from '../../episode/shared/episode.model';
import {catchError, tap} from 'rxjs/operators';
import {of} from 'rxjs/observable/of';
import {ArrayWrapper} from '../../arraywrapper.model';

@Injectable()
export class PodcastService {

  private baseUrl = '/api/podcast';  // URL to web API

  constructor(private http: HttpClient) { }

  get(echoId: string): Observable<Podcast> {
    const request = this.baseUrl + '/' + echoId;
    console.log('GET ' + request);
    return this.http.get<Podcast>(request).pipe(
      tap(_ => console.log(`found podcast matching "${echoId}"`)),
      catchError(this.handleError<Podcast>('getPodcast', new Podcast()))
    );
  }

  getEpisodes(echoId: string): Observable<Array<Episode>> {
    const request = this.baseUrl + '/' + echoId + '/episodes';
    console.log('GET ' + request);
    return this.http.get<Array<Episode>>(request).pipe(
      tap(_ => console.log(`found episodes for podcast matching "${echoId}"`)),
      catchError(this.handleError<Array<Episode>>('getPodcastEpisodes', new Array<Episode>()))
    );
  }

  getAll(page: number, size: number): Observable<ArrayWrapper<Podcast>> {
    const p = (page) ? `&p=${page}` : '';
    const s = (size) ? `&s=${size}` : '';
    const request = this.baseUrl + '?' + p + s;
    console.log('GET ' + request);
    return this.http.get<ArrayWrapper<Podcast>>(request).pipe(
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
