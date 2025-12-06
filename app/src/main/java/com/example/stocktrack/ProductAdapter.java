package com.example.stocktrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> products;
    private final OnItemClickListener listener;
    private int expandedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(Product product);
        void onEditClick(Product product);
        void onSetLowStockClick(Product product);
        void onSetSoldOutClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_products, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        // Bind data
        holder.tvProductName.setText(product.getName());
        holder.tvProductCategory.setText(product.getCategory());
        holder.tvSellingPrice.setText(String.format("Selling: ₱%.2f", product.getSellingPrice()));
        holder.tvQuantity.setText(String.format("Qty: %d", product.getQuantity()));

        // Handle expanded state
        final boolean isExpanded = position == expandedPosition;
        holder.extendedOptions.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnLongClickListener(v -> {
            if (expandedPosition >= 0) {
                notifyItemChanged(expandedPosition);
            }
            expandedPosition = isExpanded ? -1 : position;
            notifyItemChanged(position);
            return true;
        });

        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
        holder.btnEditItem.setOnClickListener(v -> listener.onEditClick(product));
        holder.btnSetLowStock.setOnClickListener(v -> listener.onSetLowStockClick(product));
        holder.btnSetSoldOut.setOnClickListener(v -> listener.onSetSoldOutClick(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductCategory, tvSellingPrice, tvQuantity;
        ImageView ivBarcodeIcon;
        LinearLayout extendedOptions;
        Button btnEditItem, btnSetLowStock, btnSetSoldOut;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCategory = itemView.findViewById(R.id.tv_product_category);
            tvSellingPrice = itemView.findViewById(R.id.tv_selling_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            ivBarcodeIcon = itemView.findViewById(R.id.iv_barcode_icon);
            extendedOptions = itemView.findViewById(R.id.extended_options);
            btnEditItem = itemView.findViewById(R.id.btn_edit_item);
            btnSetLowStock = itemView.findViewById(R.id.btn_set_low_stock);
            btnSetSoldOut = itemView.findViewById(R.id.btn_set_sold_out);
        }
    }
}
