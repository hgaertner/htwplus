package models;

import java.util.HashMap;
import java.util.Map;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;


@IndexType(name = "indexTest")
public class IndexTest extends Index {

	
	public String name;
	
	
	@Override
	public Indexable fromIndex(Map map) {
		this.name = (String) map.get("name");
        return this;
	}

	@Override
	public Map toIndex() {
		Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        return map;
	}

}
