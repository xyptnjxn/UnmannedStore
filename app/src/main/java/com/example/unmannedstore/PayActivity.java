package com.example.unmannedstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unmannedstore.Utils.ShowUtils;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class PayActivity extends AppCompatActivity {

    private TextView tvTotalPay;
    private Button btnPay, btnCancel;
    private EditText etPassword;
    private String password, marketID;
    private double sum;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        Intent receiveIntent = getIntent();
        marketID = receiveIntent.getStringExtra("marketID"); //商家标识码
        sum = receiveIntent.getIntExtra("sum",0);
        user = new HomeActivity().user;

        tvTotalPay = findViewById(R.id.tv_totalpay);
        btnPay = findViewById(R.id.btn_pay);
        btnCancel = findViewById(R.id.btn_cancel);
        etPassword = findViewById(R.id.et_password);

        tvTotalPay.setText("您需要支付的总价为：" + sum);
        //password = "";
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBack = new Intent(PayActivity.this, ShoppingActivity.class);
                setResult(RESULT_CANCELED, goBack);
                finish();
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //和数据库进行交互
                password = String.valueOf(etPassword.getText()); //支付密码
                if (!password.equals(user.getPassWord())) {
//                    ShowUtils.show(PayActivity.this, "1 "+password);
//                    ShowUtils.show(PayActivity.this, "2 "+user.getPassWord());
                    ShowUtils.show(PayActivity.this, "密码错误");
                } else {
                    pay();
                }
            }
        });
        //支付模块
    }

    public void pay() {
        //Double money = new Double(20.05);
        double balance = Double.parseDouble(new HomeActivity().balance);
        if (sum > balance) {
            ShowUtils.show(PayActivity.this, "余额不足");
            return;
        } else {
            Card buyerCard = new HomeActivity().buyerCard;
            Card sellerCard = new HomeActivity().sellerCard;
            buyerCard.setBalance(buyerCard.getBalance()-sum);
            sellerCard.setBalance(sellerCard.getBalance()+sum);
            Log.v("2","有钱");
            sellerCard.update(sellerCard.getObjectId(), new UpdateListener() {
                @Override
                public void done(BmobException e1) {
                    if (null == e1) {
                        buyerCard.update(buyerCard.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e2) {
                                if (null == e2) {
                                    Transaction transaction = new Transaction();
                                    transaction.setBuyerId(buyerCard.getCardId());
                                    transaction.setSellerId(sellerCard.getCardId());
                                    transaction.setValue(sum);
                                    Log.v("2", transaction.toString());
                                    transaction.save(new SaveListener<String>() {
                                        @Override
                                        public void done(String s, BmobException e3) {
                                            if (null != e3) {
                                                ShowUtils.log(e3.getMessage());
                                            } else {
                                                ShowUtils.show(PayActivity.this, "交易成功");
                                                Log.v("2", sellerCard.toString());
                                                Log.v("2", buyerCard.toString());
                                                HomeActivity.balance = String.valueOf((buyerCard.getBalance()));
                                                //退回到主页
                                                Intent goBack = new Intent(PayActivity.this, ShoppingActivity.class);
                                                setResult(RESULT_OK, goBack);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
    }

}