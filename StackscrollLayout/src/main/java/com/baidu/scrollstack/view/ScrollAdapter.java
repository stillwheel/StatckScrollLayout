package com.baidu.scrollstack.view;

import android.view.View;

/**
 * A scroll adapter which can be queried for meta information about the scroll state
 */
public interface ScrollAdapter {

    /**
     * @return Whether the view returned by {@link #getHostView()} is scrolled to the top
     */
    public boolean isScrolledToTop();

    /**
     * @return Whether the view returned by {@link #getHostView()} is scrolled to the bottom
     */
    public boolean isScrolledToBottom();

    /**
     * @return The view in which the scrolling is performed
     */
    public View getHostView();
}
