package com.deadman.databitsfuture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.pedro.library.AutoPermissions;
import com.shrikanthravi.customnavigationdrawer2.data.MenuItem;
import com.shrikanthravi.customnavigationdrawer2.widget.SNavigationDrawer;

import java.util.ArrayList;
import java.util.List;

public class Drawer extends AppCompatActivity {

    SNavigationDrawer sNavigationDrawer;
    Class fragmentClass;
    public static Fragment fragment;

    // Dismisses the keyboard when you tap away from an edit text field
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        AutoPermissions.Companion.loadAllPermissions(this, 1);

        sNavigationDrawer = findViewById(R.id.navigationDrawer);

        // Creating a list of menu Items
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Welcome",R.drawable.news_bg));
        menuItems.add(new MenuItem("Crowd",R.drawable.feed_bg));
        menuItems.add(new MenuItem("Pit",R.drawable.message_bg));
        menuItems.add(new MenuItem("Master",R.drawable.music_bg));
        menuItems.add(new MenuItem("Settings",R.drawable.news_bg));

        // Add them to navigation drawer

        sNavigationDrawer.setMenuItemList(menuItems);
        fragmentClass =  Welcome.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.frameLayout, fragment).commit();
        }

        // Listener to handle the menu item click. It returns the position of the menu item clicked. Based on that you can switch between the fragments.

        sNavigationDrawer.setOnMenuItemClickListener(position -> {
            System.out.println("Position "+position);

            switch (position){
                case 0:{
                    fragmentClass = Welcome.class;
                    break;
                }
                case 1:{
                    fragmentClass = Crowd.class;
                    break;
                }
                case 2:{
                    fragmentClass = Pit.class;
                    break;
                }
                case 3:{
                    fragmentClass = Master.class;
                    break;
                }
                case 4:{
                    fragmentClass = Settings.class;
                    break;
                }

            }

            // Listener for drawer events such as opening and closing.
            sNavigationDrawer.setDrawerListener(new SNavigationDrawer.DrawerListener() {

                @Override
                public void onDrawerOpened() {

                }

                @Override
                public void onDrawerOpening(){

                }

                @Override
                public void onDrawerClosing(){
                    System.out.println("Drawer closed");

                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (fragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.frameLayout, fragment).commit();

                    }
                }

                @Override
                public void onDrawerClosed() {

                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    System.out.println("State "+newState);
                }
            });
        });
    }
}
