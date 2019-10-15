package com.simcoder.whatsappclonea.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.simcoder.whatsappclonea.Object.ChatObject;
import com.simcoder.whatsappclonea.Fragment.Camera.CameraViewFragment;
import com.simcoder.whatsappclonea.Fragment.ChatListFragment;
import com.simcoder.whatsappclonea.Fragment.Camera.DisplayImageFragment;
import com.simcoder.whatsappclonea.Login.AuthenticationActivity;
import com.simcoder.whatsappclonea.Object.UserObject;
import com.simcoder.whatsappclonea.R;

import java.util.ArrayList;

/**
 * Main Activity, controller for the main fragments of the project.
 */
public class MainActivity extends AppCompatActivity{

    FragmentManager fm = getSupportFragmentManager();

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;


    FloatingActionButton fab;

    AppBarLayout mAppBar;
    int appBarHeight;

    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissions();

        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        Fresco.initialize(this);

        getUserInfo();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mAppBar = findViewById(R.id.appbar);
        mAppBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                appBarHeight = mAppBar.getMeasuredHeight(); //height is ready
                if(mSectionsPagerAdapter.getChatListFragment()!= null)
                    ((ChatListFragment) mSectionsPagerAdapter.getChatListFragment()).updatePaddingTop();

            }
        });


        tabLayout = findViewById(R.id.tabs);
        setCameraTabWidth();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition() == 0){
                    hideToolbar();
                    hideFab();
                }else{
                    showToolbar();
                    showFab();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mViewPager.setCurrentItem(1);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), FindUserActivity.class));
            }
        });


    }


    UserObject mUser;
    private void getUserInfo() {
        mUser = new UserObject(FirebaseAuth.getInstance().getUid());
        FirebaseDatabase.getInstance().getReference()
                .child("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUser.parseObject(dataSnapshot);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    public UserObject getUser() {
        return mUser;
    }

    void setCameraTabWidth(){
        LinearLayout layout = ((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(1));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.weight = 1.5f;
        layout.setLayoutParams(layoutParams);
    }

    private void hideToolbar() {
        mAppBar.animate().translationY(-appBarHeight);
        // Scale down animation
    }
    private void showToolbar() {
        mAppBar.animate().translationY(0);
    }
    void hideFab() {
        if(fab == null)
            return;
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                fab.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fab.startAnimation(shrink);
    }
    void showFab(){
        if(fab == null)
            return;
        fab.setVisibility(View.VISIBLE);
        ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        expand.setDuration(100);     // animation duration in milliseconds
        expand.setInterpolator(new AccelerateInterpolator());
        expand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fab.startAnimation(expand);
    }

    public int getAppBarHeight() {
        return appBarHeight;
    }

    ArrayList<ChatObject> chatList = new ArrayList<>();
    public ArrayList<ChatObject> getChatList() {
        return chatList;
    }
    public void setChatList(ArrayList<ChatObject> chatList) {
        this.chatList = chatList;
    }

    Bitmap bitmap = null;
    public void setBitmapToSend(Bitmap bitmapToSend){
        this.bitmap = bitmapToSend;
    }
    public Bitmap getBitmapToSend() {
        return bitmap;
    }

    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    public void  dismissProgressDialog(){
        if(mDialog!=null)
            mDialog.dismiss();
    }


    public void clearBackStack(){
        while(fm.getBackStackEntryCount()>0)
            onBackPressed();
    }

    public void openEditProfileActivity(){
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("userObject", mUser);
        startActivity(intent);
    }
    public void openDisplayImageFragment(){
        fm.beginTransaction()
                .replace(R.id.fragmentHolder, DisplayImageFragment.newInstance(), "DisplayImageFragment")
                .addToBackStack(null)
                .commit();
    }
    public void openChooseReceiverFragment(){
        fm.beginTransaction()
                .replace(R.id.fragmentHolder, ChatListFragment.newInstance(false), "ChooseReceiverFragment")
                .addToBackStack(null)
                .commit();
    }
    private void logout(){
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_profile) {
            openEditProfileActivity();
            return true;
        }
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Fragment cameraViewFragment, chatListFragment;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    if (cameraViewFragment == null)
                        cameraViewFragment = new CameraViewFragment();
                    return cameraViewFragment;
                case 1:
                    if (chatListFragment == null)
                        chatListFragment = ChatListFragment.newInstance(true);
                    return chatListFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        public Fragment getChatListFragment() {
            return chatListFragment;
        }
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(fm.getBackStackEntryCount()==0)
            mViewPager.setCurrentItem(1);
    }
}
