import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;



//Search a product in a database by its product name
public class SearchProductByName {
	
	private URL searchURL; //search url of database
	private String searchParam, searchKey; //user defined search parameters and key represents product name (in our case "search_terms")
	private String charset; //ofen UTF-8

	//construct
	public SearchProductByName(URL searchURL, String charset, String searchParam, String searchKey) {
		this.searchURL = searchURL;
		this.charset = charset;
		this.searchParam = searchParam;
		this.searchKey = searchKey;
	}
	
	//search a product described by a line in input file
	public JSONObject search(String l) {
		
		String productName;
		String description;
		
		//get product name and description from input line
		if (l.contains("/")) {
			productName = l.split("/",2)[0];
			description = l.split("/",2)[1];
		}
		else {
			productName = l;
			description = "";
		}
		
		//return value
		JSONObject result = new JSONObject();
		
		try {
			
			//construct request url
			String searchProduct = String.format("?" + searchKey + "=%s"
					, URLEncoder.encode(productName, charset));
			URL request = new URL(searchURL + searchProduct + searchParam);

			StringBuilder response = new StringBuilder();
			//open http url connection
			HttpURLConnection conn = (HttpURLConnection) request.openConnection();
		    //request is a GET
		    conn.setRequestMethod("GET");
		    	
		    //get response stream
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    //add lines to response
		    rd.lines().forEach(line->response.append(line));
		    //reader close
		    rd.close();
		    
		    //disconnect http url connection
		    conn.disconnect();
		    
		    //create a json from response
		    JSONObject jsonRes = new JSONObject(response.toString());
		    
		    //exact number of products
		    int count = jsonRes.getInt("count");
		    
		    //if such product does not exist
		    if ( count == 0 ) {
		    	System.out.println("No product named" + productName + "found!");
				result.put("product_name", productName);
		    	result.put("description", description);
		    }
		    else {
		    	//exact EAN code and description
		    	JSONArray productArray = jsonRes.getJSONArray("products");
		    	
		    	//if more than one product are found, choose the closest one
		    	if (productArray.length() > 1) {
		    		System.out.println("More than one product found, choose the closest one as result!");
		    	}
		    	JSONObject product = productArray.getJSONObject(0);
				result.put("product_name", productName);
		    	result.put("EAN code", product.getString("code"));
		    	if (product.has("generic_name"))
		    		result.put("description", product.getString("generic_name"));
		    }
		    
		//exception hanlders
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException io) {
	    	io.printStackTrace();
	    } catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;	
		
	}
	
	//search for all products in a input file
	public void searchFile(String inpath, String outpath) throws IOException, InterruptedException, ExecutionException {
		
		ExecutorService executor = Executors.newFixedThreadPool(4);
		
		//get input stream, create output stream
	    Stream<String> stream = Files.lines(Paths.get(inpath));
	    PrintStream out = new PrintStream(outpath);
	    		
	    stream.forEach(line -> {
	    	//create a new count down latch
	    	CountDownLatch latch = new CountDownLatch(1);
	    	//async http connection to search for a product
	    	CompletableFuture future = CompletableFuture.supplyAsync(() -> search(line))
	    								.thenAccept(content -> {
	    									//write result to output
	    									out.println(content);
	    									//count down latch
	    									latch.countDown();
	    								});
	    	try {
	    		latch.await();
	    	} catch (InterruptedException e) {
	    		System.out.println("Interrupted");
	    	}
	    }); 

	    //close input, output stream
	    out.close();
	    stream.close();
	}

	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		
		//you do not this part to run in eclipse
		
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        String inPath = cmd.getOptionValue("input");
        String outPath = cmd.getOptionValue("output");
        
        
		//search url of open food fact database
		URL searchURL = new URL("https://world.openfoodfacts.org/cgi/search.pl");
		    
		//response format is json, take the first page which contains 20 products
		String searchQuery = "&search_simple=1&action=process&json=1&page=1&page_size=20";
		    
		//construct
		SearchProductByName searchTest = new SearchProductByName(searchURL, "UTF-8", searchQuery, "search_terms");

		//search all products in a input file, result is written to output file
		searchTest.searchFile(inPath, outPath);
		//searchTest.searchFile("/home/tyang/grocery.txt", "/home/tyang/out.txt");

	}

}
