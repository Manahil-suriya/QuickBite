package com.example.quickbite;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Place_order extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        TextView tvOrderMessage = findViewById(R.id.tvOrderMessage);
        Button btnGetMore = findViewById(R.id.btnGetMore);

        // Get the food item and user details from the intent
        String foodName = getIntent().getStringExtra("foodName");
        String userName = getIntent().getStringExtra("userName");

        // Display the order message
        tvOrderMessage.setText("Hey " + userName + ", your order for " + foodName + " has been placed successfully!");

        // Show a toast message
        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

        // Set a click listener for the "GET more" button
        btnGetMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the home activity
                Intent intent = new Intent(Place_order.this, activity_home.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
