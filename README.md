# StackScrollLayout
##可折叠的下拉菜单动效展示<br>
![](https://github.com/stillwheel/StatckScrollLayout/blob/master/gif/stack_scroll_layout.gif)


###配置说明
``````
1.直接将/StackscrollLayout/build/outputs/aar/StackscrollLayout-release.aar放入lib目录下,
并修改build.gradle文件
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:23.3.0'
    compile(name:'StackscrollLayout-release',ext:'aar');
}

2.如需修改源代码, 请在项目中导入StackscrollLayout这个module, 并做如下配置
build.gradle
dependencies {
    ... 
    compile project(':StackscrollLayout')
    ...
}
settings.gradle
include ':StackscrollLayout'
``````

###使用说明
  具体使用方法可参考上传的demo
``````
1.初始化
stackViewAdapter = new StackViewAdapter(LayoutInflater.from(this)); // 创建一个adapter
stackViewAdapter.setData(getData()); // 为Adapter绑定数据
mStackScroller.setAdapter(stackViewAdapter); //为Scroller设置adapter
mStackScroller.setOnItemClickListener(onStackItemClickListener); // 绑定点击事件
stackViewAdapter继承StackViewBaseAdapter实现了ListAdapter, SpinnerAdapter接口，
所以这里的adapter实现逻辑与ListView的adapter基本一样, 刷新数据时只要调下stackViewAdapter的notifyDataSetChanged即可
adapter的实现逻辑主要在StackScrollLayout.java与StackViewBaseAdapter.java中

2.一些方法 
展开: stackScrollPanelView.expand(false);
关闭: stackScrollPanelView.collapse(false);
其中括号里的参数为delay, false表示直接执行操作, true表示延迟一段时间后执行
改delay的值可以在PanelView修改对应delay参数

3.核心代码
第一次点击屏幕时,由于此时view未展开，Touch事件首先被父类PanelView拦截处理：
代码：PanelView的ActionDown中return true拦截该事件;


Action_Down ---> (mExpandedHeight == 0 || PanelView正在进行展开或收合动画) Touch事件首先被父类PanelView拦截处理： 
    然后onTouchEvent的ActionMove方法：此处主要处理下拉时更新View的状态
    final float newHeight = Math.max(0, h + mInitialOffsetOnTouch);
    setExpandedHeightInternal(newHeight);
    setExpandedHeightInternal->onHeightUpdated->mStackScroller.setStackHeight(expandedHeight),这里最终实现下拉展开效果

Action_Down ---> (PanelView已经全部展开时) 最后会交友StackScrollLayout的onScrollTouch函数处理
overScrollBy:随手指拖拽
overScrollDown:向下拖拽到达顶部时，继续向下拖拽
overScrollUp:向上拖拽到达底部时，继续向上拖拽
setOverScrollAmount ---> onOverScrolled --> applyCurrentState ---> StackScrollState的apply()方法,
apply方法是处理view的层叠效果的

updateChildren() ->StackScrollAlgorithm的getStackScrollState ---> 调用findNumberOfItemsInTopStackAndUpdateState计算即将被完全遮挡的view 的index
然后再调用updatePositionsForState方法计算每个View的currentYPosition = childViewState.yTranslation + childHeight + mPaddingBetweenElements;


4.版本兼容问题
    API21一下不支持elevation,所以API21以下的View层进行了下处理:
    StackScrollLayout.java
    @Override
    protected void dispatchDraw(Canvas canvas) { // 重绘View的涂层
        if (Define.SDK_INT < 21) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                drawChild(canvas, child, getDrawingTime());
            }
        } else {
            super.dispatchDraw(canvas);
        }
    }
    ExpandableView.java
    @Override
        protected void dispatchDraw(Canvas canvas) { // 重绘View边缘的阴影
            if (Define.SDK_INT < 21) {
                super.dispatchDraw(canvas);
    
                canvas.save();
                Paint paint = new Paint();
                paint.setColor(Color.TRANSPARENT);
                paint.setAlpha(currentAlpha);
                paint.setStrokeWidth(0);
    
                Shader shader = new LinearGradient(0, getHeight() - shadowRadius, 0, getHeight(),
                        new int[] {shadowStartColor, shadowEndColor}, null, Shader.TileMode.MIRROR);
                paint.setShader(shader);
                canvas.drawRect(0, getHeight() - shadowRadius, getWidth(), getHeight(), paint);
    
                canvas.restore();
            } else {
                super.dispatchDraw(canvas);
            }
        }
``````````
