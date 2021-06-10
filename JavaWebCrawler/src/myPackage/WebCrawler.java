package myPackage;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a WebCrawler.
 * @author Gavin Peng
 *        Partnered w/ Londy Tong lee, Louie bala. Callandra, Will, Kana'i Gooding
 */
public class WebCrawler {
  
  public static HashMap<String, Node> graph = new HashMap<>();

  /**
   * The main method.
   * @param args
   */
  public static void main(String[] args) {

    try {
      Node node = new Node(args[0], 0);
      DFS(node, Integer.parseInt(args[1]));
      reset();
      BFS(node, Integer.parseInt(args[1]));

    } catch (IOException e) {
      System.out.println("Error: Invalid web address /n" 
          + "Usage: /n"
          + "WebCrawler [valid web address] [depth to be searched]");
    }
  }

  /**
   * Find's the BFS
   * @param n the node
   * @param maxDepth the max depth
   */
  private static void BFS(Node n, int maxDepth) {
    Deque<Node> theQueue = new LinkedList<>();
    theQueue.offer(n);
    Node currentNode;

    while (!theQueue.isEmpty()) {
      currentNode = theQueue.poll();
      if (!currentNode.isVisited && currentNode.depth <= maxDepth) {
       currentNode.isVisited = true;
       System.out.println("BFS " + "visited " + currentNode.web + " Depth: " + currentNode.depth);
        for (Node temp : currentNode.getChildren()) {
          if ( temp.depth <= maxDepth) {
            theQueue.offer(temp);
          }
        }
      }
    }
  }

  /**
   * Find's the DFS
   * @param n the node
   * @param maxDepth the max depth
   */
  private static void DFS(Node n, int maxDepth) {
    Stack<Node> linkStack = new Stack<>();
    linkStack.push(n);
    Node currentNode;

    while (!linkStack.empty() ) {
      currentNode = linkStack.pop();
      if (!currentNode.isVisited && currentNode.depth <= maxDepth) {
       currentNode.isVisited = true;
       System.out.println("DFS " + "visited: " + currentNode.web + " Depth: " + currentNode.depth);
       graph.put(currentNode.web, currentNode);
        for (Node temp : currentNode.getChildren()) {
          if ( temp.depth <= maxDepth) {
            linkStack.push(temp);
          }
        }
      }
    }
  }
  
  /**
   * Reset's all the nodes visited to false
   */
  private static void reset() { 
    for (Node node : graph.values()) {
      node.isVisited = false;
    }
  }

  private static class Node {
    public ArrayList<Node> childLinks; 
    String web; //string of associated website
    public boolean isVisited; //true if website has been visited
    public int depth;

    Node(String url, int depth) throws IOException {
      if (!tryConnection(url) && !validateLink(url)) {
        throw new IOException();
      }
      this.web = url;
      this.depth = depth;
    }

    private static boolean validateLink(String url) {
      try {
        // Website input argument 
        String website = url;
        // Regex to check valid URL
        // Found this at https://www.geeksforgeeks.org/check-if-an-url-is-valid-or-not-using-regular-expression/
        String regex = "((http|https)://)(www.)[a-z"
            + "A-Z0-9@:%._\\+~#?&//=]{2,256}\\.(com|edu|mil|gov|org)"
            + "\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";
        // Compile pattern
        Pattern pattern = Pattern.compile(regex);
        // Finds match between regex string and user input website string
        Matcher websiteMatch = pattern.matcher(website);
        // If website is invalid, throw error to user
        if (websiteMatch.matches() == false) {
          return false;
        }
        return true;
      } catch (NumberFormatException e) { // Catches invalid depth crawl input i.e letters
        System.out.println("Error: Invalid Depth");
        return false;
      }
    }
    
    /**
     * checks the connection of the url 
     * @param url the url
     * @return whether the url works or not
     */
    private static boolean tryConnection(String url) {
      try {
        new URL(url).toURI();
      } catch (MalformedURLException | URISyntaxException e) {
        return false;
      }

      return true;
    }

    /**
     * get's the website
     * @param url the url
     * @return a string of the response
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("unused")
    private String getWebsite (String url) throws IOException, InterruptedException {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();
    }

    /**
     * get's the links of the webstie
     * @param website the website we looking at
     * @return the arraylist with all of our websites
     */
    @SuppressWarnings("unused")
    private ArrayList<String> getLinks (String website) {
      ArrayList<String> result = new ArrayList<String>();
      String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(website);
      while (m.find())
      {
        result.add(m.group());
      }

      return result;
    }

    /**
     * Get's the children of the nodes
     * @return an arraylist of nodes
     */
    private ArrayList<Node> getChildren() {
      if (childLinks != null) {
        return childLinks;
      }
      
      ArrayList<Node> listOfChildren = new ArrayList<Node>();
      try {
        ArrayList<String> listOfLinks = getLinks(getWebsite(web));
        for (String link : listOfLinks) {
          try {
            Node child = new Node(link, depth + 1);
            listOfChildren.add(child);
          } catch (IOException e) {
            //don't do anything
          }
        }
      } catch (IOException e) {
        //do nothing
      } catch (InterruptedException p) {
        //do nothing 
      }
      childLinks = listOfChildren;
      return listOfChildren;
    } 
  }
}
