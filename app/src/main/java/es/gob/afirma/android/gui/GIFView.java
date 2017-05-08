package es.gob.afirma.android.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.mifirma.android.R;

public final class GIFView extends View {

    final private Movie movie;
    private long movieStart;

    public GIFView(Context context) {
        super(context);
        movie = Movie.decodeStream(
            context.getResources().openRawResource(+ R.drawable.dni_nfc)
        );
    }

    public GIFView(Context context,
                   AttributeSet attrs,
                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        movie = Movie.decodeStream(
            context.getResources().openRawResource(+ R.drawable.dni_nfc)
        );
    }

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
        movie = Movie.decodeStream(
            context.getResources().openRawResource(+ R.drawable.dni_nfc)
        );
        Paint p = new Paint();
        p.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, p);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(movie != null){
            final int height = MeasureSpec.getSize(heightMeasureSpec);
            final double porc = 0.9;
            setMeasuredDimension(
                new Double(height * porc * movie.width() / movie.height() + 0.5d).intValue(),
                new Double(height * porc + 0.5d).intValue()
            );
        }
        else {
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        float scaleWidth = ((this.getWidth() / (1f*movie.width())));//add 1f does the trick
        float scaleHeight = ((this.getHeight() / (1f*movie.height())));
        canvas.scale(scaleWidth, scaleHeight);
        movie.draw(canvas, 0, 0);
        super.onDraw(canvas);

        long now=android.os.SystemClock.uptimeMillis();
        if (movieStart == 0) { // first time
            movieStart = now;

        }
        int relTime = (int)((now - movieStart) % movie.duration()) ;
        movie.setTime(relTime);

        invalidate();
    }

}