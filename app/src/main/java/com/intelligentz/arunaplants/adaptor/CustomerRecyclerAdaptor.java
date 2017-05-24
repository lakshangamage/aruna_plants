package com.intelligentz.arunaplants.adaptor;

import android.app.Activity;
import android.content.Context;
import android.support.v4.text.TextDirectionHeuristicCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.intelligentz.arunaplants.model.Customer;
import com.intelligentz.arunaplants.R;
import com.intelligentz.arunaplants.view.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Lakshan on 2017-01-07.
 */

public class CustomerRecyclerAdaptor extends RecyclerView.Adapter<CustomerRecyclerAdaptor.CustomerRecyclerViewHolder> implements View.OnClickListener{
    ArrayList<Customer> customerList = null;
    Context context = null;
    Activity activity;
    public CustomerRecyclerAdaptor(ArrayList<Customer> customerList, Context context, Activity activity) {
        this.context = context;
        this.customerList = customerList;
        this.activity = activity;
    }

    @Override
    public CustomerRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_row_layout,parent,false);
        CustomerRecyclerViewHolder recyclerViewHolder = new CustomerRecyclerViewHolder(view, context, customerList);
        return recyclerViewHolder;
    }
    @Override
    public void onBindViewHolder(CustomerRecyclerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.name_txt.setText(customer.getName());
        holder.nic_txt.setText(customer.getNic());
        holder.mobile_txt.setText(customer.getMobile());
        holder.address_txt.setText(customer.getAddress());
        if (MainActivity.type.equals("1")){
            holder.officer_txt.setVisibility(View.VISIBLE);
            holder.officer_txt.setText("officer: "+customer.getOfficer_name()+"("+customer.getOfficer_id()+")");
        } else {
            if (holder.officer_txt.getVisibility() != View.GONE) {
                holder.officer_txt.setVisibility(View.GONE);
            }
        }
    }
    @Override
    public int getItemCount() {
        return customerList.size();
    }
    @Override
    public void onClick(View view) {

    }

    public void collectPayment(int position){
        ((MainActivity)activity).collectPayment(position);
    }

    public class CustomerRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name_txt;
        TextView mobile_txt;
        TextView address_txt;
        TextView nic_txt;
        TextView officer_txt;
        Button collect_btn;
        ArrayList<Customer> customerList = null;
        Context context = null;
        public CustomerRecyclerViewHolder(View itemView, Context context, ArrayList<Customer> customerList) {
            super(itemView);
            this.context =context;
            this.customerList = customerList;
            name_txt = (TextView) itemView.findViewById(R.id.customer_name);
            mobile_txt = (TextView) itemView.findViewById(R.id.mobile_txt);
            address_txt = (TextView) itemView.findViewById(R.id.address_txt);
            nic_txt = (TextView) itemView.findViewById(R.id.nic_txt);
            officer_txt = (TextView) itemView.findViewById(R.id.officer_txt);
            collect_btn = (Button) itemView.findViewById(R.id.collect_btn);
            collect_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            collectPayment(getAdapterPosition());
        }
    }
}

