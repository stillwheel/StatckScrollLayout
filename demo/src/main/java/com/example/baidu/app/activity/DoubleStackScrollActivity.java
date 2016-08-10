package com.example.baidu.app.activity;

import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.view.StackScrollPanelView;
import com.example.baidu.app.R;
import com.example.baidu.app.adapter.DoubleStackViewAdapter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by helingjian on 16/3/14.
 */
public class DoubleStackScrollActivity extends Activity {

    String[] brands =
            new String[] {"雅戈尔", "法利鳄鱼", "皮特丹顿", "蓝豹", "九牧王", "路易诗兰", "马克华菲", "法国鳄鱼", "蔓哈顿", "汤尼威尔", "雷迪波尔", "柏郎亚高",
                    "萨尔雷斯", "庄子", "豹狮杰", "富哥", "开蒙", "卡敦斐尔", "袋鼠", "企鹅岛/杰翰", "卡丹路", "老人城", "法国老人头", "唐鹰", "帝高", "纳巴罗",
                    "杰凡尼", "皮尔卡丹", "堡尼", "杉杉", "罗蒙", "金利来", "迪迪公子", "报喜鸟", "法派", "汤尼俊仕", "花雨伞", "花花公子", "U",
                    "卡努迪路", "BOSS", "迪莱", "梦特娇", "宝姿", "海澜之家", "ESPRIT", "保罗世家", "NAUTICA", "BABEI(巴贝)",
                    "CROCODILE(鳄鱼恤)", "比华利保罗", "波司登", "七匹狼", "USPOLO", "塔吉", "奥德臣", "斯诺沙克", "卡利斯特", "威可多", "JOEONE",
                    "马思图", "BAILY(巴利)", "尼诺卡丹", "雷诺天蒙", "虎都", "圣大保罗", "卡迪尔", "鳄鱼", "法国圣龙", "沙弛", "GORNIA",
                    "卡斯德利", "杰克琼斯", "巴宝利", "迪赛", "汤米", "路易世尊", "鄂尔多斯", "路易德士", "与狼共舞", "老爷车", "轩帝尼", "凯撒", "V-ONE",
                    "富铤", "旗牌王", "娃娃鱼", "胜龙", "昂斯", "布莱.希尔顿", "马基堡", "北京威可多", "日弛尼", "波顿", "克美.米罗", "卓凡里诺", "歌德克依",
                    "萨巴帝尼", "登喜路", "吉普", "洛滋", "PIEEDENTON", "铁狮丹顿", "萨托尼", "西海岸", "培罗城", "帕给维龙", "法函诗", "意劳迪斯", "应大",
                    "依文", "NOVELI", "DEZUN", "卡丹迪诺", "维尼熊", "威玛", "艾登堡", "吉田.格雷曼", "萨里奥托", "SWUBTRO", "宾度", "DIDIBOY",
                    "法曼斯", "雅格狮丹", "宏士达", "路卡迪龙", "红豆", "澳林斯顿", "卡拉里尼", "EZIO", "卡帝乐鳄鱼", "米盖尔", "吉尼亚", "欧麦格", "贝尼",
                    "ERDOS", "亚涛", "菲罗伦思", "智圣", "V.E.DELURE", "美国苹果", "浪比时", "卡尔丹顿", "CK", "海螺", "圣吉.卡丹", "骆驼",
                    "西斯顿", "圣宝龙", "ZzHISHEENG", "庄吉", "法梦迪尔", "博斯里拉", "卡丹雷杰", "KENT﹠CURWEN", "龙达飞", "纪凡希", "杭州苹果", "洲艳",
                    "新巴贝", "名马", "约翰", "派尔森", "华伦天奴", "雄", "都彭", "堡尼.丹尼", "佛伦杰尼", "赛思特", "才子", "邦纳斯", "利郎", "豪年度",
                    "思豪CEO", "步森", "丽都法诗", "天地人", "瑞尔纳", "欧兰诺", "红杉树", "金狮鹏", "卡莎米亚", "太子龙", "曼克顿", "虎豹", "沃尔夫", "金盾",
                    "威斯康尼", "名盾", "玛克世家", "EVERGENERATING", "奇尔斯丹.老人城", "苹果", "华斯度", "BALENCIAGA", "诺帝卡", "艾帝", "博格西尼",
                    "集杰", "郝捷伦", "卡斯保罗", "巴克兰盾", "新奥斯卡丹", "POLO", "斯得亚", "幸运鹿", "万祺凯易", "华伦", "斯卡图", "耶纳诺", "CERRUT",
                    "FROGNIEZILA", "伊托马斯", "郎维高", "苹果树", "奥里托那", "天恒", "雷弛", "班尼波士", "乔顿", "奥伦多兰", "左天奴", "古士旗",
                    "威鹿", "秋艳", "佛伦斯", "绅浪", "伊斯丹奴", "FONOCC", "阿雷克斯.鲁尼", "稻草人", "亨威", "八千代", "迪仕豹", "凯洋", "顺美", "路嘉纳",
                    "仕东利", "浪登", "CANLI", "GALAXY(佳乐喜)", "BYNIC", "TESTANTIN", "埃得蒙", "培罗蒙", "利丹", "肯迪文", "欧彭", "卡奴丹路",
                    "罗马世家", "恒源祥", "莱斯马", "TSTANIN", "格尼亚", "皇家圣保罗", "古杰狮", "哈雷纳.金狐", "岳豹", "CTPARIS/娃娃鱼", "花狐/阿尔皮纳",
                    "爵士丹尼", "贝利龙", "威可多", "蓝豹", "LEONARDO/老人头", "大维", "诗丹贝克", "利丹王", "胜龙", "UCLA", "法国百灵鸟", "GNTLE",
                    "乔夫", "圣罗兰", "圣凡尼", "GNTLEGOODLUCKCLAD", "卡莱蒂尼", "凯迪东倪", "雷诺马汀", "宗洋", "新竹针织", "万事好", "鹿王", "新罗蒙",
                    "东方圣罗", "佛尼亚", "金狐狸", "喜尔得", "亚韵", "圣甸奴", "ELLEHOMME", "汤尼俊仕", "利奥纳多/老人头", "法兰诗顿", "德雷萨斯来客", "地牌",
                    "金丝狐", "卡博尔", "凯莱露喜", "里奇波士", "四海龙", "浪肯", "马狮龙", "劲霸", "斯蒂文", "玛珂爱萨尼", "阔伯", "真正", "REMOGLANNI",
                    "萨尔雷斯", "庄子", "豹狮杰", "富哥", "开蒙", "卡敦斐尔", "袋鼠", "企鹅岛/杰翰", "卡丹路", "老人城", "法国老人头", "唐鹰", "帝高", "纳巴罗",
                    "绅贵", "迈亚", "乔治亨", "TIESHIDANDUN", "特派", "比音勒芬", "仕帝曼", "积家", "幸运岛", "虎都", "喜来登", "大红鹰", "乔顿",
                    "雷诺天蒙", "虎都", "圣大保罗", "卡迪尔", "鳄鱼", "法国圣龙", "尼诺卡丹", "雷诺天蒙", "虎都", "圣大保罗", "FALIEYUZJIEYU", "法国老人头"};
    private int[] drawableLeftIds;
    private String[] leftTitles;
    private DoubleStackViewAdapter stackViewAdapterLeft;
    private StackScrollLayout mStackScrollerLeft;
    private TextView stackHeaderLeft;
    private StackScrollLayout mStackScrollerRight;
    private TextView stackHeaderRight;
    private DoubleStackViewAdapter stackViewAdapterRight;
    private StackScrollPanelView stackScrollPanelViewRight;
    private StackScrollPanelView stackScrollPanelViewLeft;
    private TextView mainText;
    StackScrollLayout.OnItemClickListener onRightStackItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            stackScrollPanelViewLeft.collapse(false);
            stackScrollPanelViewRight.collapse(false);
            mainText.setText(((TextView) itemView.findViewById(R.id.text)).getText());
        }
    };
    private View lastItemView;
    StackScrollLayout.OnItemClickListener onLeftStackItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            stackScrollPanelViewRight.setExpandedHeightInternal(0);
            mStackScrollerRight.setStackHeight(0);
            stackScrollPanelViewRight.expand(true);
            stackViewAdapterRight.setData(getBrandDrawables(position), getBrandNames(position));
            stackViewAdapterRight.notifyDataSetChanged();
            changBackground(itemView, true);
            changBackground(lastItemView, false);
            lastItemView = itemView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_stack_layout);
        mainText = (TextView) findViewById(R.id.main_text);
        initValues();
        initScroller();
    }

    private void initScroller() {

        View leftStackView = findViewById(R.id.stack_view_left);
        stackScrollPanelViewLeft = (StackScrollPanelView) leftStackView.findViewById(R.id.stack_scroll_panel);
        mStackScrollerLeft = (StackScrollLayout) leftStackView.findViewById(R.id.stack_scroller);
        stackHeaderLeft = (TextView) leftStackView.findViewById(R.id.stack_scroll_header);
        stackViewAdapterLeft = new DoubleStackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterLeft.setData(drawableLeftIds, leftTitles);
        mStackScrollerLeft.setAdapter(stackViewAdapterLeft);
        mStackScrollerLeft.setOnItemClickListener(onLeftStackItemClickListener);

        View rightStackView = findViewById(R.id.stack_view_right);
        stackScrollPanelViewRight = (StackScrollPanelView) rightStackView.findViewById(R.id.stack_scroll_panel);
        mStackScrollerRight = (StackScrollLayout) rightStackView.findViewById(R.id.stack_scroller);
        stackHeaderRight = (TextView) rightStackView.findViewById(R.id.stack_scroll_header);
        stackViewAdapterRight = new DoubleStackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterRight.setData(getBrandDrawables(0), getBrandNames(0));
        mStackScrollerRight.setAdapter(stackViewAdapterRight);
        mStackScrollerRight.setOnItemClickListener(onRightStackItemClickListener);

        stackHeaderLeft.setText("类别");
        stackHeaderRight.setText("店铺");
    }

    private void initValues() {
        leftTitles = new String[] {
                getString(R.string.item_name_19), getString(R.string.item_name_18),
                getString(R.string.item_name_17), getString(R.string.item_name_16),
                getString(R.string.item_name_15), getString(R.string.item_name_14),
                getString(R.string.item_name_13), getString(R.string.item_name_12),
                getString(R.string.item_name_11), getString(R.string.item_name_10),
                getString(R.string.item_name_9), getString(R.string.item_name_8),
                getString(R.string.item_name_7), getString(R.string.item_name_6),
                getString(R.string.item_name_5), getString(R.string.item_name_4),
                getString(R.string.item_name_3), getString(R.string.item_name_2),
                getString(R.string.item_name_1)
        };
        drawableLeftIds = new int[] {
                R.drawable.icon1, R.drawable.icon2, R.drawable.icon3, R.drawable.icon4,
                R.drawable.icon5, R.drawable.icon6, R.drawable.icon7, R.drawable.icon8,
                R.drawable.icon9, R.drawable.icon10, R.drawable.icon11, R.drawable.icon12,
                R.drawable.icon13, R.drawable.icon4, R.drawable.icon15, R.drawable.icon16,
                R.drawable.icon17, R.drawable.icon8, R.drawable.icon19
        };
    }

    private String[] getBrandNames(int position) {
        int length = (position) % 3 == 0 ? 6 - (position % 3) : drawableLeftIds.length;
        String[] brandNames = new String[length];
        for (int i = 0; i < brandNames.length; i++) {
            brandNames[i] = brands[position * drawableLeftIds.length + i];
        }
        return brandNames;
    }

    private int[] getBrandDrawables(int position) {
        int[] brandDrawablesIds;
        if (position % 3 == 0) {
            brandDrawablesIds = new int[6];
            for (int i = 0; i < brandDrawablesIds.length; i++) {
                brandDrawablesIds[i] = drawableLeftIds[position % 6 + i];
            }
            return brandDrawablesIds;
        }

        brandDrawablesIds = new int[drawableLeftIds.length];
        for (int i = position; i < drawableLeftIds.length - position; i++) {
            brandDrawablesIds[i] = drawableLeftIds[i + position];
        }
        for (int i = 0; i < position; i++) {
            brandDrawablesIds[i] = drawableLeftIds[position - i];
        }
        return brandDrawablesIds;
    }

    private void changBackground(View itemView, boolean focus) {
        if (itemView == null) {
            return;
        }
        if (focus) {
            ((TextView) itemView.findViewById(R.id.text)).setTextColor(0xffff5579);
        } else {
            itemView.findViewById(R.id.content).setBackgroundColor(Color.WHITE);
            ((TextView) itemView.findViewById(R.id.text)).setTextColor(Color.BLACK);
        }
    }

}
