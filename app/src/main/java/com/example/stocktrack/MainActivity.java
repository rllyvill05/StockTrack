package com.example.stocktrack;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";
    private static final String TAG_HOME = "home";
    private static final String TAG_ADD = "add";
    private static final String TAG_LIST = "list";
//    private static final String TAG_SCAN = "scan";

    private ImageButton homeButton;
    private ImageButton addButton;
    private ImageButton listButton;
//    private Toolbar toolbar;
    private String currentFragmentTag = TAG_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent));

        setContentView(R.layout.activity_main_pager);

        com.example.stocktrack.database.ProductRepository.getInstance(this);

        // Handle window insets for proper spacing
        View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply top padding to fragment container for status bar
            View fragmentContainer = findViewById(R.id.fragment_container);
            fragmentContainer.setPadding(0, systemBars.top, 0, 0);

            // Apply bottom padding to navigation bar for system navigation
            View bottomNav = findViewById(R.id.bottom_navigation_container);
            bottomNav.setPadding(0, 12, 0, systemBars.bottom + 12);


            return WindowInsetsCompat.CONSUMED;
        });

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT, TAG_HOME);
        }
        initializeViews();
        setupBottomNavigation();
        setupWindowInsets();

        //        // Load initial fragment
        if (savedInstanceState == null) {
            navigateToListFragment();
        } else {
            // Fragment manager will restore fragments automatically
            updateBottomNavigationState();
        }

        // Rest of your initialization code...
//        setupBottomNavigation();
        loadFragment(new ListFragment());
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main_pager);
//
//        // Initialize ProductRepository with context
//        com.example.stocktrack.database.ProductRepository.getInstance(this);
//
//        // Restore state after configuration change
//        if (savedInstanceState != null) {
//            currentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT, TAG_HOME);
//        }
//
//        initializeViews();
////        setupToolbar();
//        setupBottomNavigation();
//        setupWindowInsets();
//
//        // Load initial fragment
//        if (savedInstanceState == null) {
//            navigateToAddFragment();
//        } else {
//            // Fragment manager will restore fragments automatically
//            updateBottomNavigationState();
//        }
//    }

private void loadFragment(Fragment fragment) {
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
}

    private void initializeViews() {
//        toolbar = findViewById(R.id.my_toolbar);
//        homeButton = findViewById(R.id.home_image_button);
        addButton = findViewById(R.id.add_image_button);
        listButton = findViewById(R.id.list_image_button);
    }

//    private void setupToolbar() {
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setTitle(R.string.toolbar_title);
//            }
//        }
//    }

    private void setupBottomNavigation() {
//        if (homeButton != null) {
//            homeButton.setOnClickListener(v -> {
//                animateButtonClick(v);
//                navigateToHomeFragment();
//            });
//        }

        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                animateButtonClick(v);
                navigateToAddFragment();
            });
        }

        if (listButton != null) {
            listButton.setOnClickListener(v -> {
                animateButtonClick(v);
                navigateToListFragment();
            });
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void animateButtonClick(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animation.setDuration(100);
        view.startAnimation(animation);
    }

//    public void navigateToHomeFragment() {
//        currentFragmentTag = TAG_HOME;
//        replaceFragment(new HomeFragment(), TAG_HOME);
//        updateBottomNavigationState();
//    }

    public void navigateToAddFragment() {
        currentFragmentTag = TAG_ADD;
        replaceFragment(new AddFragment(), TAG_ADD);
        updateBottomNavigationState();
    }

    public void navigateToListFragment() {
        currentFragmentTag = TAG_LIST;
        ListFragment listFragment = new ListFragment();
        replaceFragment(listFragment, TAG_LIST);
        updateBottomNavigationState();
    }

    public void navigateToProductDetail(Product product) {
        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(product);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, detailFragment, "detail");
        transaction.addToBackStack(null); // Allow back navigation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    public void navigateToEditFragment(Product product) {
        EditFragment editFragment = EditFragment.newInstance(product);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, editFragment, "edit");
        transaction.addToBackStack(null); // Allow back navigation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Add slide animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commitAllowingStateLoss();
        
        // Force immediate execution to ensure fragment is displayed
        try {
            fragmentManager.executePendingTransactions();
        } catch (Exception e) {
            // Ignore any exceptions during execution
        }
    }

    private void updateToolbarTitle(int titleResId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleResId);
        }
    }

    private void updateBottomNavigationState() {
        if (addButton != null) {
            addButton.setAlpha(0.6f);
        }
        if (listButton != null) {
            listButton.setAlpha(0.6f);
        }

        // Highlight current button
        switch (currentFragmentTag) {
            case TAG_HOME:
                if (homeButton != null) {
                    homeButton.setAlpha(1.0f);
                }
                break;
            case TAG_ADD:
                if (addButton != null) {
                    addButton.setAlpha(1.0f);
                }
                break;
            case TAG_LIST:
                if (listButton != null) {
                    listButton.setAlpha(1.0f);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            currentFragment.onCreateOptionsMenu(menu, getMenuInflater());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_FRAGMENT, currentFragmentTag);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Handle back navigation for fragments in back stack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            // Update navigation state when returning from detail/edit fragment
            // Restore the appropriate fragment tag based on the fragment that's now visible
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                String tag = currentFragment.getTag();
                if (tag != null) {
                    currentFragmentTag = tag;
                }
            }
            updateBottomNavigationState();
        } else {
            super.onBackPressed();
        }
    }
}