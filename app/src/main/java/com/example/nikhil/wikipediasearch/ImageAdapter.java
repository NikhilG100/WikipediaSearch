/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.example.nikhil.wikipediasearch;


import android.animation.Animator;
import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

public class ImageAdapter extends BaseAdapter {

    private static String[] URLS = new String[50];
    private int cnt = 0;
    private final String TAG = "Image Search";

    public int getCount() {
        return URLS.length;
    }

    public String getItem(int position) {
        return URLS[position];
    }

    public long getItemId(int position) {
        if (URLS[position] != null)
          return URLS[position].hashCode();

        return 0;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = new ImageView(parent.getContext());
            view.setPadding(6, 2, 6, 2);
            //view.getBackground().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.DARKEN);
            view.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.Color_1));

            view.invalidate();
        }

        int width= parent.getContext().getResources().getDisplayMetrics().widthPixels;
        if (URLS[position] != null) {
            Picasso.with(parent.getContext())
                    .load(URLS[position])
                    .noFade()
                    .resize(width/4, width/4)
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.loading_image)
                    .into((ImageView)view);
            }

        view.setTag(position);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int pos = (int) v.getTag();
                    zoomImageFromThumb(v, pos);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    //public String[] getURLArray() { return URLS;}

    //public void setImageURL(String imgURL)  {
    //    URLS[cnt++] = imgURL;
    //}

    public void resetArray() {
        Arrays.fill(URLS, "http://www.emgreenfield.com/UploadedFiles/Product/no_image.png");
        cnt = 0;
    }

    public void setArray(String [] strArray) {
        URLS = strArray;
    }

    private void zoomImageFromThumb(final View thumbView, int imageId) {
        // If there's an animation in progress, cancel it immediately and
        // proceed with this one.
        int startIndex = 0;
        int lastIndex = 0;

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) ((Activity) thumbView.getContext())
                .findViewById(R.id.expanded_image);

        expandedImageView.setBackgroundColor(ContextCompat.getColor(thumbView.getContext(), R.color.Color_1));
        String newURL = URLS[imageId].replace("/thumb", "");

        lastIndex = newURL.lastIndexOf("/");

        String newURLSubString = newURL.substring(lastIndex);

        newURL = newURL.replace(newURLSubString, "");

        int width= thumbView.getContext().getResources().getDisplayMetrics().widthPixels;
        int height = thumbView.getContext().getResources().getDisplayMetrics().heightPixels;

        Picasso.with(thumbView.getContext())
                .load(newURL)
                .noFade()
                .resize(width, height/2)
                .centerCrop()
                .error(R.drawable.no_image)
                .placeholder(R.drawable.progress_animation)
                .into((ImageView)expandedImageView);

        Log.d(TAG,"setOnClickListener" + newURL);

        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the
        // top-left corner of
        // the zoomed-in view (the default is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);


        // Upon clicking the zoomed-in image, it should zoom back down to the
        // original bounds
        // and show the thumbnail instead of the expanded image.
        //final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandedImageView.setVisibility(View.GONE);
            }
        });
    }
}
