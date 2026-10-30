package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * A one-off shower of emoji for a season (Seasons): each glyph drops in from above the top edge, sways a little,
 * turns slowly and fades out over the last third of its fall. Takes no focus, clicks or accessibility, and
 * calls {@code done} on the main thread when it has finished.
 */
final class EmojiShowerView extends View {
    private final Random rnd = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String[] glyphs = new String[0];
    private float[] xFrac, delay, life, size, swayAmp, swayPhase, spin;
    private int[] glyph;
    private float t;

    EmojiShowerView(Context context) {
        super(context);
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    void play(String[] what, int count, long durationMs, Runnable done) {
        glyphs = what;
        float density = getResources().getDisplayMetrics().density;
        xFrac = new float[count]; delay = new float[count]; life = new float[count]; size = new float[count];
        swayAmp = new float[count]; swayPhase = new float[count]; spin = new float[count]; glyph = new int[count];
        for (int i = 0; i < count; i++) {
            xFrac[i] = rnd.nextFloat();
            delay[i] = rnd.nextFloat() * 0.45f;
            life[i] = 0.45f + rnd.nextFloat() * 0.5f;
            if (delay[i] + life[i] > 1f) life[i] = 1f - delay[i];
            size[i] = (20 + rnd.nextFloat() * 22) * density;
            swayAmp[i] = (8 + rnd.nextFloat() * 24) * density;
            swayPhase[i] = rnd.nextFloat() * 6.2832f;
            spin[i] = (rnd.nextFloat() - 0.5f) * 120f;
            glyph[i] = rnd.nextInt(what.length);
        }
        setLayerType(LAYER_TYPE_HARDWARE, null);
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f).setDuration(durationMs);
        anim.setInterpolator(new LinearInterpolator());
        anim.addUpdateListener(a -> {
            t = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                setLayerType(LAYER_TYPE_NONE, null);
                if (done != null) done.run();
            }
        });
        anim.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (xFrac == null) return;
        int w = getWidth(), h = getHeight();
        for (int i = 0; i < xFrac.length; i++) {
            float p = (t - delay[i]) / life[i];
            if (p <= 0f || p >= 1f) continue;
            float y = -size[i] + (h + 2 * size[i]) * (p * (0.6f + 0.4f * p));   // gathers a little speed
            float x = xFrac[i] * w + swayAmp[i] * (float) Math.sin(swayPhase[i] + p * 6.2832f);
            paint.setTextSize(size[i]);
            paint.setAlpha(p > 0.66f ? Math.round(255 * (1f - p) / 0.34f) : 255);
            canvas.save();
            canvas.rotate(spin[i] * p, x, y);
            canvas.drawText(glyphs[glyph[i]], x, y, paint);
            canvas.restore();
        }
    }
}
