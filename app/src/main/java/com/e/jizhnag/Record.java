package com.e.jizhnag;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;

/**
 * ========================================
 * 记账记录数据模型
 * 每一条支出/收入都对应一个 Record 对象
 * ========================================
 */
public class Record {

    // ---------- 常量 ----------
    /** 收入类型标识 */
    public static final String TYPE_INCOME = "income";
    /** 支出类型标识 */
    public static final String TYPE_EXPENSE = "expense";

    // ---------- 字段 ----------
    /** 唯一 ID（UUID 自动生成） */
    public String id;
    /** 类型：TYPE_INCOME("income") / TYPE_EXPENSE("expense") */
    public String type;
    /** 金额，单位元 */
    public double amount;
    /** 分类，如"餐饮"、"交通"、"工资" */
    public String category;
    /** 备注文字 */
    public String note;
    /** 日期，格式 yyyy-MM-dd */
    public String date;
    /** 创建时间戳（毫秒） */
    public long timestamp;

    // ---------- 构造 ----------

    /** 空构造：自动生成 ID 和时间戳 */
    public Record() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 全参构造
     * @param type     income / expense
     * @param amount   金额
     * @param category 分类
     * @param note     备注
     * @param date     日期 yyyy-MM-dd
     */
    public Record(String type, double amount, String category, String note, String date) {
        this();
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
    }

    // ---------- JSON 序列化 / 反序列化 ----------

    /** 把当前对象转为 JSONObject（用于保存到 SharedPreferences） */
    public JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("type", type);
            obj.put("amount", amount);
            obj.put("category", category);
            obj.put("note", note);
            obj.put("date", date);
            obj.put("timestamp", timestamp);
            return obj;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    /** 从 JSONObject 还原 Record 对象 */
    public static Record fromJson(JSONObject obj) {
        Record r = new Record();
        try {
            r.id       = obj.optString("id", r.id);
            r.type     = obj.optString("type", TYPE_EXPENSE);
            r.amount   = obj.optDouble("amount", 0);
            r.category = obj.optString("category", "其他");
            r.note     = obj.optString("note", "");
            r.date     = obj.optString("date", "");
            r.timestamp= obj.optLong("timestamp", System.currentTimeMillis());
        } catch (Exception ignored) {}
        return r;
    }

    // ---------- 工具方法 ----------

    /**
     * 根据分类名称返回对应的 emoji 图标
     * 展示在记录列表和记账弹窗中
     * @param category 分类名称（中文）
     * @return emoji 字符串
     */
    public static String getCategoryEmoji(String category) {
        switch (category) {
            case "餐饮": return "🍜";   // 面条 - 餐食
            case "交通": return "🚇";   // 地铁 - 出行
            case "购物": return "🛒";   // 购物车 - 购物
            case "娱乐": return "🎮";   // 手柄 - 娱乐
            case "医疗": return "💊";   // 药丸 - 医疗
            case "教育": return "📚";   // 书本 - 教育
            case "人情": return "🧧";   // 红包 - 人情
            case "日用": return "🧴";   // 瓶子 - 日用
            case "服饰": return "👔";   // 衬衫 - 服饰
            case "住房": return "🏠";   // 房子 - 住房
            case "通讯": return "📱";   // 手机 - 通讯
            case "工资": return "💼";   // 公文包 - 工资
            case "奖金": return "🏆";   // 奖杯 - 奖金
            case "理财": return "📈";   // 上涨趋势 - 理财
            case "兼职": return "🛠️";  // 工具 - 兼职
            case "红包": return "🧧";   // 红包 - 红包收入
            default: return "📌";       // 图钉 - 其他
        }
    }
}
