import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, FormGroupDirective, NgForm } from '@angular/forms'
import { storageService } from '../localStorage.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  form: FormGroup = new FormGroup({
    mail: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  constructor(private http: Http, private router: Router, public store: storageService) { }

  ngOnInit() {
      this.store.clear()
  }

  public login() {
    if(this.form.status == "VALID") {
      let headers = new Headers({ "content-type": "application/json"});
      let options = new RequestOptions({ headers: headers });
      this.http.post("http://0.0.0.0:5000/user/signin", this.form.value, options)
      .map(result => {
        var res = <any> result
        var body = JSON.parse(res._body)
        this.store.setData("token", body.token);
        this.store.setData("user", body.user);
      })
      .subscribe(result => {
        this.router.navigate(["/home"]);
      });
    }
  }

}
