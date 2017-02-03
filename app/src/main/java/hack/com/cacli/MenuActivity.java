package hack.com.cacli;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
/**
 * Created by rain on 2017-02-03.
 */

public class MenuActivity extends Activity {
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final ImageButton btn_go = (ImageButton) findViewById(R.id.menu_Button);
         btn_go.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        //MainActivity로 가는 인텐트를 생성
                        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                        //액티비티 시작
                        startActivity(intent);
                    }
                }
        );
        btn_go.setOnTouchListener(new View.OnTouchListener() { //버튼 터치시 이벤트
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) // 버튼을 누르고 있을 때
                    btn_go.setBackgroundResource(R.drawable.go_but_1);
                if (event.getAction() == MotionEvent.ACTION_UP) { //버튼에서 손을 떼었을 때
                    btn_go.setBackgroundResource(R.drawable.go_but);
                }
                return false;
            }
        });
    }

}


