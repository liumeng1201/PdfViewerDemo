package com.artifex.mupdfdemo;

import android.graphics.PointF;
import android.graphics.RectF;

public interface MuPDFView {
    public void setPage(int page, PointF size);

    public void setScale(float scale);

    public int getPage();

    public void blank(int page);

    public Hit passClickEvent(float x, float y);

    public LinkInfo hitLink(float x, float y);

    public void selectText(float x0, float y0, float x1, float y1);

    public void deselectText();

    public boolean copySelection();

    /**
     * 文字选中后根据类型 高亮、下划线、删除线进行处理标注
     *
     * @param type
     * @return
     */
    public boolean markupSelection(Annotation.Type type);

    public void deleteSelectedAnnotation();

    public void setSearchBoxes(RectF searchBoxes[]);

    public void setLinkHighlighting(boolean f);

    public void deselectAnnotation();

    public void startDraw(float x, float y);

    public void continueDraw(float x, float y);

    public void cancelDraw();

    public boolean saveDraw();

    public void setChangeReporter(Runnable reporter);

    public void update();

    public void updateHq(boolean update);

    public void removeHq();

    public void releaseResources();

    public void releaseBitmaps();

    /**
     * 设置超链接颜色
     *
     * @param color 颜色值
     */
    public void setLinkHighlightColor(int color);

    /**
     * 设置画笔颜色
     *
     * @param color 颜色值
     */
    public void setInkColor(int color);

    /**
     * 设置画笔粗细
     *
     * @param inkThickness 粗细值
     */
    public void setPaintStrockWidth(float inkThickness);

    public float getCurrentScale();
}
