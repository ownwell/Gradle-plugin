package cc.cyning.fixtoast;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import cc.cyning.fixtoast.instrument.CaughtCallback;
import cc.cyning.fixtoast.instrument.CaughtRunnable;

import static cc.cyning.fixtoast.instrument.Reflection.getFieldValue;
import static cc.cyning.fixtoast.instrument.Reflection.setFieldValue;


public class FixToast {
    /**
     * Fix {@code WindowManager$BadTokenException} for Android N
     *
     * @param toast The original toast
     */

    private static final String TAG = "FixToast";
    private static Toast toast;

    public static FixToast makeText(Context context, CharSequence text, int duration) {
        toast = Toast.makeText(context,  text, duration);
        return new FixToast();
    }

    public void show() {
        if (toast == null) {
            throw new RuntimeException("请先调用makeText方法");
        }
        if (Build.VERSION.SDK_INT == 25) {
            workaround(toast).show();
        } else {
            toast.show();
        }
    }


    private static Toast workaround(final Toast toast) {
        final Object tn = getFieldValue(toast, "mTN");
        if (null == tn) {
            return toast;
        }

        final Object handler = getFieldValue(tn, "mHandler");
        if (handler instanceof Handler) {
            if (setFieldValue(handler, "mCallback", new CaughtCallback((Handler) handler))) {
                return toast;
            }
        }

        final Object show = getFieldValue(tn, "mShow");
        if (show instanceof Runnable) {
            if (setFieldValue(tn, "mShow", new CaughtRunnable((Runnable) show))) {
                return toast;
            }
        }

        return toast;
    }

}
