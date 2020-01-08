import { Component } from '@angular/core';
import { Http, Headers, RequestOptions } from "@angular/http";
import { Router } from "@angular/router";
import { FormGroup, FormControl, Validators, FormGroupDirective, NgForm } from '@angular/forms';
import "rxjs/Rx";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {
    form: FormGroup = new FormGroup({
        firstName: new FormControl('', Validators.required),
        lastName: new FormControl('', Validators.required),
        mail: new FormControl('', [Validators.required, Validators.email]),
        password: new FormControl('', [Validators.required, Validators.minLength(8)])
    });
    public constructor(private http: Http, private router: Router) {}
    public register() {
        if(this.form.status == "VALID") {
            let headers = new Headers({ "content-type": "application/json"});
            let options = new RequestOptions({ headers: headers });
            this.http.post("http://0.0.0.0:5000/user/signup", this.form.value, options)
                .map(result => {})
                .subscribe(result => {
                    this.router.navigate(["/login"]);
                });
        }
    }
}
