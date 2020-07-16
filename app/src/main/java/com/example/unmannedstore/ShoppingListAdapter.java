package com.example.unmannedstore;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ShoppingListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<ShoppingItemBean> shoppingList;
    private ShoppingActivity spAc;

    public ShoppingListAdapter(Context context, List<ShoppingItemBean> shoppingList, ShoppingActivity temp){
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.shoppingList = shoppingList;
        this.spAc = temp;
    }

    @Override
    public int getCount() {
        return shoppingList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    static class ViewHolder{
        public ImageView imageView;
        public TextView tvName, tvPrice, tvCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
//        ViewHolder holder = null;
//        if(view == null){
//            view = mLayoutInflater.inflate(R.layout.shopping_item, null);
//            holder = new ViewHolder();
//            holder.imageView = view.findViewById(R.id.iv);
//            holder.tvName = view.findViewById(R.id.tv_item_name);
//            holder.tvPrice = view.findViewById(R.id.tv_item_price);
//            holder.tvCount = view.findViewById(R.id.tv_item_count);
//            view.setTag(holder);
//        }else{
//            holder = (ViewHolder) view.getTag();
//        }
        View view = null;
        View viewT = null;
        if(convertView != null){
            view = convertView;
        }else{
            view = View.inflate(mContext, R.layout.shopping_item, null);
        }

        ShoppingItemBean shoppingItemBean = shoppingList.get(position);
        if(shoppingItemBean == null){
            shoppingItemBean = new ShoppingItemBean("Nothing", 0);
        }

        //更新数据
        final TextView itemTextView = view.findViewById(R.id.tv_item_name);
        itemTextView.setText(shoppingItemBean.getName());
        TextView priceTextView = view.findViewById(R.id.tv_item_price);
        priceTextView.setText(""+shoppingItemBean.getPrice());
        TextView countTextView = view.findViewById(R.id.tv_item_count);
        countTextView.setText("x "+shoppingItemBean.getCount());
        ImageView imageView = view.findViewById(R.id.iv_shoppingitem);
        switch (shoppingItemBean.getName()){
            case "fish":
                imageView.setImageResource(R.drawable.ic_fish);
                break;
            case "meat":
                imageView.setImageResource(R.drawable.ic_meat);
                break;
            case "water":
                imageView.setImageResource(R.drawable.ic_water);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_shopping_default);
        }

//        TextView sumTextView = view.findViewById();
//        sumTextView.setText("总额：");

        final int removePosition = position;

        Button deleteButton = view.findViewById(R.id.btn_delete_item);
        View finalViewT = viewT;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //删除按钮点击事件
                shoppingList.remove(position);
                spAc.setSum();
                notifyDataSetChanged();
            }
        });

//        holder.tvName.setText("thisisname");
//        holder.tvPrice.setText("thisisprice");
//        holder.tvCount.setText("thisiscount");
        return view;
    }

    private void deleteButtonAction(int position){
        ShoppingItemBean remove = shoppingList.remove(position);
        notifyDataSetChanged();
    }

    private int sum(){
        int total = 0;
        for(ShoppingItemBean shoppingItemBean : shoppingList){
            total += shoppingItemBean.getPrice() * shoppingItemBean.getCount();
        }
        return total;
    }
}
