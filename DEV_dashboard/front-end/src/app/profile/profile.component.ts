import { Component, OnInit, Inject} from '@angular/core';
import { Http, Headers, RequestOptions } from "@angular/http";
import { ActivatedRoute } from "@angular/router";
import { Location } from "@angular/common";
import { storageService } from '../localStorage.service';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import "rxjs/Rx";


export interface DialogDataEpi {
  autologin: string;
  mail: string;
}

export interface DialogDataOut {
  url: string;
}

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent {
    public profile: any;
    private token: string;
    private url: string;
    public constructor(private http: Http, private route: ActivatedRoute, private location: Location, private store: storageService, public dialog: MatDialog) {}

    public ngOnInit() {
        this.url = this.store.getUrl()
        this.profile = this.store.getData("user")
        this.token = this.store.getData("token")
        this.getProfile()

        let service = this.route.snapshot.queryParamMap.get("service")
        let code = this.route.snapshot.queryParamMap.get("code")
        let hash = window.location.hash
        let token = null
        if (hash.includes("#token="))
            token = hash.split("=")[1]
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });

        if (service != null) {
            if (service == "outlook") {
                this.http.get(this.url + "/outlook/token?code=" + code, options)
                .map(res => {
                    var rep = <any>res
                    var bo = JSON.parse(rep._body)
                    var tbody  = {
                        outlook : {
                            code : code,
                            token : bo.token,
                            log : true
                        }
                    }
                    this.http.post(this.url + "/user/modify", tbody, options)
                        .map(res2 => {
                            var rep2 = <any>res2
                            var body = JSON.parse(rep2._body)
                            this.getProfile()
                        })
                        .subscribe(result => {});
                })
                .subscribe(result => {});
            } else if (service == "yammer") {
                this.http.get(this.url + "/yammer/token?code=" + code, options)
                .map(res => {
                    var rep = <any>res
                    var bo = JSON.parse(rep._body)
                    var tbody  = {
                        yammer : {
                            code : code,
                            token : bo.token,
                            log : true
                        }
                    }
                    this.http.post(this.url + "/user/modify", tbody, options)
                        .map(res2 => {
                            var rep2 = <any>res2
                            var body = JSON.parse(rep2._body)
                            this.getProfile()
                        })
                        .subscribe(result => {});
                })
                .subscribe(result => {});
            }
        } else if (token != null) {
            var body = {
                trello : {
                    token : token,
                    log : true
                }
            }
            this.http.post(this.url + "/user/modify", body, options)
            .map(res => {
                var rep = <any>res
                var body = JSON.parse(rep._body)
                this.getProfile()
            })
            .subscribe(result => {});
        }
    }

    private getProfile() {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/user/me", options)
            .map(result => {
                var res = <any> result
                var body = JSON.parse(res._body)
                this.profile = body.user
                this.store.setData("user", this.profile)
            })
            .subscribe(result => {});
    }

    public epitech(event) {
        if (event == true) {
            this.openEpitech()
        } else {
            let headers = new Headers({
                "content-type": "application/json",
                "authorization": this.token
            });
            let options = new RequestOptions({ headers: headers });
            var body  = {
                intra : {
                    autologin : this.profile.intra.autologin,
                    mail : this.profile.intra.mail,
                    log : false,
                    token : ""
                }
            }
            this.http.post(this.url + "/user/modify", body, options)
                .map(res => {
                    var rep = <any>res
                    var body = JSON.parse(rep._body)
                    this.profile = body.user
                })
                .subscribe(result => {});
        }
    }

    public google(event) {
        if (event == true) {
            let headers = new Headers({
                "content-type": "application/json",
                "authorization": this.token
            });
            console.log(headers)
            let options = new RequestOptions({ headers: headers });
            this.http.get(this.url + "/google/login", options)
            .map(res => {
                var rep = <any> res
                var bo = JSON.parse(rep._body)
                var body  = {
                    calendar : {
                        file : bo.credentials,
                        log : true
                    }
                }
                this.http.post(this.url + "/user/modify", body, options)
                    .map(res2 => {
                        var rep2 = <any> res2
                        var body = JSON.parse(rep2._body)
                        this.profile = body.user
                    })
                    .subscribe(result => {});
            })
            .subscribe(result => {});
        } else {
            let headers = new Headers({
                "content-type": "application/json",
                "authorization": this.token
            });
            let options = new RequestOptions({ headers: headers });
            var body  = {
                calendar : {
                    file : "",
                    log : false
                }
            }
            this.http.post(this.url + "/user/modify", body, options)
                .map(res => {
                    var rep = <any>res
                    var body = JSON.parse(rep._body)
                    this.profile = body.user
                })
                .subscribe(result => {});
        }
    }


    public outlook(event) {
        if (event == true) {
            window.location.replace(this.url + "/outlook/login")
        } else {
            let headers = new Headers({
                "content-type": "application/json",
                "authorization": this.token
            });
            let options = new RequestOptions({ headers: headers });
            var body  = {
                outlook : {
                    code : "",
                    token : "",
                    log : false
                }
            }
            this.http.post(this.url + "/user/modify", body, options)
                .map(res => {
                    var rep = <any>res
                    var body = JSON.parse(rep._body)
                    this.profile = body.user
                })
                .subscribe(result => {});
        }
    }

    public trello(event) {
        if (event == true) {
            window.location.replace(this.url + "/trello/login")
        } else {
            let headers = new Headers({
                "content-type": "application/json",
                "authorization": this.token
            });
            let options = new RequestOptions({ headers: headers });
            var body  = {
                trello : {
                    token : "",
                    log : false
                }
            }
            this.http.post(this.url + "/user/modify", body, options)
                .map(res => {
                    var rep = <any>res
                    var body = JSON.parse(rep._body)
                    this.profile = body.user
                })
                .subscribe(result => {});
        }
    }

    public yammer(event) {
        if (event == true) {
            window.location.replace(this.url + "/yammer/url")
        } else {
            let headers = new Headers({
                "content-type": "application/json",
                "authorization": this.token
            });
            let options = new RequestOptions({ headers: headers });
            var body  = {
                yammer : {
                    code : "",
                    token : "",
                    id : "",
                    log : false
                }
            }
            this.http.post(this.url + "/user/modify", body, options)
                .map(res => {
                    var rep = <any>res
                    var body = JSON.parse(rep._body)
                    this.profile = body.user
                })
                .subscribe(result => {});
        }
    }


    public save() {
    }

    private openEpitech(): void {
        const dialogRef = this.dialog.open(DialogEpitech, {
            width: '350px',
            data: {autologin: this.profile.intra.autologin, mail: this.profile.intra.mail}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result != undefined) {
                let headers = new Headers({
                    "content-type": "application/json",
                    "authorization": this.token
                });
                let options = new RequestOptions({ headers: headers });
                this.http.post(this.url + "/intra/login", {url : result.autologin}, options)
                .map(res => {
                    var rep = <any> res
                    var tok = JSON.parse(rep._body)
                    var body  = {
                        intra : {
                            autologin : result.autologin,
                            mail : result.mail,
                            log : true,
                            token : tok.token
                        }
                    }
                    this.http.post(this.url + "/user/modify", body, options)
                        .map(res2 => {
                            var rep2 = <any> res2
                            var body = JSON.parse(rep2._body)
                            this.getProfile()
                        })
                        .subscribe(result => {});
                })
                .subscribe(result => {});
            }
        });
    }

    private openOutlook(url): void {
        const dialogRef = this.dialog.open(DialogOutlook, {
            width: '450px',
            height: '400px',
            data: {url: url}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result != undefined) {
                console.log(result)
            }
        });
    }
}

@Component({
  selector: 'dialog-epitech',
  templateUrl: 'dialog-epitech.html',
})
export class DialogEpitech {

  constructor(
    public dialogRef: MatDialogRef<DialogEpitech>,
    @Inject(MAT_DIALOG_DATA) public data: DialogDataEpi) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

}

@Component({
  selector: 'dialog-outlook',
  templateUrl: 'dialog-outlook.html',
})
export class DialogOutlook {

  constructor(
    public dialogRef: MatDialogRef<DialogOutlook>,
    @Inject(MAT_DIALOG_DATA) public data: DialogDataOut) {
        var test = window.open(data.url)
    }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
