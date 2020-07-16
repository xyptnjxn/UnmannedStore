package com.example.unmannedstore;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.Window;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Toast;

import com.example.unmannedstore.Utils.ShowUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class RegisterActivity extends AppCompatActivity {

    private User user;
    private String phone;
    private String code;
    private String email;
    private String cardId;
    private String pwd;
    private String name;
    EditText phoneText;
    EditText codeText;
    EditText emailText;
    EditText cardText;
    EditText pwdText;
    EditText nameText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button registerBtn=(Button)findViewById(R.id.signUp);
        Button sendCodeBtn=(Button)findViewById(R.id.sendCode);
        phoneText=(EditText)findViewById(R.id.phone);
        codeText=(EditText)findViewById(R.id.code);
        emailText=(EditText)findViewById(R.id.email);
        cardText=(EditText)findViewById(R.id.card);
        pwdText=(EditText)findViewById(R.id.password);
        nameText=(EditText)findViewById(R.id.name);

        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        sendCodeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg();
            }
        });
    }
    /*bmob自动验证了
    public static boolean isMobileNO(String mobiles) {
		/*
		移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		联通：130、131、132、152、155、156、185、186
		电信：133、153、180、189、（1349卫通）/^0?1[3|4|5|7|8][0-9]\d{8}$/
		总结起来就是第一位必定为1，第二位必定为3或5或8或7（电信运营商），其他位置的可以为0-9
		*/
    /*
        String telRegex = "[1][34578]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }
    */

    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /*现行 16 位银联卡现行卡号开头 6 位是 622126～622925 之间的，7 到 15 位是银行自定义的，
    可能是发卡分行，发卡网点，发卡序号，第 16 位是校验码。

            16 位卡号校验位采用 Luhm 校验方法计算：
            1，将未带校验位的 15 位卡号从右依次编号 1 到 15，位于奇数位号上的数字乘以 2
            2，将奇位乘积的个十位全部相加，再加上所有偶数位上的数字
            3，将加法和加上校验位能被 10 整除。*/


    public static boolean checkBankCard(String cardId) {
        String str = "\\d{12}";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(cardId);
        return m.matches();
        }

    public static boolean checkPassword(String password) {
        String str = "\\d{6}";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(password);
        return m.matches();
    }


    public void register() {
        phone=phoneText.getText().toString();
        code=codeText.getText().toString();
        email=emailText.getText().toString();
        cardId=cardText.getText().toString();
        pwd=pwdText.getText().toString();
        name=nameText.getText().toString();
        user = new User();
        user.setCardId(cardId);
        user.setEmail(email);
        user.setHealth(false);
        user.setName(name);
        user.setPassWord(pwd);
        user.setPhone(phone);
        if(isEmail(email)==false){
            show("邮箱格式错误");
        }
        else if(checkPassword(pwd)==false){
            show("密码格式错误");
        }
        else if(checkBankCard(cardId)==false){
            show("银行卡格式错误");
        }
        else if(isEmail(email)&&checkBankCard(cardId)&&checkPassword(pwd)){
            BmobQuery<User> query = new BmobQuery<>();
            BmobQuery<Card> cardQuery = new BmobQuery<>();
            query.addWhereEqualTo("email", email);
            query.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    /*if (e != null) {
                        show(e.getMessage());
                    } else */if (list.size() != 0) {
                        show("用户名已存在");
                        return;
                    } else {
                        user.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if (null != e) {
                                    show(e.getMessage());
                                } else {
                                    cardQuery.addWhereEqualTo("cardId", cardId);
                                    cardQuery.findObjects(new FindListener<Card>() {
                                        @Override
                                        public void done(List<Card> list, BmobException e) {
                                            if (list.size() != 0) {
                                                show("该银行卡已存在");
                                                user.delete(new UpdateListener() {
                                                    @Override
                                                    public void done(BmobException e) {
                                                        if(e==null){
                                                            ShowUtils.log("删除成功:"+user.getUpdatedAt());
                                                        }else{
                                                            ShowUtils.log("删除失败：" + e.getMessage());
                                                        }
                                                    }
                                                });
                                            } else {
                                                Card card = new Card();
                                                card.setBalance(1000.00);
                                                card.setCardId(cardId);
                                                card.setPassWord(pwd);
                                                card.setUserName(email);
                                                card.save(new SaveListener<String>() {
                                                    @Override
                                                    public void done(String s, BmobException e) {
                                                        if (null != e) {
                                                            show(2 + e.getMessage());
                                                        } else {
                                                            BmobUser.signOrLoginByMobilePhone(phone, code, new LogInListener<BmobUser>() {
                                                                @Override
                                                                public void done(BmobUser bmobUser, BmobException e) {
//                                                        show(code);
//                                                        if (e == null) {
//                                                            //注册成功
//                                                            show("注册成功");
//                                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//                                                            startActivity(intent);
//                                                        } else {
//                                                            show(e.getMessage());
//                                                            show("验证码错误");
//                                                        }
                                                                    show("注册成功");
                                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                                    startActivity(intent);
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
                        });
                    }
                }
            });
        }
    }

    public void sendMsg() {
        phone = phoneText.getText().toString();
        BmobSMS.requestSMSCode(phone, "myTemplate", new QueryListener<Integer>() {
            @Override
            public void done(Integer smsId, BmobException e) {
                if (e == null) {
                    show("发送验证码成功");
                } else {
                    show("发送验证码失败，请重新发送");
                }
            }
        });
    }

    public void show(String msg) {
        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}