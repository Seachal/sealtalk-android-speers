package com.caesar.rongcloudspeed.circle.widgets;

/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.facebook.common.internal.Preconditions;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.R;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.SimpleDraweeControllerBuilder;
import com.facebook.drawee.view.GenericDraweeView;

/**
 * This view takes a uri as input and internally builds and sets a controller.
 *
 * <p>This class must be statically initialized in order to be used. If you are using the Fresco
 * image pipeline, use {@link com.facebook.drawee.backends.pipeline.Fresco#initialize} to do this.
 */
public class SimpleDraweeView extends GenericDraweeView {

    private static Supplier<? extends SimpleDraweeControllerBuilder> sDraweeControllerBuilderSupplier;

    /** Initializes {@link com.facebook.drawee.view.SimpleDraweeView} with supplier of Drawee controller builders. */
    public static void initialize(
            Supplier<? extends SimpleDraweeControllerBuilder> draweeControllerBuilderSupplier) {
        sDraweeControllerBuilderSupplier = draweeControllerBuilderSupplier;
    }

    /** Shuts {@link com.facebook.drawee.view.SimpleDraweeView} down. */
    public static void shutDown() {
        sDraweeControllerBuilderSupplier = null;
    }

    private SimpleDraweeControllerBuilder mSimpleDraweeControllerBuilder;

    public SimpleDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init(context, null);
    }

    public SimpleDraweeView(Context context) {
        super(context);
        init(context, null);
    }

    public SimpleDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SimpleDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimpleDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        Preconditions.checkNotNull(
                sDraweeControllerBuilderSupplier,
                "SimpleDraweeView was not initialized!");
        mSimpleDraweeControllerBuilder = sDraweeControllerBuilderSupplier.get();

        if (attrs != null) {
            TypedArray gdhAttrs = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.SimpleDraweeView);
            try {
                if (gdhAttrs.hasValue(R.styleable.SimpleDraweeView_actualImageUri)) {
                    setImageURI(Uri.parse(gdhAttrs.getString(R.styleable.SimpleDraweeView_actualImageUri)), null);
                }
            } finally {
                gdhAttrs.recycle();
            }
        }
    }

    protected SimpleDraweeControllerBuilder getControllerBuilder() {
        return mSimpleDraweeControllerBuilder;
    }

    /**
     * Displays an image given by the uri.
     *
     * @param uri uri of the image
     * @undeprecate
     */
    @Override
    public void setImageURI(Uri uri) {
        setImageURI(uri, null);
    }

    /**
     * Displays an image given by the uri string.
     *
     * @param uriString uri string of the image
     */
    public void setImageURI(@Nullable String uriString) {
        setImageURI(uriString, null);
    }

    /**
     * Displays an image given by the uri.
     *
     * @param uri uri of the image
     * @param callerContext caller context
     */
    public void setImageURI(Uri uri, @Nullable Object callerContext) {
        DraweeController controller = mSimpleDraweeControllerBuilder
                .setCallerContext(callerContext)
                .setUri(uri)
                .setOldController(getController())
                .build();
        setController(controller);
    }

    /**
     * Displays an image given by the uri string.
     *
     * @param uriString uri string of the image
     * @param callerContext caller context
     */
    public void setImageURI(@Nullable String uriString, @Nullable Object callerContext) {
        Uri uri = (uriString != null) ? Uri.parse(uriString) : null;
        setImageURI(uri, callerContext);
    }
}

