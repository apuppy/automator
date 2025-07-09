package com.zoo.automator;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class RhinoDemo {
    public static String runDemo() {
        // Create and enter a Context. A Context stores information about the execution environment of a script.
        Context rhino = Context.enter();
        try {
            rhino.setOptimizationLevel(-1); // Required for Android
            Scriptable scope = rhino.initStandardObjects();
            // JavaScript code to get current timestamp in 'Y-m-d H:i:s' format
            String jsCode = """
                function pad(n) { return n < 10 ? '0' + n : n; }
                var now = new Date();
                var year = now.getFullYear();
                var month = pad(now.getMonth() + 1);
                var day = pad(now.getDate());
                var hour = pad(now.getHours());
                var min = pad(now.getMinutes());
                var sec = pad(now.getSeconds());
                var timestamp = year + '-' + month + '-' + day + ' ' + hour + ':' + min + ':' + sec;
                var greeting = 'Hello from Rhino!';
                greeting + '\\n2+2=' + (2+2) + '\\nTimestamp: ' + timestamp;
            """;
            Object result = rhino.evaluateString(scope, jsCode, "RhinoDemo", 1, null);
            return Context.toString(result);
        } finally {
            Context.exit();
        }
    }
}
