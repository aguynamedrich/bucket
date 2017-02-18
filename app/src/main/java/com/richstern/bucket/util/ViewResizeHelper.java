package com.richstern.bucket.util;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;

import java.util.List;

public class ViewResizeHelper {

    /**
     * Represents the desired aspect ratio of a View before resizing
     *
     * @author rich
     */
    public enum AspectRatio {
        DEFAULT(1, 1),
        SQUARE(1, 1),
        WIDESCREEN_5x2(5, 2),
        WIDESCREEN_16x9(16, 9),
        WIDESCREEN_4x3(4, 3);

        private int width, height;

        private AspectRatio(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    /**
     * Refers to which dimension of a view should be the reference
     * that the other dimension is resized against (the unchanged dimension)
     *
     * @author rich
     */
    public enum ResizeAnchor {
        WIDTH,
        HEIGHT
    }

    /**
     * Shortcut for resizing multiple views at once.  This should cut down on choppiness when
     * we have to wait for the ViewTreeObserver to call onPreDraw multiple times
     *
     * @param views
     * @param aspectRatio
     * @param resizeAnchor
     */
    public static void resize(final List<View> views, final AspectRatio aspectRatio, final ResizeAnchor resizeAnchor) {
        if (views == null || views.size() == 0) {
            throw new IllegalArgumentException("Cannot resize an empty list");
        }
        boolean canResize = false;
        // If any view has been measured, we can assume that views are ready to call doResize synchronously
        for (View view : views) {
            if (view == null) {
                throw new IllegalArgumentException("Cannot resize null views");
            }
            int dimension = resizeAnchor == ResizeAnchor.WIDTH ? view.getWidth() : view.getHeight();
            if (dimension > 0) {
                canResize = true;
                break;
            }
        }
        if (canResize) {
            for (View view : views) {
                doResize(view, aspectRatio, resizeAnchor, null);
            }
        } else {
            ViewTreeObserver vto = views.get(0).getViewTreeObserver();
            vto.addOnPreDrawListener(new OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    for (View view : views) {
                        doResize(view, aspectRatio, resizeAnchor, null);
                    }
                    views.get(0).getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    /**
     * Resize a single view by the given AspectRatio and ResizeAnchor
     * If the view has not yet been measured, the actual logic is deferred to onPreDraw
     *
     * @param view
     * @param aspectRatio
     * @param resizeAnchor
     */
    public static void resize(View view, AspectRatio aspectRatio, ResizeAnchor resizeAnchor) {
        resize(view, aspectRatio, resizeAnchor, null);
    }

    /**
     * Resize a single view by the given AspectRatio and ResizeAnchor
     * If the view has not yet been measured, the actual logic is deferred to onPreDraw
     *
     * @param view
     * @param aspectRatio
     * @param resizeAnchor
     * @param callback
     */
    public static void resize(final View view, final AspectRatio aspectRatio,
                              final ResizeAnchor resizeAnchor, final ViewResizeCallback callback) {
        if (view == null)
            return;

        if (!doResize(view, aspectRatio, resizeAnchor, callback)) {
            ViewTreeObserver obs = view.getViewTreeObserver();
            obs.addOnPreDrawListener(new OnPreDrawListener() {

                public boolean onPreDraw() {
                    if (!doResize(view, aspectRatio, resizeAnchor, callback)) {
                        if (callback != null) {
                            callback.onError(view);
                        }
                    }
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    /**
     * This method handles the actual resizing logic.  If the views are already measured for display, this is called synchronously.
     * Otherwise, the calling method will delay this call until the ViewTreeObserver calls onPreDraw
     *
     * @param view
     * @param aspectRatio
     * @param resizeAnchor
     * @param callback
     * @return
     */
    protected static boolean doResize(View view, AspectRatio aspectRatio,
                                      ResizeAnchor resizeAnchor, ViewResizeCallback callback) {
        boolean didResize = false;
        if (view != null && aspectRatio != AspectRatio.DEFAULT
            && view.getWidth() > 0) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (resizeAnchor == ResizeAnchor.WIDTH) {
                int height = view.getWidth() * aspectRatio.getHeight()
                    / aspectRatio.getWidth();
                lp.height = height;
            } else {
                int width = view.getHeight() * aspectRatio.getWidth()
                    / aspectRatio.getHeight();
                lp.width = width;
            }
            view.setLayoutParams(lp);
            didResize = true;
        }
        if (didResize && callback != null) {
            callback.onSuccess(view);
        }
        return didResize;
    }

    public interface ViewResizeCallback {
        public void onSuccess(View view);

        public void onError(View view);
    }

}
