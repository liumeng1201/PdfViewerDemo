package com.artifex.mupdfdemo.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.support.v4.widget.ViewDragHelper;

public class VDHDeepLayout extends RelativeLayout {

    private ViewDragHelper mViewDragHelper;
    /**
     * 第一个View,可自由拖动的 view
     */
    private View mDragView;

    private Point mAutoBackOriginPos = new Point();

    public VDHDeepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         * 创建实例需要3个参数，第一个就是当前的ViewGroup，第二个sensitivity，主要用于设置touchSlop:
         */
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {

            /**
             * 如何返回ture则表示可以捕获该view，你可以根据传入的第一个view参数决定哪些可以捕获
             */
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                // mEdgeTrackerView禁止直接拖动
                return child == mDragView;
            }

            /**
             * 可以在该方法中对child移动的边界进行控制，left , top 分别为即将移动到的位置，
             * 比如横向的情况下，我希望只在ViewGroup的内部移动，
             * 即：最小>=paddingleft，最大<=ViewGroup.getWidth()-paddingright-child.getWidth。
             *   若直接返回left则会拖到边界外(针对于所有的View)。
             */
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingLeft();
                final int rightBound = getWidth() - mDragView.getWidth() - leftBound;
                final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                return newLeft;
            }

            /**
             * 优化了一下，对移动的Y边界进行控制
             */
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                int topBound = getPaddingTop();
                int bottomBound = getHeight() - mDragView.getHeight() - topBound;
                int newTop = Math.min(Math.max(top, topBound), bottomBound);
                return newTop;
            }

            /**
             * 手指释放时回调:
             *
             * 如果是mAutoBackView则调用settleCapturedViewAt回到初始的位置。
             * 大家可以看到紧随其后的代码是invalidate();
             * 因为其内部使用的是mScroller.startScroll，所以别忘了需要invalidate()以及结合computeScroll方法一起。
             */
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
            }

            /**
             * 在边界拖动时回调:
             *
             * 我们在onEdgeDragStarted回调方法中，主动通过captureChildView对其进行捕获，
             * 该方法可以绕过tryCaptureView，所以我们的tryCaptureView虽然并为返回true，但却不影响。
             * 注意如果需要使用边界检测需要添加:  ViewDragHelper.EDGE_LEFT
             * */
            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                super.onEdgeDragStarted(edgeFlags, pointerId);
            }

            // clickable=true，意思就是子View可以消耗事件
            @Override
            public int getViewHorizontalDragRange(View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }
        });
        // 注意如果需要使用边界检测需要添加  ViewDragHelper.EDGE_LEFT
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
//        super.computeScroll();
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mAutoBackOriginPos.x = mDragView.getLeft();
        mAutoBackOriginPos.y = mDragView.getTop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView = getChildAt(0);
    }
}
