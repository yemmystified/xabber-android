package com.xabber.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.intent.AccountIntentBuilder;
import com.xabber.android.data.intent.EntityIntentBuilder;
import com.xabber.android.data.roster.AbstractContact;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.ui.helper.ContactTitleInflater;
import com.xabber.android.ui.helper.ManagedActivity;
import com.xabber.androiddev.R;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;


public class ContactViewerNew extends ManagedActivity implements ObservableScrollViewCallbacks {

    private int toolbarHeight;
    private View actionBarView;
    private int paddingLeft;
    private int paddingRight;
    private int actionBarSize;
    private int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_viewer);
        setSupportActionBar((Toolbar) findViewById(R.id.contact_viewer_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        int[] accountActionBarColors;
        int[] accountStatusBarColors;

        accountActionBarColors = getResources().getIntArray(R.array.account_action_bar);
        accountStatusBarColors = getResources().getIntArray(R.array.account_status_bar);

        actionBarView = findViewById(R.id.title);

        paddingLeft = getResources().getDimensionPixelSize(R.dimen.contact_title_padding_left);
        paddingRight = getResources().getDimensionPixelSize(R.dimen.contact_title_padding_right);

        TypedArray a = getTheme().obtainStyledAttributes(R.style.Theme, new int[] {R.attr.colorPrimary});

        AbstractContact bestContact = RosterManager.getInstance().getBestContact(getAccount(getIntent()), getUser(getIntent()));

        ContactTitleInflater.updateTitle(actionBarView, this, bestContact);

        int colorLevel = AccountManager.getInstance().getColorLevel(bestContact.getAccount());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(accountStatusBarColors[colorLevel]);
        }

        actionBarView.setBackgroundDrawable(new ColorDrawable(accountActionBarColors[colorLevel]));

        toolbarHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_height);

        final ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.scroll);
        scrollView.setScrollViewCallbacks(this);

        ScrollUtils.addOnGlobalLayoutListener(findViewById(R.id.contact_viewer_toolbar), new Runnable() {
            @Override
            public void run() {
                updateFlexibleSpaceText(scrollView.getCurrentScrollY());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        actionBarSize = getActionBarSize();
        radius = toolbarHeight - actionBarSize;
    }

    protected int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateFlexibleSpaceText(scrollY);
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private void updateFlexibleSpaceText(final int scrollY) {
        if (scrollY <= radius) {
            int newPadding = (int) round(sqrt(pow(radius, 2) - pow(scrollY - radius, 2)));

            if (newPadding > radius) {
                newPadding = radius;
            }

            actionBarView.setPadding(paddingLeft + newPadding, 0, paddingRight, 0);
        }

        int newHeight = toolbarHeight - scrollY;
        if (newHeight < actionBarSize) {
            newHeight = actionBarSize;
        }

        actionBarView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, newHeight));
    }

    public static Intent createIntent(Context context, String account, String user) {
        return new EntityIntentBuilder(context, ContactViewerNew.class).setAccount(account).setUser(user).build();
    }
    private static String getAccount(Intent intent) {
        return AccountIntentBuilder.getAccount(intent);
    }

    private static String getUser(Intent intent) {
        return EntityIntentBuilder.getUser(intent);
    }

}
