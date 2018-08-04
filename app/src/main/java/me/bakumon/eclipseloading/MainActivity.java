package me.bakumon.eclipseloading;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import me.bakumon.library.EclipseLoadingView;

/**
 * @author Bakumon https://bakumon.me
 * @date 2018/01/03
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void click(View view) {
        ((EclipseLoadingView)view).setSunColor(getResources().getColor(R.color.colorAccent));
    }
}
