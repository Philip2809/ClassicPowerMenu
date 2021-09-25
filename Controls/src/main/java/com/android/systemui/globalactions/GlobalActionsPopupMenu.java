/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.globalactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.LayoutDirection;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;

import com.android.systemui.controls.R;
import com.android.systemui.util.MonetColorProvider;
import com.android.systemui.util.MonetColorProviderProvider;

/**
 * Customized widget for use in the GlobalActionsDialog. Ensures common positioning and user
 * interactions.
 *
 * It should be created with a {@link Context} with the right theme
 */
public class GlobalActionsPopupMenu extends ListPopupWindow {
    private Context mContext;
    private boolean mIsDropDownMode;
    private int mMenuVerticalPadding = 0;
    private int mGlobalActionsSidePadding = 0;
    private ListAdapter mAdapter;
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener;

    private MonetColorProvider monet = MonetColorProviderProvider.INSTANCE.getMonetColorProvider();

    public GlobalActionsPopupMenu(@NonNull Context context, boolean isDropDownMode) {
        super(context);
        mContext = context;
        Resources res = mContext.getResources();
        Drawable background = res.getDrawable(R.drawable.rounded_bg_full, context.getTheme());
        background.setTint(monet.getBackgroundColorSecondary(context, true));
        setBackgroundDrawable(background);
        mIsDropDownMode = isDropDownMode;

        // required to show above the global actions dialog
        //setWindowLayoutType(WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY);
        setInputMethodMode(INPUT_METHOD_NOT_NEEDED);
        setModal(true);

        mGlobalActionsSidePadding = res.getDimensionPixelSize(R.dimen.global_actions_side_margin);
        if (!isDropDownMode) {
            mMenuVerticalPadding = res.getDimensionPixelSize(R.dimen.control_menu_vertical_padding);
        }
    }

    /**
     * Set the listadapter used to populate this menu.
     */
    public void setAdapter(@Nullable ListAdapter adapter) {
        mAdapter = adapter;
        super.setAdapter(adapter);
    }

    /**
      * Show the dialog.
      */
    @SuppressLint("WrongConstant")
    public void show() {
        // need to call show() first in order to construct the listView
        super.show();
        if (mOnItemLongClickListener != null) {
            getListView().setOnItemLongClickListener(mOnItemLongClickListener);
        }

        ListView listView = getListView();
        Resources res = mContext.getResources();

        setVerticalOffset(-getAnchorView().getHeight() / 2);

        if (mIsDropDownMode) {
            // use a divider
            listView.setDividerHeight(res.getDimensionPixelSize(R.dimen.control_list_divider));
            listView.setDivider(res.getDrawable(R.drawable.controls_list_divider_inset));
        } else {
            if (mAdapter == null) return;

            // width should be between [.5, .9] of screen
            int parentWidth = res.getSystem().getDisplayMetrics().widthPixels;
            int widthSpec = MeasureSpec.makeMeasureSpec(
                    (int) (parentWidth * 0.9), MeasureSpec.AT_MOST);
            int maxWidth = 0;
            for (int i = 0; i < mAdapter.getCount(); i++) {
                View child = mAdapter.getView(i, null, listView);
                child.measure(widthSpec, MeasureSpec.UNSPECIFIED);
                int w = child.getMeasuredWidth();
                maxWidth = Math.max(w, maxWidth);
            }
            int width = Math.max(maxWidth, (int) (parentWidth * 0.5));
            listView.setPadding(0, mMenuVerticalPadding, 0, mMenuVerticalPadding);

            setWidth(width);
            if (getAnchorView().getLayoutDirection() == LayoutDirection.LTR) {
                setHorizontalOffset(getAnchorView().getWidth() - mGlobalActionsSidePadding - width);
            } else {
                setHorizontalOffset(mGlobalActionsSidePadding);
            }
        }

        super.show();
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }
}