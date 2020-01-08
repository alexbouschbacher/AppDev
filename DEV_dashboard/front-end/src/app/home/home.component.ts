import { Component, OnInit, Inject } from '@angular/core';
import { Http, Headers, RequestOptions } from "@angular/http";
import { Router, ActivatedRoute } from "@angular/router";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { storageService } from '../localStorage.service';
import { FormGroup, FormControl, Validators, FormGroupDirective, NgForm } from '@angular/forms';
import "rxjs/Rx";

export interface DialogData {
    services: any;
    newWidget: any;
}

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})


export class HomeComponent implements OnInit {

    private profile: any
    private token: string
    public services : any
    public widgets: Array<any>;
    private url: string

    public constructor(private http: Http, private router: Router, private route: ActivatedRoute, public dialog: MatDialog, private store : storageService) {}
    public ngOnInit() {
        this.url = this.store.getUrl()
        this.profile = this.store.getData("user")
        this.token = this.store.getData("token")
        this.getProfile()

        this.widgets = this.profile.services
        this.setWidgets(this.widgets)

        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/about.json", options)
            .map(result => {
                var res = <any> result
                var body = JSON.parse(res._body)
                this.services = body.server.services
            })
            .subscribe(result => {});
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

    newWidget : any

    openDialog(): void {
        this.newWidget = {
            service : "",
            name : ""
        }
        const dialogRef = this.dialog.open(DialogWidget, {
            width: '250px',
            data: {services: this.services, newWidget: this.newWidget}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result != undefined) {
                if (result.params.limit)
                    result.params.limit = parseInt(result.params.limit, 10)
                if (result.params.max)
                    result.params.max = parseInt(result.params.max, 10)
                if (result.params.channel)
                    result.params.channel = "" + result.params.channel
                let body = {
                    name : result.service,
                    widget : {
                        name : result.name,
                        params : result.params
                    },
                    refresh : result.refresh
                }
                let headers = new Headers({
                    "content-type": "application/json",
                    "authorization": this.token
                });
                let options = new RequestOptions({ headers: headers });
                this.http.post(this.url + "/user/suscribe", body, options)
                .map(result => {
                    var res = <any> result
                    var body = JSON.parse(res._body)
                    this.profile = body.user
                    this.store.setData("user", this.profile)
                    this.widgets = this.profile.services
                    this.setWidgets(this.widgets)
                })
                .subscribe(result => {});
            }
        });
    }

    drop(event: CdkDragDrop<string[]>) {
        moveItemInArray(this.widgets, event.previousIndex, event.currentIndex);
    }


    setWidgets(widgetList) {
        widgetList.forEach((widget) => {
            console.log(widget)
            if (widget.name == 'calendar' && widget.service=='intra') {
                this.getCalendarIntra(widget)
                setInterval(()=> { this.getCalendarIntra(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'notes' && widget.service=='intra') {
                this.getNotes(widget)
                setInterval(()=> { this.getNotes(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'messages' && widget.service=='yammer') {
                this.getYammer(widget)
                setInterval(()=> { this.getYammer(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'mails' && widget.service=='outlook') {
                this.getMails(widget)
                setInterval(()=> { this.getMails(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'calendar' && widget.service=='calendar') {
                this.getGoogleCalendar(widget)
                setInterval(()=> { this.getGoogleCalendar(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'theme' && widget.service=='news') {
                this.getNewsQuery(widget)
                setInterval(()=> { this.getNewsQuery(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'author' && widget.service=='news') {
                this.getNewsAuthor(widget)
                setInterval(()=> { this.getNewsAuthor(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'board' && widget.service=='trello') {
                this.getTrelloBoard(widget)
                setInterval(()=> { this.getTrelloBoard(widget) }, widget.refresh * 1000);
            }
            if (widget.name == 'ending' && widget.service=='trello') {
                this.getTrelloTime(widget)
                setInterval(()=> { this.getTrelloTime(widget) }, widget.refresh * 1000);
            }
        })
    }

    getCalendarIntra(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });

        var now = new Date()
        let start = "" + now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate()

        if(widget.params[0].value == 'day')
            now.setDate(now.getDate() + 1)
        if(widget.params[0].value == 'week')
            now.setDate(now.getDate() + 7)
        if(widget.params[0].value == 'month')
            now.setDate(now.getDate() + 31)
        let end = "" + now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate()

        this.http.get(this.url + "/intra/calendar?start=" + start + "&end=" + end, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.calendar
        })
        .subscribe(result => {});
    }

    getNotes(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/intra/module?module=" + widget.params[0].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.notes
        })
        .subscribe(result => {});
    }

    getYammer(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/yammer/group?limit=" + widget.params[0].value + "&group=" + widget.params[1].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.response
        })
        .subscribe(result => {});
    }

    getMails(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/outlook/mails?max=" + widget.params[0].value + "&from=" + widget.params[1].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.response
        })
        .subscribe(result => {});
    }

    getGoogleCalendar(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/google/calendar?max=" + widget.params[0].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.calendar
        })
        .subscribe(result => {});
    }

    getNewsQuery(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/google/news?query=" + widget.params[0].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.articles
        })
        .subscribe(result => {});
    }

    getNewsAuthor(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/google/news?query=" + widget.params[0].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.articles
        })
        .subscribe(result => {});
    }

    getTrelloBoard(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/trello/cards?board=" + widget.params[0].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            widget.result = body.cards
        })
        .subscribe(result => {});
    }

    getTrelloTime(widget) {
        let headers = new Headers({
            "content-type": "application/json",
            "authorization": this.token
        });
        let options = new RequestOptions({ headers: headers });
        this.http.get(this.url + "/trello/cards?time=" + widget.params[0].value, options)
        .map(result => {
            var res = <any> result
            var body = JSON.parse(res._body)
            console.log(body)
            widget.result = body.cards
        })
        .subscribe(result => {});
    }
}

@Component({
	selector: 'dialog-overview-example-dialog',
	templateUrl: 'dialog-overview-example-dialog.html',
  })
  export class DialogWidget {

    widgets : Array<any>;
    widget : any
    newWidget : {
        service : string,
        name:string,
        params : any,
        refresh : number,
        model : any
    }
    token : string
    url : string

	constructor(
        private store: storageService,
        private http: Http,
	  public dialogRef: MatDialogRef<DialogWidget>,
	  @Inject(MAT_DIALOG_DATA) public data: DialogData) {
          this.widgets = null
          this.widget = null
          this.url = this.store.getUrl()
          this.newWidget = {
              service : "",
              name : "",
              params : null,
              refresh : 3600,
              model : null
          }
          this.token = this.store.getData("token")
      }

	onNoClick(): void {
	  this.dialogRef.close();
	}

    changeService(service) {
        this.widget = null
        this.data.services.forEach((value) => {
            if (value.name == service) {
                this.newWidget.service = service
                this.widgets = value.widgets
            }
        });
    }

    list_arg : Array<any>

    changeWidget(widget) {
        this.list_arg = null
        this.widgets.forEach((value) => {
            if (value.name == widget) {
                this.widget = value
                this.newWidget.model = value
                this.newWidget.name = widget
                let nw = this.newWidget
                if (nw.service == "intra" && nw.name == "calendar") {
                    this.newWidget.params = {
                        duration : ""
                    }
                }
                if (nw.service == "intra" && nw.name == "notes") {
                    this.newWidget.params = {
                        module : ""
                    }
                    let headers = new Headers({
                        "content-type": "application/json",
                        "authorization": this.token
                    });
                    let options = new RequestOptions({ headers: headers });
                    this.http.get(this.url + "/intra/module", options)
                    .map(result => {
                        var res = <any> result
                        var body = JSON.parse(res._body)
                        this.list_arg = body.profil
                    })
                    .subscribe(result => {});
                }

                if (nw.service == "yammer" && nw.name == "messages") {
                    this.newWidget.params = {
                        limit : "0",
                        channel : ""
                    }
                    let headers = new Headers({
                        "content-type": "application/json",
                        "authorization": this.token
                    });
                    let options = new RequestOptions({ headers: headers });
                    this.http.get(this.url + "/yammer/groups", options)
                    .map(result => {
                        var res = <any> result
                        var body = JSON.parse(res._body)
                        this.list_arg = body.groups
                    })
                    .subscribe(result => {});
                }

                if (nw.service == "outlook" && nw.name == "mails") {
                    this.newWidget.params = {
                        max : "0",
                        from: ""
                    }
                }

                if (nw.service == "calendar" && nw.name == "calendar") {
                    this.newWidget.params = {
                        max : "0"
                    }
                }

                if (nw.service == "news" && nw.name == "theme") {
                    this.newWidget.params = {
                        query : ""
                    }
                }

                if (nw.service == "news" && nw.name == "author") {
                    this.newWidget.params = {
                        authors : ""
                    }
                    let headers = new Headers({
                        "content-type": "application/json",
                        "authorization": this.token
                    });
                    let options = new RequestOptions({ headers: headers });
                    this.http.get(this.url + "/google/authors", options)
                    .map(result => {
                        var res = <any> result
                        var body = JSON.parse(res._body)
                        this.list_arg = body.authors.sources
                    })
                    .subscribe(result => {});
                }

                if (nw.service == "trello" && nw.name == "board") {
                    this.newWidget.params = {
                        board : ""
                    }
                    let headers = new Headers({
                        "content-type": "application/json",
                        "authorization": this.token
                    });
                    let options = new RequestOptions({ headers: headers });
                    this.http.get(this.url + "/trello/boards", options)
                    .map(result => {
                        var res = <any> result
                        var body = JSON.parse(res._body)
                        this.list_arg = body.boards
                    })
                    .subscribe(result => {});
                }

                if (nw.service == "trello" && nw.name == "ending") {
                    this.newWidget.params = {
                        time : "0"
                    }
                }
            }
        });
    }

    group() {
        console.log(this.newWidget, this.newWidget.params)
    }

  }
