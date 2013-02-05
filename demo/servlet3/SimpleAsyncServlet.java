package mood.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

@javax.servlet.annotation.WebServlet(
    // servlet name
    name = "simple",
    // servlet url pattern
    value = {"/simple"},
    // async support needed
    asyncSupported = true
)
public class SimpleAsyncServlet extends HttpServlet {


/**
 * Simply spawn a new thread (from the app server's pool) for every new async request.
 * Will consume a lot more threads for many concurrent requests.
 */
public void service(ServletRequest req, final ServletResponse res)
    throws ServletException, IOException {

  // create the async context, otherwise getAsyncContext() will be null
  final AsyncContext ctx = req.startAsync();

  // set the timeout
  ctx.setTimeout(30000);

  // attach listener to respond to lifecycle events of this AsyncContext
  ctx.addListener(new AsyncListener() {
    public void onComplete(AsyncEvent event) throws IOException {
      log("onComplete called");
    }
    public void onTimeout(AsyncEvent event) throws IOException {
      log("onTimeout called");
    }
    public void onError(AsyncEvent event) throws IOException {
      log("onError called");
    }
    public void onStartAsync(AsyncEvent event) throws IOException {
      log("onStartAsync called");
    }
  });

  // spawn some task in a background thread
  ctx.start(new Runnable() {
    public void run() {

      try {
        ctx.getResponse().getWriter().write(
          "<h1>Processing task in bgt_id:" + Thread.currentThread().getId() + "</h1>");
      }
      catch (IOException e) {
        log("Problem processing task", e);
      }

      ctx.complete();
    }
  });

}

}