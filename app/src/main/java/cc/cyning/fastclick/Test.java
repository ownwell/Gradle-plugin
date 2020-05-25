package cc.cyning.fastclick;

import android.view.View;

public class Test {
    public  void test( View mView) {

        if (FastClick.isFastDoubleClick(mView, 100)) {
            System.out.println("hello world");
        }

    }
}
