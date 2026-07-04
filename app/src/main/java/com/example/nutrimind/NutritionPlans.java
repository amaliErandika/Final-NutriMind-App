package com.example.nutrimind;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class NutritionPlans extends AppCompatActivity {

    Button btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday, btnSunday, btnBackToHome;
    BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_plans);

        // Initialize buttons
        btnMonday = findViewById(R.id.btnMonday);
        btnTuesday = findViewById(R.id.btnTuesday);
        btnWednesday = findViewById(R.id.btnWednesday);
        btnThursday = findViewById(R.id.btnThursday);
        btnFriday = findViewById(R.id.btnFriday);
        btnSaturday = findViewById(R.id.btnSaturday);
        btnSunday = findViewById(R.id.btnSunday);
        btnBackToHome = findViewById(R.id.btnBackToHome);  // Back to Home button

        // Set OnClickListeners for each button
        btnMonday.setOnClickListener(v -> showDietPlanDialog("Monday"));
        btnTuesday.setOnClickListener(v -> showDietPlanDialog("Tuesday"));
        btnWednesday.setOnClickListener(v -> showDietPlanDialog("Wednesday"));
        btnThursday.setOnClickListener(v -> showDietPlanDialog("Thursday"));
        btnFriday.setOnClickListener(v -> showDietPlanDialog("Friday"));
        btnSaturday.setOnClickListener(v -> showDietPlanDialog("Saturday"));
        btnSunday.setOnClickListener(v -> showDietPlanDialog("Sunday"));

        // Set OnClickListener for Back to Home button
        btnBackToHome.setOnClickListener(v -> {
            // Create an Intent to navigate back to HomeActivity
            Intent intent = new Intent(NutritionPlans.this, HomeActivity.class);
            startActivity(intent);
            finish();  // Optional: finish current activity so that it doesn't stay in the back stack
        });
    }

    private void showDietPlanDialog(String day) {
        // Inflate BottomSheet layout
        View bottomSheetView = LayoutInflater.from(NutritionPlans.this)
                .inflate(R.layout.dialogdietplan, null);

        // Get references to TextViews in the BottomSheet layout
        TextView tvDietPlanTitle = bottomSheetView.findViewById(R.id.tvDietPlanTitle);
        TextView tvDietPlanDetails = bottomSheetView.findViewById(R.id.tvDietPlanDetails);

        // Set the title and details based on the clicked day
        tvDietPlanTitle.setText("Diet Plan for " + day);

        // Set specific details based on the day
        switch (day) {
            case "Monday":
                tvDietPlanDetails.setText("Breakfast: String hoppers with coconut sambol & dhal curry\nLunch: Rice & curry (Red rice, fish curry, mallung, dhal, papadam)\nDinner: Pittu with coconut milk");
                break;
            case "Tuesday":
                tvDietPlanDetails.setText("Breakfast: Kiribath (milk rice) with lunumiris\nLunch: Rice & curry (white rice, chicken curry, beetroot curry, gotukola sambol)\nDinner: Thosai with sambhar & coconut chutney");
                break;
            case "Wednesday":
                tvDietPlanDetails.setText("Breakfast: Pol roti with lunu miris & boiled eggs\nLunch: Rice & curry (Red rice, dried fish curry, pumpkin curry, cucumber salad)\nDinner: Hoppers with coconut sambol & chicken curry");
                break;
            case "Thursday":
                tvDietPlanDetails.setText("Breakfast: Mung kiribath (green gram milk rice) with kithul treacle\nLunch: Rice & curry (White rice, fish ambul thiyal, okra curry, carrot sambol)\nDinner: Chapati with dhal & brinjal moju");
                break;
            case "Friday":
                tvDietPlanDetails.setText("Breakfast: String hoppers with coconut sambol & dhal curry\nLunch: Rice & curry (Red rice, prawn curry, cabbage mallung, green beans curry)\nDinner: Parippu (lentil soup) with roast paan (Sri Lankan bread)");
                break;
            case "Saturday":
                tvDietPlanDetails.setText("Breakfast: Dosa with potato curry & coconut chutney\nLunch: Rice & curry (White rice, egg curry, bitter gourd stir-fry, jackfruit mallung\nDinner: Kottu roti with chicken & vegetables");
                break;
            case "Sunday":
                tvDietPlanDetails.setText("Breakfast: Pittu with coconut milk & banana\nLunch: Rice & curry (Red rice, crab curry, winged bean sambol, cucumber raita)\nDinner: String hoppers with seeni sambol & soya curry");
                break;
        }

        // Show the BottomSheetDialog
        bottomSheetDialog = new BottomSheetDialog(NutritionPlans.this);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
