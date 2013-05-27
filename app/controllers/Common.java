package controllers;

import models.Account;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class Common extends Action.Simple {
	
	@Override
    public Result call(Context ctx) throws Throwable {
		Logger.info("Account Action called");
		Account account = Account.findByEmail(ctx.session().get("email"));
        ctx.args.put("account", account);
        return delegate.call(ctx);
    }

    public static Account currentAccount() {
        return (Account)Context.current().args.get("account");
    }

}