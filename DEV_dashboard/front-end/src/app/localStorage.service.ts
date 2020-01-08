import { Injectable } from '@angular/core';
import { CookiesStorageService, LocalStorageService, SessionStorageService, SharedStorageService } from 'ngx-store';

@Injectable(
    {providedIn: 'root'}
)

export class storageService  {

  constructor(public localStorage: LocalStorageService,
      public CookiesStorage: CookiesStorageService,
      public SessionStorage: SessionStorageService,
      public SharedStorage: SharedStorageService){
  }

  public getUrl () {
    return ("http://0.0.0.0:5000")
  }

  public setData (string, value: any) {
    this.localStorage.set(string, value);
  }

  public getData (string) {
    return (this.localStorage.get(string));
  }

  public clear () {
      this.localStorage.clear();
  }
}
