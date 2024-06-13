package com.example.meltingpot;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.widget.AppCompatButton;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton fab;
    private BottomNavigationView bottomNav;
    private int lastSelectedItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.nav_view);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        fab = findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        // Show the HomeFragment by default
        if (savedInstanceState == null) {
            lastSelectedItemId = R.id.navigation_home;
            bottomNav.setSelectedItemId(lastSelectedItemId);
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                    new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    if (lastSelectedItemId == itemId) {
                        return false;
                    }

                    switch (itemId) {
                        case R.id.navigation_home:
                            selectedFragment = new HomeFragment();
                            fab.setVisibility(View.GONE);
                            fab.setBackgroundTintList(getResources().getColorStateList(R.color.orange));
                            bottomNav.setBackgroundColor(getResources().getColor(R.color.orange));
                            break;
                        case R.id.navigation_search:
                            selectedFragment = new SearchFragment();
                            fab.setVisibility(View.GONE);
                            bottomNav.setBackgroundColor(getResources().getColor(R.color.orange));
                            break;
                        case R.id.navigation_health:
                            selectedFragment = new HealthFragment();
                            fab.setVisibility(View.VISIBLE);
                            fab.setBackgroundTintList(getResources().getColorStateList(R.color.mint));
                            bottomNav.setBackgroundColor(getResources().getColor(R.color.mint));
                            break;
                        case R.id.navigation_recipes:
                            selectedFragment = new RecipesFragment();
                            fab.setVisibility(View.VISIBLE);
                            fab.setBackgroundTintList(getResources().getColorStateList(R.color.orange));
                            bottomNav.setBackgroundColor(getResources().getColor(R.color.orange));
                            break;
                        case R.id.navigation_profile:
                            selectedFragment = new ProfileFragment();
                            fab.setVisibility(View.GONE);
                            bottomNav.setBackgroundColor(getResources().getColor(R.color.orange));
                            break;
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.nav_host_fragment, selectedFragment)
                                .commit();
                        lastSelectedItemId = itemId;
                        return true;
                    }
                    return false;
                }
            };

    @Override
    public void onBackPressed() {
        if (lastSelectedItemId != R.id.navigation_home) {

            bottomNav.setSelectedItemId(R.id.navigation_home);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new HomeFragment())
                    .commit();
            lastSelectedItemId = R.id.navigation_home;
        } else {
            super.onBackPressed();
        }
    }
}

