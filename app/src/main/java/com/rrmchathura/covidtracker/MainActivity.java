package com.rrmchathura.covidtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.hbb20.CountryCodePicker;
import com.rrmchathura.covidtracker.databinding.ActivityMainBinding;

import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ActivityMainBinding binding;
    String[] types = {"cases","deaths","recovered","active"};
    private List<ModelClass> modelClassList;
    private List<ModelClass> modelClassList2;
    com.rrmchathura.covidtracker.Adapter adapter;
    String country;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        modelClassList = new ArrayList<>();
        modelClassList2 = new ArrayList<>();

        binding.spinner.setOnItemSelectedListener(this);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        binding.spinner.setAdapter(arrayAdapter);
        binding.spinner.setSelection(0,true);

        ApiUtilities.getApiInterface().getCountryData().enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {
                modelClassList2.addAll(response.body());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {

            }
        });

        adapter = new Adapter(this,modelClassList2);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(adapter);

        binding.ccp.setAutoDetectedCountry(true);
        country = binding.ccp.getSelectedCountryName();
        binding.ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                country = binding.ccp.getSelectedCountryName();
                fetchData();
            }
        });

        fetchData();

    }

    private void fetchData() {

        ApiUtilities.getApiInterface().getCountryData().enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {

                modelClassList.addAll(response.body());
                for (int i=0;i<modelClassList.size();i++){
                    if (modelClassList.get(i).getCountry().equals(country)){
                        binding.activecase.setText((modelClassList.get(i).getActive()));
                        binding.todaydeath.setText((modelClassList.get(i).getTodayDeaths()));
                        binding.todayrecover.setText((modelClassList.get(i).getTodayRecovered()));
                        binding.todaytotal.setText((modelClassList.get(i).getTodayCases()));
                        binding.totalcase.setText((modelClassList.get(i).getCases()));
                        binding.totaldeaths.setText((modelClassList.get(i).getTodayDeaths()));
                        binding.recoverdcase.setText((modelClassList.get(i).getRecovered()));

                        int active,total,recovered,deaths;

                        active = Integer.parseInt(modelClassList.get(i).getActive());
                        total = Integer.parseInt(modelClassList.get(i).getCases());
                        recovered = Integer.parseInt(modelClassList.get(i).getRecovered());
                        deaths = Integer.parseInt(modelClassList.get(i).getDeaths());

                        updateGraph(active,total,recovered,deaths);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {

            }
        });
    }

    private void updateGraph(int active, int total, int recovered, int deaths) {

        binding.piechart.clearChart();
        binding.piechart.addPieSlice(new PieModel("confirm",total, Color.parseColor("#FFB701")));
        binding.piechart.addPieSlice(new PieModel("Active",active, Color.parseColor("#FF4CAF50")));
        binding.piechart.addPieSlice(new PieModel("Recovered",recovered, Color.parseColor("#38ACCD")));
        binding.piechart.addPieSlice(new PieModel("Deaths",deaths, Color.parseColor("#F55C47")));
        binding.piechart.startAnimation();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = types[i];
        binding.filter.setText(item);
        adapter.filter(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}