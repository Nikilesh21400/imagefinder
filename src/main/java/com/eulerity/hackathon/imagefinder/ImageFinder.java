package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
  		};

	public static ArrayList<String> ImageUrls; // List of Image Urls
	public static ArrayList<String> UrlList; // List of Urls for which Images should be crawled

	// Method to crawl Images of each Url passed
	ArrayList<String> getUrls(String url){
		ArrayList<String> tmpUrlList=new ArrayList<>();
		tmpUrlList.add(url); // Adding the url passed by user to fetch images
		try {
            // Connecting to the user entered website and parse the document
            Document document = Jsoup.connect(url).get();

            // Crawling all urls (Hyperlinks) from website
            Elements urlElements = document.select("a[href]");

            // Extract the src attribute of each image element and print it
            for (Element urlElement : urlElements) {
                String tmpurl = urlElement.attr("abs:href"); // Use "abs:href" to get the absolute URL
                System.out.println(tmpurl);
				tmpUrlList.add(tmpurl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
		return tmpUrlList;
	}

	// Method to crawl Images of each Url passed
	static class GetImagesfromUrl implements Callable<List<String>> {
        private final String url;

        public GetImagesfromUrl(String url) {
            this.url = url;
        }
		@Override
        public ArrayList<String> call() {
			ArrayList<String> tmpImages=new ArrayList<>();
			try {
				// Connecting to the Url and parse the document
				Document document = Jsoup.connect(url).get();
				// Selecting all image elements from website
				Elements imageElements = document.select("img");
				// Extract the src attribute of each image element and print it
				for (Element imageElement : imageElements) {
					String imgUrl = imageElement.attr("abs:src"); // Use "abs:src" to get the absolute URL
					System.out.println(imgUrl);
					tmpImages.add(imgUrl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return tmpImages;
		}
	}
	

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		System.out.println("Got request of:" + path + " with query param:" + url);

		try {
            
			ImageUrls=new ArrayList<>(); // Initializing arraylist to add image urls
			UrlList=getUrls(url); // Crawling the urls present in the user entered website

			ExecutorService executorService = Executors.newFixedThreadPool(UrlList.size()); // Thread pool to the no of urls for multi-threading
			
			List<Future<List<String>>> futureResults = new ArrayList<>();

			for (String tmpUrl : UrlList) {
				futureResults.add(executorService.submit(new GetImagesfromUrl(tmpUrl)));
			}

			for (Future<List<String>> result : futureResults) {
				try {
					ImageUrls.addAll(result.get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	
			executorService.shutdown();


        } catch (Exception e) {
            e.printStackTrace();
        }

		resp.getWriter().print(GSON.toJson(ImageUrls));

		//resp.getWriter().print(GSON.toJson(testImages));
	}
}
