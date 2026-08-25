package ru.trucker.sign;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** Рисование подписи пальцем с эффектом пера. */
public class DrawSignatureActivity extends Activity {

    private PadView pad;
    private int inkColor = 0xFF1A237E;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Нарисуйте подпись");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg());
        root.setPadding(Util.dp(this, 16), Util.dp(this, 16), Util.dp(this, 16), Util.dp(this, 16));

        TextView hint = new TextView(this);
        hint.setText("Нарисуйте подпись пальцем. Чем медленнее — тем жирнее штрих, как у настоящей ручки.");
        hint.setTextSize(13);
        hint.setTextColor(Ui.sub());
        root.addView(hint);

        pad = new PadView(this);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        plp.topMargin = Util.dp(this, 12);
        pad.setLayoutParams(plp);
        root.addView(pad);

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setPadding(0, Util.dp(this, 12), 0, 0);
        Button clearBtn = btn("Очистить", Ui.danger());
        Button undoBtn = btn("Отменить", Ui.neutral());
        Button saveBtn = btn("Сохранить", Ui.success());
        btns.addView(clearBtn, new LinearLayout.LayoutParams(0, Util.dp(this, 48), 1f));
        btns.addView(undoBtn, new LinearLayout.LayoutParams(0, Util.dp(this, 48), 1f));
        btns.addView(saveBtn, new LinearLayout.LayoutParams(0, Util.dp(this, 48), 1f));
        root.addView(btns);

        setContentView(root);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                pad.strokes.clear();
                pad.invalidate();
            }
        });
        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!pad.strokes.isEmpty()) {
                    pad.strokes.remove(pad.strokes.size() - 1);
                    pad.invalidate();
                }
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveSignature(); }
        });
    }

    private Button btn(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(Ui.buttonText());
        b.setBackground(Ui.round(this, color, 12));
        return b;
    }

    private void saveSignature() {
        if (pad.strokes.isEmpty()) {
            Toast.makeText(this, "Сначала нарисуйте подпись", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int w = Math.max(1, pad.getWidth());
            int h = Math.max(1, pad.getHeight());
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            pad.renderInk(canvas);
            SignatureStore.save(this, bmp);
            bmp.recycle();
            Toast.makeText(this, "Подпись сохранена", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------- поле рисования ----------

    private static class Stroke {
        final List<Float> xs = new ArrayList<>();
        final List<Float> ys = new ArrayList<>();
        final List<Long> ts = new ArrayList<>();
    }

    private class PadView extends View {

        final List<Stroke> strokes = new ArrayList<>();
        private Stroke cur;

        private final Paint ink = new Paint();
        private final Paint line = new Paint();
        private final Paint hintP = new Paint();

        private float baselineY = -1;
        private float density;

        PadView(Context ctx) {
            super(ctx);
            density = getResources().getDisplayMetrics().density;
            setBackground(Ui.round(ctx, 0xFFFFFFFF, 16));
            setElevation(Util.dp(ctx, 2));

            ink.setColor(inkColor);
            ink.setStyle(Paint.Style.STROKE);
            ink.setStrokeCap(Paint.Cap.ROUND);
            ink.setStrokeJoin(Paint.Join.ROUND);

            line.setColor(0xFFB0BEC5);
            line.setStrokeWidth(Util.dp(ctx, 1));

            hintP.setColor(0xFF90A4AE);
            hintP.setTextSize(Util.dp(ctx, 13));
            hintP.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            hintP.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            baselineY = h - Util.dp(getContext(), 26);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (baselineY > 0) {
                canvas.drawLine(Util.dp(getContext(), 20), baselineY,
                        getWidth() - Util.dp(getContext(), 20), baselineY, line);
                canvas.drawText("Подпишите здесь", getWidth() / 2f,
                        baselineY - Util.dp(getContext(), 8), hintP);
            }
            for (Stroke s : strokes) renderStroke(canvas, s);
        }

        void renderInk(Canvas canvas) {
            for (Stroke s : strokes) renderStroke(canvas, s);
        }

        private void renderStroke(Canvas canvas, Stroke s) {
            int n = s.xs.size();
            if (n == 0) return;
            if (n == 1) {
                ink.setStrokeWidth(2.5f * density);
                canvas.drawPoint(s.xs.get(0), s.ys.get(0), ink);
                return;
            }
            float minSeg = density * 0.4f;
            float w = 0f;
            boolean first = true;
            for (int i = 0; i < n - 1; i++) {
                float x1 = s.xs.get(i), y1 = s.ys.get(i);
                float x2 = s.xs.get(i + 1), y2 = s.ys.get(i + 1);
                float seg = (float) Math.hypot(x2 - x1, y2 - y1);
                if (seg < minSeg) continue;
                float newW = widthFor(s, i);
                w = first ? newW : (w * 0.6f + newW * 0.4f);
                first = false;
                ink.setStrokeWidth(w);
                canvas.drawLine(x1, y1, x2, y2, ink);
            }
        }

        private float widthFor(Stroke s, int i) {
            int j = Math.min(i + 1, s.xs.size() - 1);
            float dist = (float) Math.hypot(s.xs.get(j) - s.xs.get(i), s.ys.get(j) - s.ys.get(i));
            long dt = Math.max(s.ts.get(j) - s.ts.get(i), 1L);
            float speed = dist / (float) dt; // px/ms
            float baseW = 2.9f * density;
            float maxW = 4.2f * density;
            float minW = 1.4f * density;
            float speedRange = 2.0f;
            float k = speed >= speedRange ? 1f : (speed / speedRange);
            float w = baseW + (maxW - baseW) * (1f - k);
            if (w < minW) w = minW;
            return w;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    cur = new Stroke();
                    add(cur, ev.getX(), ev.getY());
                    strokes.add(cur);
                    invalidate();
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (cur != null) {
                        float x = ev.getX();
                        float y = ev.getY();
                        float lx = cur.xs.get(cur.xs.size() - 1);
                        float ly = cur.ys.get(cur.ys.size() - 1);
                        if (Math.hypot(x - lx, y - ly) > density) {
                            add(cur, x, y);
                            invalidate();
                        }
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    cur = null;
                    return true;
                }
            }
            return super.onTouchEvent(ev);
        }

        private void add(Stroke s, float x, float y) {
            s.xs.add(x);
            s.ys.add(y);
            s.ts.add(System.currentTimeMillis());
        }
    }
}
