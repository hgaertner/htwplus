package controllers;

import static play.data.Form.form;
import models.Account;
import models.Group;
import models.Login;
import play.Logger;
import play.api.templates.Html;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class Component extends Action.Simple {
	
	@Override
	@Transactional
    public Result call(Context ctx) throws Throwable {
		Account account = Account.findByEmail(ctx.session().get("email"));
		ctx.args.put("account", account);
        return delegate.call(ctx);
    }

    public static Account currentAccount() {
        return (Account)Context.current().args.get("account");
    }
    
	public static Html loginForm() {
		Form<Login> loginForm = form(Login.class);
		Form<Account> signupForm = form(Account.class);
		return views.html.snippets.loginForm.render(loginForm, signupForm);
	}
    
}