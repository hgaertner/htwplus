package controllers;

import static play.data.Form.form;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
	
    /**
     * Generates an md5 hash of a String.
     * @param input String value
     * @return Hashvalue of the String.
     */
    public static String md5(String input) {
        
        String md5 = null;
         
        if(null == input) return null;
         
        try {
             
        //Create MessageDigest object for MD5
        MessageDigest digest = MessageDigest.getInstance("MD5");
         
        //Update input string in message digest
        digest.update(input.getBytes(), 0, input.length());
 
        //Converts message digest value in base 16 (hex) 
        md5 = new BigInteger(1, digest.digest()).toString(16);
 
        } catch (NoSuchAlgorithmException e) {
 
            e.printStackTrace();
        }
        return md5;
    }
    
}