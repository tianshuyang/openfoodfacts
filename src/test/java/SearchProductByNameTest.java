import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class SearchProductByNameTest {

	@Test
	public void testSearch() throws MalformedURLException, JSONException {
		// search url of open food fact database
	    URL searchURL = new URL("https://world.openfoodfacts.org/cgi/search.pl");
	    // output format is json, take the first page, each page contains 20 products, order by popularity
	    String searchQuery = "&search_simple=1&action=process&json=1&page=1&page_size=20";
	    SearchProductByName searchTest = new SearchProductByName(searchURL, "UTF-8", searchQuery, "search_terms");
	    JSONObject correct1 = new JSONObject("{\"description\":\"compote de pomme bio\",\"EAN code\":\"3021760403167\",\"product_name\":\"compote bio pomme\"}");
	    assertEquals(correct1.toString(), searchTest.search("compote bio pomme/vanille jardin bio 680g").toString());
	    JSONObject correct2 = new JSONObject("{\"description\":\"hereisthedescription\",\"product_name\":\"writewhateveryouwant\"}");
	    assertEquals(correct2.toString(), searchTest.search("writewhateveryouwant/hereisthedescription").toString());
	}

}
