package com.example.unmannedstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.unmannedstore.Utils.QRCodeUtil;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import java.text.DecimalFormat;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class HomeActivity extends AppCompatActivity {

    public static User user;
    public static String SellerCardId = null;
    public static String balance;
    public static Card sellerCard;
    public static Card buyerCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent intent = getIntent();
        user = (User)intent.getExtras().get("User");
        Log.v("2", user.toString());
        // SellerCardId = "";
        sellerCard = new Card();
        buyerCard = new Card();
        Button balanceBtn=(Button) findViewById(R.id.search_button);//关联查询按钮
        Button toHealthBtn=(Button) findViewById(R.id.report_button);//关联上报健康信息按钮
        Button shopBtn=(Button) findViewById(R.id.shop_button);//关联购物按钮

        ImageButton scanBtn = (ImageButton) findViewById(R.id.scan_button);//关联扫码按钮
        ImageView mImageView = (ImageView) findViewById(R.id.code_image);
        final boolean health = user.isHealth();
        String color;
        if(health == true){
            color = "#71C671";
        }else{
            color = "#EE0000";
        }
        shopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isHealthy = user.isHealth();
                if(isHealthy == true) {
                    if(SellerCardId != null) {
                        Intent intent = new Intent(HomeActivity.this, ShoppingActivity.class);
                        // intent.putExtra("marketID", marketID); // 商家ID
                        startActivity(intent);
                    }else{
                        Toast.makeText(HomeActivity.this, "请先扫描正确的商家码！", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(HomeActivity.this, "您的健康状态不允许购物！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Bitmap mBitmap = QRCodeUtil.createQRCodeBitmap(user.getCardId(),221, Color.parseColor(color) ,Color.parseColor("#000000"));
        mImageView.setImageBitmap(mBitmap);
        //查询按钮绑定监听事件
		balanceBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 给查询按钮添加点击响应事件
				Intent intent=new Intent(HomeActivity.this,BalanceTranscationActivity.class);
				//启动
                intent.putExtra("User", user);
				startActivity(intent);
			}
		});
        toHealthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 给上报健康信息按钮添加监听事件
                Intent intent=new Intent(HomeActivity.this,HealthInfoActivity.class);
                intent.putExtra("User",user);
                //启动
                startActivity(intent);
            }

        });
//		shopBtn.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// 给购物按钮添加监听事件
//				Intent intent=new Intent(MainActivity.this,ShoppingActivity.class);
//				//启动
//				startActivity(intent);
//			}
//		});
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 给扫码按钮添加监听事件
                Intent intent=new Intent(HomeActivity.this, CaptureActivity.class);
                //启动
                startActivityForResult(intent, Constant.REQ_QR_CODE);
            }

        });
        getBalance();
        //shop();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean isChange=false;
        if(requestCode==1){
            isChange=data.getBooleanExtra("isChange",false);
        }else if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            //扫码结果获取商家银行卡号
            Bundle bundle = data.getExtras();
            SellerCardId = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            if(cardIdValid()){
                final boolean isHealthy = user.isHealth();
                if(isHealthy == true) {
                    Intent intent = new Intent(HomeActivity.this, ShoppingActivity.class);
                    intent.putExtra("marketID", SellerCardId); // 商家ID
                    startActivity(intent);
                }else{
                    Toast.makeText(HomeActivity.this, "您的健康状态不允许购物！", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(HomeActivity.this, "请扫描正确的商家码！", Toast.LENGTH_SHORT).show();
            }
        }

        //此处的requestCode就是startActivityForResult里面传递的requestCode
        // 可以用来区分是哪个按钮请求的
        // 如果健康状况改变则重新生成二维码
        if(isChange == true){
            User user = new User();
            boolean health = user.isHealth();
            ImageView mImageView = (ImageView) findViewById(R.id.code_image);
            String color;
            if(health == true){
                color = "#71C671";
            }else{
                color = "#EE0000";
            }
            Bitmap mBitmap = QRCodeUtil.createQRCodeBitmap(user.getCardId(),221, Color.parseColor(color) ,Color.parseColor("#000000"));
            mImageView.setImageBitmap(mBitmap);
        }
    }

    public void getBalance() {
        String cardId = user.getCardId();
        BmobQuery<Card> query = new BmobQuery<>();
        query.addWhereEqualTo("cardId", cardId);
        query.findObjects(new FindListener<Card>() {
            @Override
            public void done(List<Card> list, BmobException e) {
                if (null != e) {
                    Log.v("2", e.getMessage());
                    return;
                } else if (list.size() == 0) {
                    Log.v("2","查询错误");
                    return;
                } else {
                    buyerCard = list.get(0);
                    DecimalFormat df = new DecimalFormat("######0.00");
                    balance = df.format(buyerCard.getBalance());
                    Log.v("2","查询成功");
//                    if (SellerCardId != null) {
//                        shop();
//                    }
                }
            }
        });
    }

    public boolean cardIdValid(){
        for(int i=SellerCardId.length();--i>=0;){
            int chr=SellerCardId.charAt(i);
            if(chr<48 || chr>57)
                return false;
        }
        if(SellerCardId.length() != 12){ return false; }
        return true;
    }



    public void shop() {
        BmobQuery<Card> querySeller = new BmobQuery<>();
        querySeller.addWhereEqualTo("cardId", SellerCardId);
        querySeller.findObjects(new FindListener<Card>() {
            @Override
            public void done(List<Card> list, BmobException e) {
                if (null != e) {
                    Log.v("2", e.getMessage());
                    return;
                } else if (list.size() == 0) {
                    Log.v("2", "查询错误");
                    return;
                } else {
                    sellerCard = list.get(0);
                    Log.v("2", sellerCard.toString());
                }
            }
        });
    }
}
