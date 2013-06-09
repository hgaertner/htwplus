package controllers;

import models.Account;
import models.Group;
import play.Logger;
import play.data.Form;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class Common extends Action.Simple {
	
	static Form<Login> loginForm = Form.form(Login.class);
	
	@Override
    public Result call(Context ctx) throws Throwable {
		Account account = Account.findByEmail(ctx.session().get("email"));
        ctx.args.put("account", account);
        return delegate.call(ctx);
    }

    public static Account currentAccount() {
        return (Account)Context.current().args.get("account");
    }
    
	public static void setLoginForm(Form<Login> loginForm) {
		Common.loginForm = loginForm;
	}
    
    public static Form<Login> getLoginForm() {
        return loginForm;
    }

}