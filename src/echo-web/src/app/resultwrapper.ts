import { Result } from './result';

export class ResultWrapper {
    currPage: int;
    maxPage: int;
    totalHits: int;
    results: Result[];

    constructor() {
        this.results = [];
    }

}
