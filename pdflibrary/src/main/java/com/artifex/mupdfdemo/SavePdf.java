package com.artifex.mupdfdemo;

import android.graphics.Bitmap;
import android.util.Log;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;


/**
 * Created by Jammy on 2016/6/23.
 */
public class SavePdf {

    private float defaultScale = 0.90756303f;
    public void setWidthScale(float widthScale) {
        this.widthScale = widthScale;
    }

    public void setHeightScale(float heightScale) {
        this.heightScale = heightScale;
    }

    float widthScale;
    float heightScale;
    String inPath;/////当前的PDF地址
    String outPath;////要输出的PDF地址
    private int pageNum;/////签名所在的页码
    private Bitmap bitmap;//////签名图像
    private float scale;
    private float density;  ///手机屏幕的分辨率密度

     private float width;
     private float height;

    /**
     * 设置放大比例
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * 设置宽高
     * @param
     */
    public void setWH(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 设置分辨率密度
     *
     * @param density
     */
    public void setDensity(float density) {
        this.density = density;
    }

    /**
     * 设置嵌入的图片
     *
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * 设置需要嵌入的页面
     *
     * @param pageNum
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public SavePdf(String inPath, String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    /**
     * 将图片加入PDF并保存
     */
    public void addText() {
        try {
            PdfReader reader = new PdfReader(inPath, "PDF".getBytes());///打开要写入的PDF
            FileOutputStream outputStream = new FileOutputStream(outPath);//设置涂鸦后的PDF
            PdfStamper stamp;
            stamp = new PdfStamper(reader, outputStream);
            PdfContentByte over = stamp.getOverContent(pageNum);//////用于设置在第几页打印签名
            byte[] bytes = Bitmap2Bytes(bitmap);
            Image img = Image.getInstance(bytes);//将要放到PDF的图片传过来，要设置为byte[]类型
            com.lowagie.text.Rectangle rectangle = reader.getPageSize(pageNum);
            img.setAlignment(Image.MIDDLE);// 图像在文档中的对齐方式

            //这里是重点！！！！！设置Image图片大小，需要根据屏幕的分辨率，签名时PDF的放大比例来计算；还有就是当PDF开始显示的时候，他已经做了一次缩放，可以用 rectangle.getWidth() / (bitmap.getWidth() / 2)求得那个放大比
//            img.scaleAbsolute(363 * 1.0f * density / 2 / scale * rectangle.getWidth() / (bitmap.getWidth() / 2), 557 * 1.0f * density / 2 / scale * rectangle.getWidth() / (bitmap.getWidth() / 2));
//            img.scaleAbsolute(594 * 1.0f * density / 2 / scale * rectangle.getWidth() / (bitmap.getWidth() / 2), 557 * 1.0f * density / 2 / scale * rectangle.getWidth() / (bitmap.getWidth() / 2));
//            img.scaleAbsolute(602 * 1.0f * density / 2 / scale * rectangle.getWidth() / (bitmap.getWidth() / 2), 870 * 1.0f * density / 2 / scale * rectangle.getWidth() / (bitmap.getWidth() / 2));
            //这里设置image相对PDF左下角的偏移量，我的做法是得到放大后位置相对于整个PDF的百分比再乘PDF的大小得到他的相对偏移位置
//            img.setAbsolutePosition(rectangle.getWidth() * widthScale, rectangle.getHeight() * heightScale);// 偏右上

            Log.e("zyw", "position = " + rectangle.getWidth() * widthScale + "  " + rectangle.getHeight() * heightScale);
            Log.e("zyw", "density = " + density);
            Log.e("zyw", "img.getWidth() = " + img.getWidth() + "  img.getHeight() = " + img.getHeight());
            Log.e("zyw", "scale = " + scale);
            Log.e("zyw", "widthScale = " + widthScale + "  heightScale = " + heightScale);
            Log.e("zyw", "bitmap.w = " + bitmap.getWidth() + "  bitmap.h = " + bitmap.getHeight());
            Log.e("zyw", "rectangle.getLeft = " + rectangle.getLeft() + "  rectangle.getBottom() = " + rectangle.getBottom());
            Log.e("zyw", "rectangle.getWidth = " + rectangle.getWidth() + "  rectangle.getHeight = " + rectangle.getHeight());


            Log.e("zyw", "比例1 = " +  ((float)rectangle.getWidth()/img.getWidth())*100);
            Log.e("zyw", "比例2 = " +  rectangle.getWidth() * widthScale*100);
            Log.e("zyw", "坐标AbsolutePosition = " +  width * (rectangle.getWidth() * widthScale) + " " + (1964-height-img.getHeight()) * (rectangle.getWidth() * widthScale));
            Log.e("zyw", "差值 = " + rectangle.getHeight() * (heightScale-widthScale));
            //            img.scalePercent(((float)594/1080)*100);
            Log.e("zyw", "缩放比例 = " + scale / defaultScale);
            img.scalePercent(rectangle.getWidth() * widthScale*100);
//            img.setAbsolutePosition(rectangle.getLeft(), rectangle.getBottom() - 120);
            img.setAbsolutePosition(width * (rectangle.getWidth() * widthScale) * (scale ), rectangle.getHeight() - ((height) * (rectangle.getWidth() * widthScale) * (scale / defaultScale)) + img.getHeight()/2*widthScale*100);
//            img.setAbsolutePosition(rectangle.getLeft() + percent/2, rectangle.getBottom() - percent/2);// 偏左下
            over.addImage(img);
            stamp.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将BitMap转换为Bytes
     *
     * @param bm
     * @return
     */
    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
