package controllers;

import play.Logger;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class AdminAction extends Action.Simple {

	@Override
	public Result call(Context ctx) throws Throwable {
		if(!Secured.isAdmin()){
			return redirect(routes.Application.index());
		}
		Navigation.set(Navigation.Level.ADMIN);
		return delegate.call(ctx);
	}


}