package ru.trucker.sign;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** Главный экран: подписание, подпись, недавние документы. */
public class MainActivity extends Activity {

    private static final int REQ_IMPORT = 1;

    private LinearLayout recentBox;
    private ImageView sigPreview;
    private TextView sigStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())
                && getIntent().getData() != null) {
            openSigner(getIntent().getData());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderRecent();
        updateSignatureCard();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_IMPORT && res == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri uri = data.getData();
                Bitmap bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                if (bmp == null) throw new Exception();
                SignatureStore.save(this, bmp);
                bmp.recycle();
                Toast.makeText(this, "Подпись импортирована", Toast.LENGTH_SHORT).show();
                updateSignatureCard();
            } catch (Exception e) {
                Toast.makeText(this, "Не удалось импортировать подпись", Toast.LENGTH_LONG).show();
            }
        }
    }

    private View buildUi() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.bg());
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Util.dp(this, 16), 0, Util.dp(this, 16), Util.dp(this, 24));
        sv.addView(root);

        // Шапка
        TextView title = new TextView(this);
        title.setText("✍ Подпись PDF");
        title.setTextSize(26);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(Util.dp(this, 16), 0, 0, 0);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackground(Ui.gradient(this, Ui.headerStart(), Ui.headerEnd(), 20));
        header.setPadding(0, Util.dp(this, 22), 0, Util.dp(this, 22));
        header.addView(title);
        TextView sub = new TextView(this);
        sub.setText("Акты, реестры и другие документы — подпись в один тап");
        sub.setTextSize(13);
        sub.setTextColor(0xFFC5CAE9);
        sub.setPadding(Util.dp(this, 16), Util.dp(this, 4), Util.dp(this, 16), 0);
        header.addView(sub);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = Util.dp(this, 12);
        header.setLayoutParams(hlp);
        root.addView(header);

        // Большая кнопка «Подписать документ»
        Button big = new Button(this);
        big.setText("📄  Подписать документ");
        big.setTextSize(18);
        big.setAllCaps(false);
        big.setTextColor(Ui.buttonText());
        big.setBackground(Ui.gradient(this, Ui.accent(), Ui.accentDark(), 16));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Util.dp(this, 64));
        blp.topMargin = Util.dp(this, 16);
        big.setLayoutParams(blp);
        big.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignActivity.class));
            }
        });
        root.addView(big);

        // Карточка подписи
        LinearLayout sigCard = new LinearLayout(this);
        sigCard.setOrientation(LinearLayout.VERTICAL);
        sigCard.setBackground(Ui.round(this, Ui.card(), 16));
        sigCard.setPadding(Util.dp(this, 16), Util.dp(this, 16), Util.dp(this, 16), Util.dp(this, 16));
        sigCard.setElevation(Util.dp(this, 2));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.topMargin = Util.dp(this, 14);
        sigCard.setLayoutParams(clp);

        TextView sigTitle = new TextView(this);
        sigTitle.setText("Моя подпись");
        sigTitle.setTextSize(16);
        sigTitle.setTextColor(Ui.title());
        sigTitle.setTypeface(sigTitle.getTypeface(), android.graphics.Typeface.BOLD);
        sigCard.addView(sigTitle);

        sigPreview = new ImageView(this);
        sigPreview.setBackgroundColor(0xFFE8EDF5);
        LinearLayout.LayoutParams iplp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Util.dp(this, 90));
        iplp.topMargin = Util.dp(this, 10);
        sigPreview.setLayoutParams(iplp);
        sigPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        sigCard.addView(sigPreview);

        sigStatus = new TextView(this);
        sigStatus.setTextSize(13);
        sigStatus.setTextColor(Ui.sub());
        sigStatus.setPadding(0, Util.dp(this, 8), 0, 0);
        sigCard.addView(sigStatus);

        LinearLayout sigBtns = new LinearLayout(this);
        sigBtns.setOrientation(LinearLayout.HORIZONTAL);
        sigBtns.setPadding(0, Util.dp(this, 10), 0, 0);
        Button loadBtn = smallBtn("Загрузить", Ui.accent());
        Button drawBtn = smallBtn("✍ Нарисовать", Ui.neutral());
        sigBtns.addView(loadBtn, new LinearLayout.LayoutParams(0, Util.dp(this, 44), 1f));
        sigBtns.addView(drawBtn, new LinearLayout.LayoutParams(0, Util.dp(this, 44), 1f));
        sigCard.addView(sigBtns);

        LinearLayout sigBtns2 = new LinearLayout(this);
        sigBtns2.setOrientation(LinearLayout.HORIZONTAL);
        sigBtns2.setPadding(0, Util.dp(this, 8), 0, 0);
        Button expBtn = smallBtn("Экспорт", Ui.neutral());
        Button impBtn = smallBtn("Импорт", Ui.neutral());
        sigBtns2.addView(expBtn, new LinearLayout.LayoutParams(0, Util.dp(this, 44), 1f));
        sigBtns2.addView(impBtn, new LinearLayout.LayoutParams(0, Util.dp(this, 44), 1f));
        sigCard.addView(sigBtns2);

        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignatureActivity.class));
            }
        });
        drawBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DrawSignatureActivity.class));
            }
        });
        expBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { SignatureActivity.export(MainActivity.this); }
        });
        impBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { pickImport(); }
        });
        root.addView(sigCard);

        // Недавние
        TextView recentTitle = new TextView(this);
        recentTitle.setText("Недавние документы");
        recentTitle.setTextSize(16);
        recentTitle.setTextColor(Ui.title());
        recentTitle.setTypeface(recentTitle.getTypeface(), android.graphics.Typeface.BOLD);
        recentTitle.setPadding(0, Util.dp(this, 18), 0, Util.dp(this, 6));
        root.addView(recentTitle);

        recentBox = new LinearLayout(this);
        recentBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(recentBox);

        TextView footer = new TextView(this);
        footer.setText("Разработчик: adreno97\nadreno97@mail.ru");
        footer.setTextSize(12);
        footer.setTextColor(Ui.sub());
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, Util.dp(this, 18), 0, Util.dp(this, 4));
        root.addView(footer);

        return sv;
    }

    private Button smallBtn(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTextColor(Ui.buttonText());
        b.setBackground(Ui.round(this, color, 10));
        b.setPadding(Util.dp(this, 4), 0, Util.dp(this, 4), 0);
        return b;
    }

    private void pickImport() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, REQ_IMPORT);
    }

    private void updateSignatureCard() {
        Bitmap sig = SignatureStore.load(this);
        if (sig != null) {
            sigPreview.setImageBitmap(sig);
            sigStatus.setText("Подпись загружена. Экспортируйте, чтобы перенести на другой телефон.");
        } else {
            sigPreview.setImageBitmap(null);
            sigStatus.setText("Подпись не загружена. Сфотографируйте свою подпись на белом фоне.");
        }
    }

    private void renderRecent() {
        recentBox.removeAllViews();
        List<RecentDocs.Item> list = RecentDocs.list(this);
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Пока пусто. Откройте PDF и подпишите — документ появится здесь.");
            empty.setTextSize(13);
            empty.setTextColor(Ui.sub());
            empty.setPadding(0, Util.dp(this, 6), 0, 0);
            recentBox.addView(empty);
            return;
        }
        for (final RecentDocs.Item it : list) {
            Button row = new Button(this);
            row.setBackground(Ui.round(this, Ui.card(), 14));
            row.setAllCaps(false);
            row.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            row.setTextColor(Ui.primary());
            row.setPadding(Util.dp(this, 16), Util.dp(this, 10), Util.dp(this, 16), Util.dp(this, 10));
            row.setText("📄 " + it.name + "\n" + Util.date(it.ts));
            row.setTextSize(14);
            row.setLineSpacing(0f, 1.1f);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.bottomMargin = Util.dp(this, 8);
            row.setLayoutParams(p);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    try {
                        openSigner(Uri.parse(it.uri));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Файл недоступен", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    RecentDocs.remove(MainActivity.this, Uri.parse(it.uri));
                    renderRecent();
                    return true;
                }
            });
            recentBox.addView(row);
        }
    }

    private void openSigner(Uri uri) {
        Intent i = new Intent(MainActivity.this, SignActivity.class);
        i.putExtra("uri", uri.toString());
        startActivity(i);
    }
}
