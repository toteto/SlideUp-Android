package com.example.slideup;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

public class SlideUpViewActivity extends AppCompatActivity {
    private SlideUp slideUp;
    private View dim;
    private View sliderView;
    private TextView textView;
    private FloatingActionButton fab;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_up_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sliderView = findViewById(R.id.slideView);
        sliderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(SlideUpViewActivity.this, "click", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        dim = findViewById(R.id.dim);
        fab = findViewById(R.id.fab);
        textView = findViewById(R.id.textView);

        slideUp = new SlideUpBuilder(sliderView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(SlideUp slideUp, float percent) {
                        dim.setAlpha(1 - (percent / 100));
                        if (fab.isShown() && percent < 100) {
                            fab.hide();
                        }

                    }

                    @Override
                    public void onShown(SlideUp slideUp) {
                        textView.setText(R.string.slide_it_down);
                    }

                    @Override
                    public void onHidden(SlideUp slideUp) {
                        fab.show();
                        textView.setText(R.string.slide_it_up);
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(true)
                .withGesturesEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .withSlideFromOtherView(findViewById(R.id.rootView))
                .withPullTabView(textView)
                .build();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_slide_up_view, menu);
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_slide_start:
                startActivity(new Intent(this, SlideStartViewActivity.class));
                break;
            case R.id.action_slide_end:
                startActivity(new Intent(this, SlideEndViewActivity.class));
                break;
            case R.id.action_slide_down:
                startActivity(new Intent(this, SlideDownViewActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
