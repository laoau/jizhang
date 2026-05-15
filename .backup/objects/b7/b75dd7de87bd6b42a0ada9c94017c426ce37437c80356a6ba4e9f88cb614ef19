package com.e.jizhnag;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 全部记录 - 查看所有记账记录
 *
 * 功能：
 * 1. 显示所有记录（全部时间，不限当月）
 * 2. 顶部统计总收入/总支出/总结余
 * 3. 点击记录可删除
 * 4. 顶部返回按钮回到主页
 */
public class AllRecordsActivity extends AppCompatActivity {

    private RecordManager recordManager;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_records);

        recordManager = RecordManager.getInstance(this);

        // 返回按钮
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        container = findViewById(R.id.all_records_container);

        // 刷新数据
        refreshUI();
    }

    private void refreshUI() {
        // 统计所有记录
        List<Record> all = recordManager.getRecords();
        double totalIncome = 0, totalExpense = 0;
        for (Record r : all) {
            if (Record.TYPE_INCOME.equals(r.type)) {
                totalIncome += r.amount;
            } else {
                totalExpense += r.amount;
            }
        }
        double totalBalance = totalIncome - totalExpense;

        TextView tvIncome = findViewById(R.id.tv_total_income);
        TextView tvExpense = findViewById(R.id.tv_total_expense);
        TextView tvBalance = findViewById(R.id.tv_total_balance);

        tvIncome.setText(formatMoney(totalIncome));
        tvExpense.setText(formatMoney(totalExpense));
        tvBalance.setText(formatMoney(totalBalance));

        // 刷新列表
        refreshRecords(all);
    }

    private void refreshRecords(List<Record> all) {
        container.removeAllViews();

        if (all.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("暂无记录");
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
            tv.setTextSize(13);
            tv.setPadding(0, 32, 0, 32);
            container.addView(tv);
            return;
        }

        // 按时间降序
        Collections.sort(all, (a, b) -> Long.compare(b.timestamp, a.timestamp));

        for (int i = 0; i < all.size(); i++) {
            container.addView(createRecordItem(all.get(i)));

            if (i < all.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.card_stroke));
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
                dlp.setMargins(dp(16), 0, dp(16), 0);
                divider.setLayoutParams(dlp);
                container.addView(divider);
            }
        }
    }

    private View createRecordItem(Record r) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16), dp(12), dp(16), dp(12));
        item.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        item.setForeground(getDrawable(android.R.drawable.list_selector_background));

        // emoji
        TextView icon = new TextView(this);
        icon.setTextSize(22);
        icon.setGravity(Gravity.CENTER);
        icon.setText(Record.getCategoryEmoji(r.category));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(44), dp(44));
        icon.setLayoutParams(iconLp);

        // 中间文字
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setPadding(dp(14), 0, 0, 0);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textLayout.setLayoutParams(textLp);

        TextView categoryTv = new TextView(this);
        categoryTv.setText(r.category != null ? r.category : "其他");
        categoryTv.setTextSize(15);
        categoryTv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        TextView dateTv = new TextView(this);
        dateTv.setText(r.date != null ? r.date : "");
        dateTv.setTextSize(11);
        dateTv.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        dateTv.setPadding(0, dp(3), 0, 0);

        textLayout.addView(categoryTv);
        textLayout.addView(dateTv);

        // 金额
        TextView amountTv = new TextView(this);
        boolean isIncome = Record.TYPE_INCOME.equals(r.type);
        amountTv.setText((isIncome ? "+" : "-") + formatMoneyRaw(r.amount));
        amountTv.setTextSize(15);
        amountTv.setTextColor(ContextCompat.getColor(this,
            isIncome ? R.color.income_green : R.color.expense_red));

        item.addView(icon);
        item.addView(textLayout);
        item.addView(amountTv);

        // 点击删除
        item.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("删除记录")
                .setMessage("确定删除这条 " + r.category + " " + formatMoneyRaw(r.amount) + " 的记录？")
                .setPositiveButton("删除", (d, w) -> {
                    recordManager.deleteRecord(r.id);
                    refreshUI();
                })
                .setNegativeButton("取消", null)
                .show();
        });

        return item;
    }

    private String formatMoney(double value) {
        return String.format(Locale.CHINA, "¥ %.2f", value);
    }

    private String formatMoneyRaw(double value) {
        if (value == (long) value) {
            return String.format(Locale.CHINA, "¥ %d", (long) value);
        }
        return String.format(Locale.CHINA, "¥ %.2f", value);
    }

    private int dp(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
