package cc.cyning.learninggrale;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cc.cyning.libuitls.Cy;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
            }
        });
        test();
        Toast.makeText(this, "dd", 100).show();

    }


    @Cy(id = 2019, msg = "xiuqi")
    protected void test() {
        Test mTest = new Test();
    }
}
