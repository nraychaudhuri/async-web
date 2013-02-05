@javax.servlet.annotation.WebServlet(
    // servlet name
    name = "complex",
    // servlet url pattern
    value = {"/complex"},
    // async support needed
    asyncSupported = true,
    // servlet init params
    initParams = {
        @WebInitParam(name = "threadpoolsize", value = "3")
    }
)
public class ComplexAsyncServlet extends HttpServlet {

public static final AtomicInteger counter = new AtomicInteger(0);
public static final int CALLBACK_TIMEOUT = 60000;
public static final int MAX_SIMULATED_TASK_LENGTH_MS = 5000;

/** executor svc */
private ExecutorService exec;

/** create the executor */
public void init() throws ServletException {

  int size = Integer.parseInt(
      getInitParameter("threadpoolsize"));
  exec = Executors.newFixedThreadPool(size);

}

/** destroy the executor */
public void destroy() {

  exec.shutdown();

}

/**
 * Spawn the task on the provided {@link #exec} object.
 * This limits the max number of threads in the
 * pool that can be spawned and puts a ceiling on
 * the max number of threads that can be used to
 * the init param "threadpoolsize".
 */
public void service(final ServletRequest req, final ServletResponse res)
    throws ServletException, IOException {

  // create the async context, otherwise getAsyncContext() will be null
  final AsyncContext ctx = req.startAsync();

  // set the timeout
  ctx.setTimeout(CALLBACK_TIMEOUT);

  // attach listener to respond to lifecycle events of this AsyncContext
  ctx.addListener(new AsyncListener() {
    /** complete() has already been called on the async context, nothing to do */
    public void onComplete(AsyncEvent event) throws IOException { }
    /** timeout has occured in async task... handle it */
    public void onTimeout(AsyncEvent event) throws IOException {
      log("onTimeout called");
      log(event.toString());
      ctx.getResponse().getWriter().write("TIMEOUT");
      ctx.complete();
    }
    /** THIS NEVER GETS CALLED - error has occured in async task... handle it */
    public void onError(AsyncEvent event) throws IOException {
      log("onError called");
      log(event.toString());
      ctx.getResponse().getWriter().write("ERROR");
      ctx.complete();
    }
    /** async context has started, nothing to do */
    public void onStartAsync(AsyncEvent event) throws IOException { }
  });

  // simulate error - this does not cause onError - causes network error on client side
  if (counter.addAndGet(1) < 5) {
    throw new IndexOutOfBoundsException("Simulated error");
  }
  else {
    // spawn some task to be run in executor
    enqueLongRunningTask(ctx);
  }

}

/**
 * if something goes wrong in the task, it simply causes timeout condition that causes
 * the async context listener to be invoked (after the fact)
 * <p/>
 * if the {@link AsyncContext#getResponse()} is null, that means this context has
 * already timedout (and context listener has been invoked).
 */
private void enqueLongRunningTask(final AsyncContext ctx) {

  exec.execute(new Runnable() {
    public void run() {

      try {

        // simulate random delay
        int delay = new Random().nextInt(MAX_SIMULATED_TASK_LENGTH_MS);
        Thread.currentThread().sleep(delay);

        // response is null if the context has already timedout
        // (at this point the app server has called the listener already)
        ServletResponse response = ctx.getResponse();
        if (response != null) {
          response.getWriter().write(
              MessageFormat.format("<h1>Processing task in bgt_id:[{0}], delay:{1}</h1>",
                                   Thread.currentThread().getId(), delay)
          );
          ctx.complete();
        }
        else {
          throw new IllegalStateException("Response object from context is null!");
        }
      }
      catch (Exception e) {
        log("Problem processing task", e);
        e.printStackTrace();
      }

    }
  });
}

}