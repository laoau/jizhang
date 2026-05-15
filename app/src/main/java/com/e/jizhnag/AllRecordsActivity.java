package com.e.jizhnag;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
        // 启用返回过渡动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(null);
        }

        setContentView(R.layout.activity_all_records);

        recordManager = RecordManager.getInstance(this);

        // 返回按钮
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        container = findViewById(R.id.all_records_container);

        // 刷新数据
        refreshUI();
    }

    @Override
    public void finish() {
        super.finish();
        // 返回动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }

    private void refreshUI() {
        // 统计所有记录
        List<Record> all = recordManager.getRecords();
        double totalIncome = 0, totalExpense = 0;
        for (Record r : all) {
            if (Record.TYPE_INCOME.equals(r.getType())) {
                totalIncome += r.getAmount();
            } else {
                totalExpense += r.getAmount();
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

    // ==========================================
    // 📅 日期辅助方法
    // ==========================================

    /**
     * 创建日期标题头 View（按天分组时使用）
     * @param dateStr     日期字符串 yyyy-MM-dd
     * @param dayIncome   当天总收入
     * @param dayExpense  当天总支出
     */
    private View createDateHeader(String dateStr, double dayIncome, double dayExpense) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(14), dp(16), dp(6));
        header.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // 小圆点装饰
        View dot = new View(this);
        dot.setBackgroundResource(R.drawable.circle_green);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(6), dp(6));
        dot.setLayoutParams(dotLp);

        // 日期文字（使用 RecordManager 的统一工具方法）
        TextView tvDate = new TextView(this);
        tvDate.setText(RecordManager.getDateDisplayName(dateStr));
        tvDate.setTextSize(13);
        tvDate.setTypeface(null, Typeface.BOLD);
        tvDate.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvDate.setPadding(dp(8), 0, 0, 0);

        // 小计金额（通过参数传入，无需再遍历）
        TextView tvSubtotal = new TextView(this);
        if (dayExpense > 0 || dayIncome > 0) {
            StringBuilder sb = new StringBuilder();
            if (dayIncome > 0) sb.append("+").append(formatMoneyRaw(dayIncome)).append("  ");
            if (dayExpense > 0) sb.append("-").append(formatMoneyRaw(dayExpense));
            tvSubtotal.setText(sb.toString());
        }
        tvSubtotal.setTextSize(11);
        tvSubtotal.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        tvSubtotal.setGravity(Gravity.END);

        LinearLayout.LayoutParams subtotalLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvSubtotal.setLayoutParams(subtotalLp);

        header.addView(dot);
        header.addView(tvDate);
        header.addView(tvSubtotal);

        return header;
    }

    private void refreshRecords(List<Record> all) {
        container.removeAllViews();

        if (all.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("暂无记录 🤔");
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
            tv.setTextSize(13);
            tv.setPadding(0, 48, 0, 48);
            container.addView(tv);
            return;
        }

        // 按时间降序
        Collections.sort(all, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        // ====== 按日期分组（保留插入顺序） ======
        LinkedHashMap<String, List<Record>> grouped = new LinkedHashMap<>();
        for (Record r : all) {
            String d = (r.getDate() != null) ? r.getDate() : "未知日期";
            if (!grouped.containsKey(d)) {
                grouped.put(d, new ArrayList<Record>());
            }
            grouped.get(d).add(r);
        }

        // ⚡ 一次遍历：预计算每日汇总（O(N)），避免 createDateHeader 中重复遍历（原来是 O(N×D)）
        Map<String, double[]> dailyTotals = new HashMap<>(); // [0]=income, [1]=expense
        for (Record r : all) {
            String d = (r.getDate() != null) ? r.getDate() : "未知日期";
            double[] t = dailyTotals.get(d);
            if (t == null) {
                t = new double[2];
                dailyTotals.put(d, t);
            }
            if (Record.TYPE_INCOME.equals(r.getType())) t[0] += r.getAmount();
            else t[1] += r.getAmount();
        }

        int animDelay = 0;
        boolean isFirstGroup = true;

        for (Map.Entry<String, List<Record>> entry : grouped.entrySet()) {
            // ----- 组间分割线（除了第一组） -----
            if (!isFirstGroup) {
                View groupDivider = new View(this);
                groupDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.card_stroke));
                LinearLayout.LayoutParams gdlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
                groupDivider.setLayoutParams(gdlp);
                container.addView(groupDivider);

                AlphaAnimation ga = new AlphaAnimation(0f, 1f);
                ga.setDuration(200);
                ga.setStartOffset(animDelay);
                groupDivider.startAnimation(ga);
            }
            isFirstGroup = false;

            // ----- 日期标题头（从预计算的 dailyTotals 取值，无需遍历） -----
            String dateKey = entry.getKey();
            double[] totals = dailyTotals.get(dateKey);
            double dayIncome = totals != null ? totals[0] : 0;
            double dayExpense = totals != null ? totals[1] : 0;
            View header = createDateHeader(dateKey, dayIncome, dayExpense);
            container.addView(header);

            AnimationSet headerAnim = new AnimationSet(true);
            headerAnim.setInterpolator(new DecelerateInterpolator());
            headerAnim.setStartOffset(animDelay);
            AlphaAnimation ha = new AlphaAnimation(0f, 1f);
            ha.setDuration(300);
            TranslateAnimation ht = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.15f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f);
            ht.setDuration(300);
            headerAnim.addAnimation(ha);
            headerAnim.addAnimation(ht);
            header.startAnimation(headerAnim);
            animDelay += 30;

            // ----- 当天记录 -----
            List<Record> dayRecords = entry.getValue();
            for (int i = 0; i < dayRecords.size(); i++) {
                View itemView = createRecordItem(dayRecords.get(i));
                container.addView(itemView);

                // ✨ 入场动画
                AnimationSet anim = new AnimationSet(true);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.setStartOffset(animDelay);
                AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
                alpha.setDuration(300);
                TranslateAnimation translate = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0.2f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f);
                translate.setDuration(300);
                anim.addAnimation(alpha);
                anim.addAnimation(translate);
                itemView.startAnimation(anim);
                animDelay += 30;

                // ----- 组内分割线 -----
                if (i < dayRecords.size() - 1) {
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
    }

    private View createRecordItem(Record r) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16), dp(12), dp(16), dp(12));
        item.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // emoji
        TextView icon = new TextView(this);
        icon.setTextSize(22);
        icon.setGravity(Gravity.CENTER);
        icon.setText(Record.getCategoryEmoji(r.getCategory()));
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
        categoryTv.setText(r.getCategory() != null ? r.getCategory() : "其他");
        categoryTv.setTextSize(15);
        categoryTv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        // 备注（如果有且不同于分类名）
        boolean hasNote = r.getNote() != null && !r.getNote().isEmpty() && !r.getNote().equals(r.getCategory());
        if (hasNote) {
            TextView noteTv = new TextView(this);
            noteTv.setText(r.getNote());
            noteTv.setTextSize(11);
            noteTv.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
            noteTv.setPadding(0, dp(2), 0, 0);
            noteTv.setSingleLine(true);
            textLayout.addView(noteTv);
        }

        TextView dateTv = new TextView(this);
        dateTv.setText(r.getDate() != null ? r.getDate() : "");
        dateTv.setTextSize(11);
        dateTv.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        dateTv.setPadding(0, dp(2), 0, 0);

        textLayout.addView(categoryTv);
        textLayout.addView(dateTv);

        // 金额
        TextView amountTv = new TextView(this);
        boolean isIncome = Record.TYPE_INCOME.equals(r.getType());
        amountTv.setText((isIncome ? "+" : "-") + formatMoneyRaw(r.getAmount()));
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
                .setMessage("确定删除这条 " + r.getCategory() + " " + formatMoneyRaw(r.getAmount()) + " 的记录？")
                .setPositiveButton("删除", (d, w) -> {
                    recordManager.deleteRecord(r.getId());
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
