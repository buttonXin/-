package com.oldhigh.intimenotes.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oldhigh.intimenotes.R;
import com.oldhigh.intimenotes.bean.NewEventBean;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 浮动窗口的工具栏
 * Created by oldhigh on 2017/12/22.
 */

public class FloatViewUtil implements View.OnClickListener {

    //显示floatImage
    public static final int FLOAT_VIEW_IMAGE = 3;
    //显示中间布局
    public static final int FLOAT_VIEW = 4;

    private WindowManager mWindowManager;
    private Context mContext;

    private WindowManager.LayoutParams mLayoutParams;

    private View mFloatViewImage;

    private View mFloatView;

    private Disposable mSubscribe;
    private boolean isAddView = false;
    private AnimatorSet mSet;
    private NewEventBean mNewEventBean;
    private EditText mEdit_content;

    public FloatViewUtil(Context context) {

        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mFloatViewImage = LayoutInflater.from(mContext).inflate(R.layout.float_view_button, null);
        mFloatView = LayoutInflater.from(mContext).inflate(R.layout.float_view, null);
    }

    /**
     * 创建浮动图片
     *
     * @param newEventBean
     */
    public void createImage(NewEventBean newEventBean) {

        mNewEventBean = newEventBean;

        RxHelper.checkDisposable(mSubscribe);

        //如果添加了就不在进行添加， 只是重置一下消失时间
        if (!isAddView) {

            windowManagerAddView(mFloatViewImage, FLOAT_VIEW_IMAGE);

            mFloatViewImage.findViewById(R.id.image_float).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    createFloatView();

                    RxHelper.checkDisposable(mSubscribe);
                    mWindowManager.removeView(mFloatViewImage);
                    //点击后改为flase
                    isAddView = !isAddView;
                }
            });

            //添加
            isAddView = true;

            L.e("image -- > ok。。");
        }

        mSubscribe = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(6)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        animationImage(aLong);
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (isAddView && aLong == 5) {
                            removeView(mFloatViewImage);
                            isAddView = !isAddView;

                            if (mSet != null) {
                                mSet.cancel();
                            }
                            RxHelper.checkDisposable(mSubscribe);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        L.e("这里错了");
                    }
                });
    }

    /**
     * 添加动画
     *
     * @param aLong
     */
    private void animationImage(Long aLong) {
        if (!isAddView) {
            return;
        }
        if (aLong % 2 == 0) {
            return;
        }

        mSet = new AnimatorSet();
        mSet.playTogether(
                ObjectAnimator.ofFloat(mFloatViewImage, "scaleX", 1, 1.3f, 1),
                ObjectAnimator.ofFloat(mFloatViewImage, "scaleY", 1, 1.3f, 1)

        );

        mSet.setDuration(300).start();
    }


    /**
     * 创建 显示 浮动窗口
     */
    private void createFloatView() {

        windowManagerAddView(mFloatView, FLOAT_VIEW);

        mEdit_content = mFloatView.findViewById(R.id.edit_content);
        RelativeLayout rl_view = mFloatView.findViewById(R.id.rl_view);

        TextView text_add = mFloatView.findViewById(R.id.text_add);
        TextView text_search = mFloatView.findViewById(R.id.text_search);
        //View view_bg = mFloatView.findViewById(R.id.view_bg);
        View view_back = mFloatView.findViewById(R.id.view_back);
        View view_setting = mFloatView.findViewById(R.id.view_setting);

        text_add.setOnClickListener(this);

        text_search.setOnClickListener(this);
        view_back.setOnClickListener(this);
        //view_bg.setOnClickListener(this);


        mEdit_content.setText(mNewEventBean.getContent().trim());


        onOtherTouchListener(mFloatView , rl_view);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.text_add:
                RealmUtil.add(mNewEventBean);

                if (mEdit_content != null) {
                    mEdit_content.getText().clear();
                }
                break;
            case R.id.text_search:
                if (mEdit_content != null) {

                    openURlAdress(mContext , mEdit_content.getText().toString());
                }
                break;

            case R.id.view_back:
                mWindowManager.removeView(mFloatView);
                break;
            /*case R.id.view_bg:
                //mWindowManager.removeView(mFloatView);
                break;*/
            case R.id.view_setting:
                Toast.makeText(mContext, "还没加呢", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    /**
     * 添加到window中
     *
     * @param view
     * @param type
     */
    private void windowManagerAddView(View view, int type) {
        mLayoutParams = new WindowManager.LayoutParams();

        int LPtype;
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.N_MR1){
            LPtype = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LPtype = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            LPtype = WindowManager.LayoutParams.TYPE_PHONE;
        }

        mLayoutParams.type = LPtype;


        //浮动的图片
        if (type == FLOAT_VIEW_IMAGE) {

            // 设置Window flag
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;

            // 设置悬浮窗的长得宽
            mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;


            mLayoutParams.gravity = Gravity.END | Gravity.TOP;
            mLayoutParams.x = 10;
            mLayoutParams.y = ScreenUtil.statusHeight(mContext) +
                    ScreenUtil.toolBarHeight(mContext);
        }
        //浮动的输入框等
        if (type == FLOAT_VIEW) {

            // 设置Window flag
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;

            // 设置悬浮窗的长得宽
            mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;


            mLayoutParams.gravity = Gravity.START | Gravity.TOP;
            mLayoutParams.x = 0;
            mLayoutParams.y = 0;

        }


        mLayoutParams.format = PixelFormat.RGBA_8888;

        mWindowManager.addView(view, mLayoutParams);


    }

    /**
     * 点击外部可以清楚view
     *
     * @param view
     * @param rl_view
     */
    private void onOtherTouchListener(final View view, final RelativeLayout rl_view) {
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                int x = (int) event.getX();
                int y = (int) event.getY();
                Rect rect = new Rect();
                rl_view.getGlobalVisibleRect(rect);

                if (!rect.contains(x, y)) {
                    removeView(view);
                }
                L.e("onTouch : " + x + ", " + y + ", rect: "
                        + rect);
                return false;
            }
        });
    }

    /**
     * 移除view
     *
     * @param view
     */
    public void removeView(View view) {
        try {
            mWindowManager.removeView(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加返回事件
     */
    public void onBackPress(final View view) {
        // 点击back键可消除
        L.e("start key");
        view.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                L.e("key code " + keyCode);
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        removeView(view);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    /**
     * 清理数据
     */
    public void clear() {

        mContext = null;
        mWindowManager = null;
        mFloatView = null;
        mFloatViewImage = null;
        mSet = null;
        RxHelper.checkDisposable(mSubscribe);


    }


    public static void openURlAdress(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http:"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}