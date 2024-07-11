package com.example.quickbite;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FoodItemAdapter extends FirebaseRecyclerAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder> {
    private final Context context;
    private DatabaseReference reference;

    public FoodItemAdapter(@NonNull FirebaseRecyclerOptions<FoodItem> options, Context context) {
        super(options);
        this.context = context;
        this.reference = FirebaseDatabase.getInstance().getReference().child("foodItems");
    }

    @Override
    protected void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position, @NonNull FoodItem model) {
        holder.bind(model);

        // Handle item clicks
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrderFormDialog(model);
            }
        });

        // Handle long clicks for update/delete
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPasswordDialogForUpdateDelete(holder.getAdapterPosition(), model);
                return true;
            }
        });
    }

    private void showPasswordDialogForUpdateDelete(int position, FoodItem model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Admin Verification");
        View view = LayoutInflater.from(context).inflate(R.layout.admin_password_dialog, null);
        builder.setView(view);

        TextInputEditText etPassword = view.findViewById(R.id.etPassword);

        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = etPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(password) && password.equals("admin123")) {
                    // Password is correct, show update/delete dialog
                    showUpdateDeleteDialog(position, model);
                } else {
                    // Incorrect password
                    Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showUpdateDeleteDialog(int position, FoodItem model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Action");

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showUpdateItemDialog(position, model);
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(position);
            }
        });

        builder.show();
    }

    private void showUpdateItemDialog(int position, FoodItem model) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Update Food Item");
        View view = LayoutInflater.from(context).inflate(R.layout.add_food_item_form, null);
        dialogBuilder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etFoodName);
        TextInputEditText etDescription = view.findViewById(R.id.etFoodDescription);
        TextInputEditText etPrice = view.findViewById(R.id.etFoodPrice);

        etName.setText(model.getName());
        etDescription.setText(model.getDescription());
        etPrice.setText(String.valueOf(model.getPrice()));

        dialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String priceText = etPrice.getText().toString().trim();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(priceText)) {
                    double price = Double.parseDouble(priceText);
                    HashMap<String, Object> updates = new HashMap<>();
                    updates.put("name", name);
                    updates.put("description", description);
                    updates.put("price", price);

                    getRef(position).updateChildren(updates)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Food item updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.show();
    }

    private void deleteItem(int position) {
        getRef(position).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Food item deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showOrderFormDialog(FoodItem model) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Place Order");
        View view = LayoutInflater.from(context).inflate(R.layout.order_form, null, false);
        dialogBuilder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etUserName);
        TextInputEditText etAddress = view.findViewById(R.id.etUserAddress);
        TextInputEditText etEmail = view.findViewById(R.id.etUserEmail);
        TextInputEditText etPhone = view.findViewById(R.id.etUserPhone);

        AlertDialog dialog = dialogBuilder.create();

        view.findViewById(R.id.btnPlaceOrder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etName.getText().toString().trim();
                String userAddress = etAddress.getText().toString().trim();
                String userEmail = etEmail.getText().toString().trim();
                String userPhone = etPhone.getText().toString().trim();

                if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userAddress)
                        && !TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPhone)) {
                    Intent intent = new Intent(context, Place_order.class);
                    intent.putExtra("foodName", model.getName());
                    intent.putExtra("foodDescription", model.getDescription());
                    intent.putExtra("foodPrice", model.getPrice());
                    intent.putExtra("userName", userName);
                    intent.putExtra("userAddress", userAddress);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("userPhone", userPhone);
                    context.startActivity(intent);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void placeOrder(FoodItem model) {
        // Handle order placement logic here
        Toast.makeText(context, "Order placed successfully for " + model.getName(), Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodItemViewHolder(view);
    }

    static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_food_name);
            tvDescription = itemView.findViewById(R.id.tv_food_description);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
        }

        public void bind(FoodItem foodItem) {
            tvName.setText(foodItem.getName());
            tvDescription.setText(foodItem.getDescription());
            tvPrice.setText(String.valueOf(foodItem.getPrice()));
        }
    }
}
