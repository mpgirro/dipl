import { Result } from './result';

export class ResultWrapper {
    currPage: number;
    maxPage: number;
    totalHits: number;
    results: Result[];

    constructor() {
        this.results = [];
    }

}
