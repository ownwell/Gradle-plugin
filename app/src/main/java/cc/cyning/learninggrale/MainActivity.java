package cc.cyning.learninggrale;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cc.cyning.libuitls.Cy;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test();

    }


    @Cy(id = 2019, msg = "xiuqi")
    protected void test() {
        Test mTest = new Test();
    }
}
