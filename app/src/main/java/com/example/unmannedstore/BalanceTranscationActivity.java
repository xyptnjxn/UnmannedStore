package com.example.unmannedstore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unmannedstore.Utils.ShowUtils;
import com.example.unmannedstore.component.AdapterWrapper;
import com.example.unmannedstore.component.StickyListAdapter;
import com.example.unmannedstore.component.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class BalanceTranscationActivity extends Activity implements AdapterWrapper.OnHeaderClickListener, AdapterView.OnItemClickListener
        , StickyListHeadersListView.OnLoadingMoreLinstener, StickyListHeadersListView.OnHeaderClickListener{

    private LayoutInflater inflater;
    public static int transId = 0;
    private User user;
    ArrayList<String> slist;
    private ArrayList<TransactionDetail> tList;
    private String a;
    StickyListAdapter adapter;
    StickyListHeadersListView stickyLV;

    private RelativeLayout moredata;
    private View progressBarView;
    private TextView progressBarTextView;
    private AnimationDrawable loadingAnimation;
    private boolean isLoading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_transcation);

        Intent intent = getIntent();
        user = (User) intent.getExtras().get("User");
//        a = "type/money/other/mine/time/id/";
        a = "";
        tList = new ArrayList<TransactionDetail>();
        slist = new ArrayList<>();
        getTList();
    }

    private void loadingFinished() {

        if (null != loadingAnimation && loadingAnimation.isRunning()) {
            loadingAnimation.stop();
        }
        progressBarView.setVisibility(View.INVISIBLE);
        progressBarTextView.setVisibility(View.INVISIBLE);
        isLoading = false;

        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnLoadingMore() {
        progressBarView.setVisibility(View.VISIBLE);
        progressBarTextView.setVisibility(View.VISIBLE);

        loadingAnimation.start();

        if(!isLoading) {
            isLoading = true;
            new Handler().postDelayed(new Runnable() {

                String[] tr=a.split("/");
                int tr_length = tr.length;
                @Override
                public void run() {

                    for(int i = 0; i < 7; i ++) {
                        if(transId<tr_length) {
                            slist.add("\n"+"\b\b" +tr[transId + 0] + tr[transId + 1] +"￥"+ "\n\n" +"\b\b" + "other:\b" + tr[transId + 2] +
                                    "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + "mine:\b" + tr[transId + 3] + "\n\n" +"\b\b" + tr[transId + 4] +
                                    "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\bid:\b" + tr[transId + 5]+"\n");
                            transId = transId + 6;
                        }
                        else break;
                    }
                    loadingFinished();
                }
            }, 1200);
        }
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header,
                              int itemPosition, long headerId, boolean currentlySticky) {
        /*Toast.makeText(this, "header-list" + headerId, Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        /*Toast.makeText(this, "item" + position, Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onHeaderClick(View header, int itemPosition, long headerId) {
        /*Toast.makeText(this, "header" + headerId, Toast.LENGTH_SHORT).show();*/

    }

    public void getTList() {
        String cardId = user.getCardId();
        BmobQuery<Transaction> queryBuyer = new BmobQuery<>();
        BmobQuery<Transaction> querySeller = new BmobQuery<>();
        BmobQuery<Transaction> or = new BmobQuery<>();
        List<BmobQuery<Transaction>> queries = new ArrayList<>();
        queryBuyer.addWhereEqualTo("buyerId", cardId);
        querySeller.addWhereEqualTo("sellerId", cardId);
        queries.add(queryBuyer);
        queries.add(querySeller);
        or.or(queries).findObjects(new FindListener<Transaction>() {
            @Override
            public void done(List<Transaction> list, BmobException e) {
                if (null != e) {
                    ShowUtils.show(BalanceTranscationActivity.this, "e " + e.getMessage());
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        Transaction tmp = list.get(i);
                        TransactionDetail transactionDetail = new TransactionDetail();
                        transactionDetail.setId(tmp.getObjectId());
                        transactionDetail.setTime(tmp.getUpdatedAt());
                        transactionDetail.setMoney(tmp.getValue());
                        if (list.get(i).getBuyerId().equals(user.getCardId())) {
                            transactionDetail.setType('-');
                            transactionDetail.setMine(tmp.getBuyerId().substring(tmp.getBuyerId().length()-4));
                            transactionDetail.setOther(tmp.getSellerId().substring(tmp.getSellerId().length()-4));
                        } else {
                            transactionDetail.setType('+');
                            transactionDetail.setOther(tmp.getBuyerId().substring(tmp.getBuyerId().length()-4));
                            transactionDetail.setMine(tmp.getSellerId().substring(tmp.getSellerId().length()-4));
                        }

                        tList.add(transactionDetail);
                    }
                    for (int i = tList.size()-1; i >= 0; i--) {
                        a += tList.get(i);
                    }
                    Log.v("2", "流水查询成功");

                    if (a != "") {
                        String[] tr=a.split("/");
                        Log.v("2", tr.toString());
                        int tr_length = tr.length;
                        slist = new ArrayList<String>();
                        transId = 0;

                        for(int j = 1; j <= 7; j++) {
                            //set text view
                            if(transId<tr_length) {
                                slist.add("\n"+"\b\b" +tr[transId + 0] + tr[transId + 1] +"￥"+ "\n\n" +"\b\b" + "other:\b" + tr[transId + 2] +
                                        "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + "mine:\b" + tr[transId + 3] + "\n\n" +"\b\b" + tr[transId + 4] +
                                        "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\bid:\b" + tr[transId + 5]+"\n");
                                transId = transId + 6;
                            }
                            else break;
                        }
                    } else {
                        slist.add("");
                    }

                    adapter = new StickyListAdapter();
                    adapter.init(BalanceTranscationActivity.this, slist);

                    inflater = LayoutInflater.from(BalanceTranscationActivity.this);

                    moredata = (RelativeLayout)inflater.inflate(R.layout.moredata, null);
                    progressBarView = (View) moredata.findViewById(R.id.loadmore_foot_progressbar);
                    progressBarTextView = (TextView) moredata.findViewById(R.id.loadmore_foot_text);

                    stickyLV = (StickyListHeadersListView)BalanceTranscationActivity.this.findViewById(R.id.stickyList);

                    loadingAnimation = (AnimationDrawable) progressBarView.getBackground();
                    stickyLV.addFooterView(moredata);
                    stickyLV.setAdapter(adapter);

                    stickyLV.setOnItemClickListener(BalanceTranscationActivity.this);
                    stickyLV.setOnHeaderClickListener(BalanceTranscationActivity.this);
                    stickyLV.setLoadingMoreListener(BalanceTranscationActivity.this);
                }
            }
        });
    }
}