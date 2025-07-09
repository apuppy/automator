package com.zoo.automator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_dashboard);
        Button runJsDemo = findViewById(R.id.button_run_js_demo);
        TextView textDashboard = findViewById(R.id.text_dashboard);
        runJsDemo.setOnClickListener(v -> {
            Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show();
            String result = "(no result)";
            try {
                result = runDemoJs();
            } catch (Exception e) {
                result = "Exception: " + e.getMessage();
                Log.e("DashboardActivity", "Exception running JS", e);
            }
            textDashboard.setText(result);
            Toast.makeText(this, "JS Demo Result: " + result, Toast.LENGTH_SHORT).show();
            Log.d("DashboardActivity", "JS Demo Result: " + result);
        });
        Log.d("DashboardActivity", "Button and TextView wired: " + (runJsDemo != null) + ", " + (textDashboard != null));
    }

    private String runDemoJs() {
        Context rhino = Context.enter();
        try {
            rhino.setOptimizationLevel(-1);
            Scriptable scope = rhino.initStandardObjects();
            String jsCode = loadAssetJs("js/demo.js");
            Object result = rhino.evaluateString(scope, jsCode, "demo.js", 1, null);
            return Context.toString(result);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        } finally {
            Context.exit();
        }
    }

    private String loadAssetJs(String assetPath) throws Exception {
        InputStream is = getAssets().open(assetPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
}
