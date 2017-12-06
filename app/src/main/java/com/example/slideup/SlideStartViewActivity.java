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
import android.widget.Toast;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

public class SlideStartViewActivity extends AppCompatActivity {
    private SlideUp slideUp;
    private View dim;
    private View sliderView;
    private FloatingActionButton fab;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_start_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sliderView = findViewById(R.id.slideView);
        sliderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(SlideStartViewActivity.this, "click", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        dim = findViewById(R.id.dim);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        slideUp = new SlideUpBuilder(sliderView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(SlideUp slideUp, float percent) {
                        dim.setAlpha(1 - (percent / 100));
                    }

                    @Override
                    public void onShown(SlideUp slideUp) {

                    }

                    @Override
                    public void onHidden(SlideUp slideUp) {
                        fab.show();
                    }
                })
                .withStartGravity(Gravity.START)
                .withLoggingEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .build();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.show();
                fab.hide();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_slide_start_view, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_slide_up:
                startActivity(new Intent(this, SlideUpViewActivity.class));
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
