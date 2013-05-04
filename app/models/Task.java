package models;

import java.util.*;
import play.data.validation.Constraints.*;

import javax.annotation.Generated;
import javax.persistence.*;
import play.db.jpa.*;

@Entity
@SequenceGenerator(name = "task_seq", sequenceName = "task_seq")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
	public Long id;
	
	@Required
	public String label;
	
	//public static Finder<Long, Task> find = new Finder(Long.class, Task.class);
	
	public static List<Task> all() {
		List<Task> tasks = JPA.em().createQuery("SELECT t FROM Task t").getResultList();
		return tasks;
	}
	
	public static void create(Task task) {
		JPA.em().persist(task);

	}

	public static void delete(Long id) {
		Task t = JPA.em().find(Task.class, id);
		JPA.em().remove(t);
	}

}