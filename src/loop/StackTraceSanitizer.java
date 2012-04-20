package loop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class StackTraceSanitizer {
  private static final List<String> REFLECT_SEGMENT = Arrays.asList(
     "sun.reflect.NativeMethodAccessorImpl",
     "sun.reflect.NativeMethodAccessorImpl",
     "sun.reflect.DelegatingMethodAccessorImpl",
     "java.lang.reflect.Method",
     "loop.runtime.Caller"
    );

  public static void clean(Throwable t) {
    StackTraceElement[] trace = t.getStackTrace();

    List<StackTraceElement> pruned = new ArrayList<StackTraceElement>(trace.length);
    for (int i = 0, traceLength = trace.length; i < traceLength; i++) {
      StackTraceElement element = trace[i];

      if ("loop.runtime.Caller".equals(element.getClassName()))
        continue;

      if (REFLECT_SEGMENT.get(0).equals(element.getClassName())) {
        int r = 1, k = i + 1;
        while (r < REFLECT_SEGMENT.size() && k < trace.length
            && REFLECT_SEGMENT.get(r).equals(trace[k].getClassName())) {
          r++;
          k++;
        }

        if (r == REFLECT_SEGMENT.size()) {
          i = k - 1;
          continue;
        }
      }

      pruned.add(element);
    }

    t.setStackTrace(pruned.toArray(new StackTraceElement[pruned.size()]));
  }
}
