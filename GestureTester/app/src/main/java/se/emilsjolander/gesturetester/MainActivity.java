package se.emilsjolander.gesturetester;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

public class MainActivity extends Activity implements HeadGestureDetector.Listener {

    private TextView text;
    private HeadGestureDetector detector;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        detector = new HeadGestureDetector(this);
        detector.setListener(this);

        setContentView(R.layout.main);
        text = (TextView) findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        detector.start();
    }

    @Override
    protected void onPause() {
        detector.stop();
        super.onPause();
    }

    @Override
    public void onGestureDetected(HeadGestureDetector.Gesture gesture) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.MENU);
        switch (gesture) {
            case YES:
                card.setText("YES");
                break;
            case NO:
                card.setText("NO");
                break;
            case NONE:
                card.setText("");
                break;
        }
        setContentView(card.getView());
    }

}
