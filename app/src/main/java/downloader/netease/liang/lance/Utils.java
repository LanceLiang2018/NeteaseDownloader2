package downloader.netease.liang.lance;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.content.pm.*;

public class Utils
{
	/**
     * 将bitmap中的某种颜色值替换成新的颜色
     * @param bitmap
     * @param oldColor
     * @param newColor
     * @return
     */
    public static Bitmap replaceBitmapColor(Bitmap oldBitmap,int oldColor,int newColor)
    {
        //相关说明可参考 http://xys289187120.blog.51cto.com/3361352/657590/
        Bitmap mBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //循环获得bitmap所有像素点
        int mBitmapWidth = mBitmap.getWidth();          
        int mBitmapHeight = mBitmap.getHeight();           
        int mArrayColorLengh = mBitmapWidth * mBitmapHeight;          
        int[] mArrayColor = new int[mArrayColorLengh];          
        int count = 0;          
        for (int i = 0; i < mBitmapHeight; i++) {          
            for (int j = 0; j < mBitmapWidth; j++) {              
                //获得Bitmap 图片中每一个点的color颜色值
                //将需要填充的颜色值如果不是              
                //在这说明一下 如果color 是全透明 或者全黑 返回值为 0              
                //getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000               
                //而不透明黑色是0xFF000000 如果不计算透明部分就都是0了       
                int color = mBitmap.getPixel(j, i);
                //将颜色值存在一个数组中 方便后面修改             
                if (color == oldColor) {                  
                    mBitmap.setPixel(j, i, newColor);  //将白色替换成透明色            
				}                      

            }  
        }
        return mBitmap;
    }
	public static int getPrimaryColor(Context context) {
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
		int color = typedValue.data;
		return color;
	}

	public static int getAccentColor(Context context) {
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
		int color = typedValue.data;
		return color;
	}
	
	public static ContentValues ContentPut(String key, String dat) {
		ContentValues val = new ContentValues();
		val.put(key, dat);
		return val;
	}
	public static ContentValues ContentPut(ContentValues val, String key, String dat) {
		val.put(key, dat);
		return val;
	}
	
//	public static void plusPrintCount() {
//		Config config = Config.get(MyApplication.getMyApplication().getApplicationContext());
//		if (new MyGetTime().date().equals(config.data.settings.lastPrintDate)) {
//			config.data.settings.count_today++;
//		} else {
//			config.data.settings.count_today = 0;
//		}
//		config.data.settings.count_total++;
//		config.data.settings.lastPrintDate = new MyGetTime().date();
//		config.save();
//	}
	
	static public void setMargins_match(View v, int left, int top, int right, int bottom) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
		if (params == null)
			params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
												   ViewGroup.LayoutParams.WRAP_CONTENT);
//		int oldLeft = params.leftMargin;
//		int oldTop = params.topMargin;
//		int oldRight = params.rightMargin;
//		int oldBottom = params.bottomMargin;
//		params.setMargins(oldLeft + left, oldTop + top, oldRight + right, oldBottom + bottom);
		params.setMargins(left, top, right, bottom);
		v.setLayoutParams(params);
	}
	
	public static int max(int a, int b) {
		return a > b ? a : b;
	}
	
	public static String getVerName(Context context) {
		String name = "";
		try {
			name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {}
		return name;
	}
}
class Content {
	public ContentValues val;
	Content() {
		val = new ContentValues();
	}
	public Content put(String key, String data) {
		val.put(key, data);
		return this;
	}
}

