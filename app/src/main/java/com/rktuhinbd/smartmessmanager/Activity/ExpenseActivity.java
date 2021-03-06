package com.rktuhinbd.smartmessmanager.Activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rktuhinbd.smartmessmanager.Adapter.ExpenseRecyclerAdapter;
import com.rktuhinbd.smartmessmanager.Database.DatabaseHelper;
import com.rktuhinbd.smartmessmanager.Dialog.AddExpenseDialog;
import com.rktuhinbd.smartmessmanager.Dialog.ExpenseInfoBottomSheet;
import com.rktuhinbd.smartmessmanager.Listener.AddExpenseDialogListener;
import com.rktuhinbd.smartmessmanager.Model.Rents;
import com.rktuhinbd.smartmessmanager.R;
import com.rktuhinbd.smartmessmanager.Utility.Keys;
import com.rktuhinbd.smartmessmanager.Utility.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class ExpenseActivity extends AppCompatActivity implements AddExpenseDialogListener, ExpenseInfoBottomSheet.BottomSheetListener {

    private PieChart pieChart;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private ExpenseRecyclerAdapter expenseRecyclerAdapter;
    private LinearLayoutManager layoutManager;

    private DatabaseHelper databaseHelper;
    private SharedPrefs sharedPrefs;
    private ArrayList<Rents> rents;
    private List<PieEntry> pieEntries;
    private PieData pieData;

    private String rentId, rentCategory, rentDescription, rentDate;
    private int rentAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.expenses);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initiateProperties();
        initiateRecyclerView();
        initiatePieChart();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenAddRentDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //Initiate properties
    private void initiateProperties() {
        recyclerView = findViewById(R.id.recyclerView_expense);
        fab = findViewById(R.id.fab);
        pieChart = findViewById(R.id.pieChart_rents);

        rents = new ArrayList<>();
        pieEntries = new ArrayList<>();

        sharedPrefs = new SharedPrefs(this);
        rentDate = sharedPrefs.getSharedPrefDataString(Keys.MONTH);

        databaseHelper = new DatabaseHelper(this);
    }

    //Initiate RecyclerView
    private void initiateRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        rents = databaseHelper.getRents(rentDate);
        int totalRent = 0;
        for (int i = 0; i < rents.size(); i++) {
            totalRent += rents.get(i).getRentAmount();
        }
        sharedPrefs.setSharedPrefDataInt(Keys.TOTAL_RENT, totalRent);

        expenseRecyclerAdapter = new ExpenseRecyclerAdapter(this, rents);
        recyclerView.setAdapter(expenseRecyclerAdapter);

        expenseRecyclerAdapter.setOnItemClickListener(new ExpenseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                rentId = rents.get(position).getRentId();
                rentCategory = rents.get(position).getRentCategory();
                rentAmount = rents.get(position).getRentAmount();
                rentDescription = rents.get(position).getRentDescription();
                rentDetailsBottomSheet(position);
            }
        });
    }

    //Initiate Rent Pie Chart
    private void initiatePieChart() {
        for (int i = 0; i < rents.size(); i++) {
            pieEntries.add(new PieEntry(rents.get(i).getRentAmount(), rents.get(i).getRentCategory()));
        }
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setSliceSpace(1f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pieDataSet.setValueTypeface(getResources().getFont(R.font.arima_madurai_bold));
        }
//        pieDataSet.setDrawValues(false);
        pieDataSet.setValueTextColor(Color.WHITE);      //Set Pie chart value text color
        pieDataSet.setValueTextSize(12f);
        pieData = new PieData(pieDataSet);

        pieChart.setDescription(null);                  //Hide description label in Pie chart

        pieChart.setDrawSliceText(false);               //Hide Pie chart text
        pieChart.animateX(1000);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    //Open bottom sheet to show rent details
    private void rentDetailsBottomSheet(int position) {
        ExpenseInfoBottomSheet bottomSheet = new ExpenseInfoBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putInt(Keys.POSITION, position);
        bundle.putString(Keys.ID, rentId);
        bundle.putString(Keys.RENT_CATEGORY, rentCategory);
        bundle.putInt(Keys.RENT_AMOUNT, rentAmount);
        bundle.putString(Keys.RENT_DATE, rentDate);
        bundle.putString(Keys.RENT_DESCRIPTION, rentDescription);
        bottomSheet.setArguments(bundle);
        bottomSheet.show(getSupportFragmentManager(), "Rent info bottom sheet");
    }

    //Update Rent Pie Chart
    public void updatePieChart(int amount, String rentCategory) {
        pieEntries.add(new PieEntry(amount, rentCategory));

        pieChart.animateX(1000);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    //Open Add rent dialog
    private void OpenAddRentDialog() {
        AddExpenseDialog dialog = new AddExpenseDialog();
        dialog.setCancelable(true);
        dialog.setDialogListener(this);
        dialog.show(getSupportFragmentManager(), "Add rent");
    }

    //Store rent information to database if any value is saved in dialog
    @Override
    public void stateChanged(boolean updateToken, int amount, String category, String description, String rentDate) {
        if (updateToken) {
            databaseHelper.addRent(amount, category, description, rentDate);
            updatePieChart(amount, category);
            initiateRecyclerView();
        }
    }

    //Bottom sheet
    @Override
    public void onBottomSheetItemClick(String key, int position) {
        if (key.equals("updated")) {
            pieEntries.clear();
            rents.clear();
            rents.addAll(databaseHelper.getRents(rentDate));
            expenseRecyclerAdapter.notifyDataSetChanged();

            int totalRent = 0;
            for (int i = 0; i < rents.size(); i++) {
                totalRent += rents.get(i).getRentAmount();
                updatePieChart(rents.get(i).getRentAmount(), rents.get(i).getRentCategory());
            }
            if (pieEntries.size() == 0) {
                pieChart.setVisibility(View.GONE);
            } else {
                pieChart.setVisibility(View.VISIBLE);
            }
            sharedPrefs.setSharedPrefDataInt(Keys.TOTAL_RENT, totalRent);
        } else {
            rents.remove(position);
            expenseRecyclerAdapter.notifyItemRemoved(position);

            pieEntries.clear();
            int totalRent = 0;
            for (int i = 0; i < rents.size(); i++) {
                totalRent += rents.get(i).getRentAmount();
                updatePieChart(rents.get(i).getRentAmount(), rents.get(i).getRentCategory());
            }
            if (pieEntries.size() == 0) {
                pieChart.setVisibility(View.GONE);
            } else {
                pieChart.setVisibility(View.VISIBLE);
            }
            sharedPrefs.setSharedPrefDataInt(Keys.TOTAL_RENT, totalRent);
        }
    }
}
