package com.example.quickbite;


import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickbite.FoodItem;
import com.example.quickbite.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class activity_home extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private DatabaseReference reference;
    private FloatingActionButton fabAddItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance().getReference().child("foodItems");

        FirebaseRecyclerOptions<FoodItem> options =
                new FirebaseRecyclerOptions.Builder<FoodItem>()
                        .setQuery(reference, FoodItem.class)
                        .build();

        adapter = new FoodItemAdapter(options, this);
        recyclerView.setAdapter(adapter);

        fabAddItem = findViewById(R.id.fab_add);
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordDialogForAdding();
            }
        });
    }

    private void showPasswordDialogForAdding() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Verification");
        View view = getLayoutInflater().inflate(R.layout.admin_password_dialog, null);
        builder.setView(view);

        TextInputEditText etPassword = view.findViewById(R.id.etPassword);

        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = etPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(password) && password.equals("admin123")) {
                    // Password is correct, show add item dialog
                    showAddItemDialog();
                } else {
                    // Incorrect password
                    Toast.makeText(activity_home.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
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

    private void showAddItemDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Add New Food Item");
        View view = getLayoutInflater().inflate(R.layout.add_food_item_form, null);
        dialogBuilder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etFoodName);
        TextInputEditText etDescription = view.findViewById(R.id.etFoodDescription);
        TextInputEditText etPrice = view.findViewById(R.id.etFoodPrice);

        dialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String priceText = etPrice.getText().toString().trim();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(priceText)) {
                    double price = Double.parseDouble(priceText);
                    FoodItem newItem = new FoodItem(name, description, price);
                    reference.push().setValue(newItem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(activity_home.this, "Food item added", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(activity_home.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(activity_home.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Search food items...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFoodItems(newText);
                return true;
            }
        });

        return true;
    }

    private void searchFoodItems(String query) {
        FirebaseRecyclerOptions<FoodItem> options =
                new FirebaseRecyclerOptions.Builder<FoodItem>()
                        .setQuery(reference.orderByChild("name").startAt(query).endAt(query + "\uf8ff"), FoodItem.class)
                        .build();

        adapter.updateOptions(options);
        adapter.notifyDataSetChanged(); // Add this line
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
