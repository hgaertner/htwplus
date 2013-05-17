package controllers;

import models.Account;
import play.db.jpa.Transactional;

public class Login {

		public String email;
		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String password;

		@Transactional
		public String validate() {
			if (Account.authenticate(email, password) == null) {
				return "Invalid user or password";
			}
			return null;
		}
	}