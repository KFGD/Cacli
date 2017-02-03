package hack.com.cacli;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class StarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);
        TextView tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("즐겨찾기");
        tv_title.setTypeface(Typeface.createFromAsset(getAssets(), "YoonGothic760.ttf"));
    }
}
