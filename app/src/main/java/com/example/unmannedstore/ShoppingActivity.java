package com.example.unmannedstore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ShoppingActivity extends AppCompatActivity {

    private Context mContext;
    private ListView shoppingLv;
    private static TextView tvSum;
    private ShoppingListAdapter shoppingListAdapter;
    private List<ShoppingItemBean> shoppingList = new ArrayList<ShoppingItemBean>();
    private Button btnPay, btnScan;
    private int sum;
    private Map<String, Integer> itemList = new HashMap<String, Integer>();
    private String marketID = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);
        this.mContext = this;

        // Toast.makeText(this, "oncreate",Toast.LENGTH_LONG).show(); // 验证这个页面被创建了几次
        listInit(); //初始化商品列表
        Intent receiveIntent = getIntent();
        // marketID = receiveIntent.getStringExtra("marketID"); // 接受从主页传过来的商家识别码

        //加载listview
        shoppingLv = findViewById(R.id.lv_shopping);
        shoppingListAdapter = new ShoppingListAdapter(mContext, shoppingList, this);
        shoppingLv.setAdapter(shoppingListAdapter);

        btnPay = findViewById(R.id.btn_pay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sum = count(); //计算总额
                if(sum == 0){
                    Toast.makeText(ShoppingActivity.this, "请先购买商品！", Toast.LENGTH_SHORT).show();
                    //return error message
                }else{
                    Intent intent = new Intent(ShoppingActivity.this, PayActivity.class); //跳转到支付页面
                    intent.putExtra("sum", sum); //传递商品总价
                    // intent.putExtra("marketID", marketID);// 传递商家识别码
                    startActivityForResult(intent, 11);
                }
            }
        });

        //扫描商品的点击事件
        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoAnother = new Intent(ShoppingActivity.this, CaptureActivity.class);
                startActivityForResult(gotoAnother, 10);
            }
        });
        shop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && resultCode == RESULT_OK) {
            tvSum = findViewById(R.id.tv_sum);
            int price = findPrice(data.getStringExtra(Constant.INTENT_EXTRA_KEY_QR_SCAN));
            if (price == -1 ) {
                Toast.makeText(this, "请扫描正确的商品二维码！", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean existed = false;
            //将商品和它的价格添加到列表
            for (ShoppingItemBean shoppingItemBean : shoppingList) {
                if ((data.getStringExtra(Constant.INTENT_EXTRA_KEY_QR_SCAN).equals(shoppingItemBean.getName()))) {
                    //如果已经存在于List中，数量+1
                    shoppingItemBean.countPlus();
                    existed = true;
                    break;
                }
            }
            // 如果没有存在于商品列表中，添加商品列表
            if (existed == false) {
                ShoppingItemBean shoppingItemBean = new ShoppingItemBean(data.getStringExtra(Constant.INTENT_EXTRA_KEY_QR_SCAN), price);
                shoppingList.add(shoppingItemBean);
            }
            tvSum.setText("总额：" + count());
            shoppingListAdapter.notifyDataSetChanged();
        }
        if(requestCode == 11 && resultCode == RESULT_OK) {
            finish();
        }
        if(requestCode == 11 && resultCode == RESULT_CANCELED){
        }
    }

    public int count(){
        int totalMoney = 0;
        for(ShoppingItemBean shoppingItemBean : shoppingList){
            totalMoney += shoppingItemBean.getPrice() * shoppingItemBean.getCount();
        }
        return totalMoney;
    }

    //查询商品价格
    public int findPrice(String itemName){
        if(itemList.get(itemName) != null)
            return itemList.get(itemName);
        else
            return -1;
    }


    public void listInit(){
        itemList.put("fish", 15);
        itemList.put("meat", 20);
        itemList.put("water", 5);
    }


    public void shop() {
        String sellerCardId = new HomeActivity().SellerCardId;
        BmobQuery<Card> querySeller = new BmobQuery<>();
        querySeller.addWhereEqualTo("cardId", sellerCardId);
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
                        new HomeActivity().sellerCard = list.get(0);
                        Log.v("2", new HomeActivity().sellerCard.toString());
                    }
                }
            });
    }

    public void setSum() {
        TextView tv_Sum = findViewById(R.id.tv_sum);
        tv_Sum.setText("总额：" + count());
    }
}

