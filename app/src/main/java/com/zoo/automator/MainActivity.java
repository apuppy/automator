package com.zoo.automator;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.zoo.automator.databinding.ActivityMainBinding;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        Button fabActionButton = findViewById(R.id.button_rhino);
        fabActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call RhinoDemo and show result in the TextView
                String result = RhinoDemo.runDemo();
                TextView rhinoResult = findViewById(R.id.text_rhino_result);
                rhinoResult.setText(result);
                Log.d("debug", "Rhino result: " + result);
            }
        });
        // HTTP Request button logic
        Button httpButton = findViewById(R.id.button_http_request);
        TextView rhinoResult = findViewById(R.id.text_rhino_result);
        httpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpClient client = new OkHttpClient();
                // Create POST data
                okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");
                String jsonBody = "{\"field1\":\"value1\",\"field2\":\"value2\"}";
                okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody, JSON);
                // Build request with headers
                Request request = new Request.Builder()
                        .url("https://httpbin.org/post")
                        .addHeader("Custom-Header", "HeaderValue")
                        .addHeader("Another-Header", "AnotherValue")
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> rhinoResult.setText("HTTP request failed: " + e.getMessage()));
                        Log.e("HTTP", "Request failed", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String msg = "HTTP response status: " + response.code();
                        runOnUiThread(() -> rhinoResult.setText(msg));
                        if (response.body() != null) {
                            String body = response.body().string();
                            Log.d("HTTP", "Response: " + body);
                        } else {
                            Log.d("HTTP", "Empty response body");
                        }
                    }
                });
            }
        });
        // Accessibility settings button logic
        Button accessibilityButton = findViewById(R.id.button_open_accessibility_settings);
        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the system accessibility settings and wait for result using Activity Result API
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                accessibilitySettingsLauncher.launch(intent);
            }
        });
        // Chrome automation button logic
        Button chromeButton = findViewById(R.id.button_run_chrome_automation);
        chromeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Try to launch Chrome using ACTION_VIEW with a dummy URL
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("http://www.bing.com"));
                intent.setPackage("com.android.chrome");
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Snackbar.make(v, "Chrome is not installed.", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        FrameLayout bottomNavContainer = findViewById(R.id.bottom_nav_container);
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        LayoutInflater inflater = LayoutInflater.from(this);
        // Do not show dashboard by default, leave container empty
        bottomNavContainer.removeAllViews();
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                // Show dashboard in the container, do not start a new activity
                bottomNavContainer.removeAllViews();
                bottomNavContainer.addView(inflater.inflate(R.layout.page_dashboard, bottomNavContainer, false));
                setContentMainVisibility(false);
                return true;
            } else if (item.getItemId() == R.id.nav_setting) {
                // Show setting in the container, do not start a new activity
                bottomNavContainer.removeAllViews();
                bottomNavContainer.addView(inflater.inflate(R.layout.page_setting, bottomNavContainer, false));
                setContentMainVisibility(false);
                return true;
            }
            return false;
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Handle left drawer navigation for Home
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // When Home is selected from the drawer, show content_main controls and clear the container
                setContentMainVisibility(true);
                bottomNavContainer.removeAllViews();
                return true;
            }
            // Let NavigationUI handle other items
            return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment_content_main));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Use Activity Result API instead of deprecated startActivityForResult
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> accessibilitySettingsLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                // User returned from accessibility settings, check if service is enabled
                if (isAccessibilityServiceEnabled()) {
                    Snackbar.make(findViewById(android.R.id.content), "Accessibility enabled!", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Accessibility not enabled.", Snackbar.LENGTH_LONG).show();
                }
            });

    private boolean isAccessibilityServiceEnabled() {
        android.content.ComponentName expectedComponentName = new android.content.ComponentName(this, com.zoo.automator.MyAccessibilityService.class);
        android.text.TextUtils.SimpleStringSplitter colonSplitter = new android.text.TextUtils.SimpleStringSplitter(':');
        String enabledServices = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServices == null) return false;
        colonSplitter.setString(enabledServices);
        while (colonSplitter.hasNext()) {
            String componentName = colonSplitter.next();
            if (componentName.equalsIgnoreCase(expectedComponentName.flattenToString())) {
                return true;
            }
        }
        return false;
    }

    // Helper to show/hide content_main controls
    private void setContentMainVisibility(boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        findViewById(R.id.text_rhino_result).setVisibility(v);
        findViewById(R.id.button_rhino).setVisibility(v);
        findViewById(R.id.button_http_request).setVisibility(v);
        findViewById(R.id.button_open_accessibility_settings).setVisibility(v);
        findViewById(R.id.button_run_chrome_automation).setVisibility(v);
    }
}