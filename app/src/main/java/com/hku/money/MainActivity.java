package com.hku.money;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.hku.money.data.LiveApi api = new com.hku.money.data.LiveApi(this);
        if (!api.signedIn()) {
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (!api.preview() && android.os.Build.VERSION.SDK_INT >= 37 && checkSelfPermission(
                android.Manifest.permission.ACCESS_LOCAL_NETWORK) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_LOCAL_NETWORK}, 1);
        }
        setContentView(R.layout.activity_main);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        NavController navController = navHost.getNavController();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setLabelVisibilityMode(com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED);
        bottomNavigation.setItemActiveIndicatorEnabled(false);
        bottomNavigation.setBackgroundColor(android.graphics.Color.WHITE);
        android.content.res.ColorStateList tabColors = new android.content.res.ColorStateList(
            new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
            new int[]{0xff14636a, 0xff62766f});
        bottomNavigation.setItemIconTintList(tabColors);
        bottomNavigation.setItemTextColor(tabColors);
        getWindow().setStatusBarColor(0xfff2faf1);
        NavigationUI.setupWithNavController(bottomNavigation, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean child = destination.getId() == R.id.publishFragment || destination.getId() == R.id.channelsFragment || destination.getId() == R.id.profilePageFragment;
            bottomNavigation.setVisibility(child ? android.view.View.GONE : android.view.View.VISIBLE);
        });
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars() | androidx.core.view.WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
