import { Injectable } from '@angular/core';

@Injectable()
export class DomainService {

   extractHostname(url: string): string {
     let hostname;
     // find & remove protocol (http, ftp, etc.) and get hostname

     if (url.indexOf('://') > -1) {
       hostname = url.split('/')[2];
     } else {
       hostname = url.split('/')[0];
     }

     // find & remove port number
     hostname = hostname.split(':')[0];
     // find & remove "?"
     hostname = hostname.split('?')[0];

     return hostname;
  }

  removeProtocol(url: string): string {
    let hostname;
    if (url.indexOf('://') > -1) {
      hostname = url.split('://')[1];
    } else {
      hostname = url.split('/')[0];
    }
    // console.log(url + ' --> ' + hostname)
    return hostname;
  }

}


