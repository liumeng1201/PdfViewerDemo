package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.lonelypluto.pdflibrary.R;
import com.lonelypluto.pdflibrary.constants.SPConsts;
import com.lonelypluto.pdflibrary.utils.SharedPreferencesUtil;

public class MuPDFReaderView extends ReaderView {
    private MuPDFReaderViewListener listener;

    public enum Mode {Viewing, Selecting, Drawing}

    private final Context mContext;
    private boolean mLinksEnabled = false;// 超链接是否高亮显示
    private boolean isLinkHighlightColor = false;// 是否设置了超链接颜色
    private Mode mMode = Mode.Viewing;
    private boolean tapDisabled = false;
    private int tapPageMargin;

    private int mLinkHighlightColor;// 超链接颜色

    protected void onTapMainDocArea() {
        checkMuPDFReaderViewListener();
        listener.onTapMainDocArea();
    }

    protected void onDocMotion() {
        checkMuPDFReaderViewListener();
        listener.onDocMotion();
    }

    protected void onHit(Hit item) {
        checkMuPDFReaderViewListener();
        listener.onHit(item);
    }

    /**
     * 设置超链接是否高亮显示
     *
     * @param b
     */
    public void setLinksEnabled(boolean b) {
        mLinksEnabled = b;
        resetupChildren();
    }

    /**
     * 设置超链接颜色
     *
     * @param color 颜色值
     */
    public void setLinkHighlightColor(int color) {
        isLinkHighlightColor = true;
        mLinkHighlightColor = color;
        resetupChildren();
    }

    /**
     * 设置搜索文字颜色
     *
     * @param color 颜色值
     */
    public void setSearchTextColor(int color) {
        SharedPreferencesUtil.put(SPConsts.SP_COLOR_SEARCH_TEXT, color);
        resetupChildren();
    }

    /**
     * 设置画笔颜色
     *
     * @param color 颜色值
     */
    public void setInkColor(int color) {
//		SharedPreferencesUtil.put(SPConsts.SP_COLOR_SEARCH_TEXT, color);
        ((MuPDFView) getCurrentView()).setInkColor(color);
    }

    /**
     * 设置画笔粗细
     *
     * @param inkThickness 粗细值
     */
    public void setPaintStrockWidth(float inkThickness) {
//		SharedPreferencesUtil.put(SPConsts.SP_COLOR_SEARCH_TEXT, color);
        ((MuPDFView) getCurrentView()).setPaintStrockWidth(inkThickness);
    }

    public float getCurrentScale() {
        return ((MuPDFView) getCurrentView()).getCurrentScale();
    }

    public void setMode(Mode m) {
        mMode = m;
    }

    private void setup() {
        // Get the screen size etc to customise tap margins.
        // We calculate the size of 1 inch of the screen for tapping.
        // On some devices the dpi values returned are wrong, so we
        // sanity check it: we first restrict it so that we are never
        // less than 100 pixels (the smallest Android device screen
        // dimension I've seen is 480 pixels or so). Then we check
        // to ensure we are never more than 1/5 of the screen width.
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        tapPageMargin = (int) dm.xdpi;
        if (tapPageMargin < 100)
            tapPageMargin = 100;
        if (tapPageMargin > dm.widthPixels / 5)
            tapPageMargin = dm.widthPixels / 5;
        // set view backgroundColor
        setBackgroundColor(mContext.getColor(R.color.muPDFReaderView_bg));
    }

    public MuPDFReaderView(Context context) {
        super(context);
        mContext = context;
        setup();
    }

    public MuPDFReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setup();
    }

    public boolean onSingleTapUp(MotionEvent e) {
        LinkInfo link = null;

        if (mMode == Mode.Viewing && !tapDisabled) {
            MuPDFView pageView = (MuPDFView) getDisplayedView();
            Hit item = pageView.passClickEvent(e.getX(), e.getY());
            onHit(item);
            if (item == Hit.Nothing) {
                if (mLinksEnabled && pageView != null
                        && (link = pageView.hitLink(e.getX(), e.getY())) != null) {
                    link.acceptVisitor(new LinkInfoVisitor() {
                        @Override
                        public void visitInternal(LinkInfoInternal li) {
                            // Clicked on an internal (GoTo) link
                            setDisplayedViewIndex(li.pageNumber);
                        }

                        @Override
                        public void visitExternal(LinkInfoExternal li) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                                    .parse(li.url));
                            mContext.startActivity(intent);
                        }

                        @Override
                        public void visitRemote(LinkInfoRemote li) {
                            // Clicked on a remote (GoToR) link
                        }
                    });
                } else if (e.getX() < tapPageMargin) {
                    super.smartMoveBackwards();
                } else if (e.getX() > super.getWidth() - tapPageMargin) {
                    super.smartMoveForwards();
                } else if (e.getY() < tapPageMargin) {
                    super.smartMoveBackwards();
                } else if (e.getY() > super.getHeight() - tapPageMargin) {
                    super.smartMoveForwards();
                } else {
                    onTapMainDocArea();
                }
            }
        }
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        return super.onDown(e);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        MuPDFView pageView = (MuPDFView) getDisplayedView();
        switch (mMode) {
            case Viewing:
                if (!tapDisabled)
                    onDocMotion();

                return super.onScroll(e1, e2, distanceX, distanceY);
            case Selecting:
                if (pageView != null)
                    pageView.selectText(e1.getX(), e1.getY(), e2.getX(), e2.getY());
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        switch (mMode) {
            case Viewing:
                return super.onFling(e1, e2, velocityX, velocityY);
            default:
                return true;
        }
    }

    public boolean onScaleBegin(ScaleGestureDetector d) {
        // Disabled showing the buttons until next touch.
        // Not sure why this is needed, but without it
        // pinch zoom can make the buttons appear
        tapDisabled = true;
        return super.onScaleBegin(d);
    }

    public boolean onTouchEvent(MotionEvent event) {

        if (mMode == Mode.Drawing) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    break;
            }
        }

        if ((event.getAction() & event.getActionMasked()) == MotionEvent.ACTION_DOWN) {
            tapDisabled = false;
        }

        return super.onTouchEvent(event);
    }

    private float mX, mY;

    private static final float TOUCH_TOLERANCE = 2;

    private void touch_start(float x, float y) {

        MuPDFView pageView = (MuPDFView) getDisplayedView();
        if (pageView != null) {
            pageView.startDraw(x, y);
        }
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            MuPDFView pageView = (MuPDFView) getDisplayedView();
            if (pageView != null) {
                pageView.continueDraw(x, y);
            }
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {

        // NOOP
    }

    protected void onChildSetup(int i, View v) {
        if (SearchTaskResult.get() != null
                && SearchTaskResult.get().pageNumber == i)
            ((MuPDFView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
        else
            ((MuPDFView) v).setSearchBoxes(null);

        ((MuPDFView) v).setLinkHighlighting(mLinksEnabled);

        // 设置超链接颜色
        if (isLinkHighlightColor) {
            ((MuPDFView) v).setLinkHighlightColor(mLinkHighlightColor);
        }
        ((MuPDFView) v).setChangeReporter(new Runnable() {
            public void run() {
                applyToChildren(new ViewMapper() {
                    @Override
                    public void applyToView(View view) {
                        ((MuPDFView) view).update();
                    }
                });
            }
        });
    }

    protected void onMoveToChild(int i) {
        if (SearchTaskResult.get() != null
                && SearchTaskResult.get().pageNumber != i) {
            SearchTaskResult.set(null);
            resetupChildren();
        }
        checkMuPDFReaderViewListener();
        listener.onMoveToChild(i);
    }

    @Override
    protected void onMoveOffChild(int i) {
        View v = getView(i);
        if (v != null)
            ((MuPDFView) v).deselectAnnotation();
    }

    protected void onSettle(View v) {
        // When the layout has settled ask the page to render
        // in HQ
        ((MuPDFView) v).updateHq(false);
    }

    protected void onUnsettle(View v) {
        // When something changes making the previous settled view
        // no longer appropriate, tell the page to remove HQ
        ((MuPDFView) v).removeHq();
    }

    @Override
    protected void onNotInUse(View v) {
        ((MuPDFView) v).releaseResources();
    }

    @Override
    protected void onScaleChild(View v, Float scale) {
        ((MuPDFView) v).setScale(scale);
    }

    /**
     * 设置监听事件
     *
     * @param listener
     */
    public void setListener(MuPDFReaderViewListener listener) {
        this.listener = listener;
    }

    private void checkMuPDFReaderViewListener() {
        if (listener == null) {
            listener = new MuPDFReaderViewListener() {
                @Override
                public void onMoveToChild(int i) {

                }

                @Override
                public void onTapMainDocArea() {

                }

                @Override
                public void onDocMotion() {

                }

                @Override
                public void onHit(Hit item) {

                }
            };
        }
    }
}
