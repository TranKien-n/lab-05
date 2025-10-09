package com.example.listycity5;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivityStarter extends AppCompatActivity {
    ListView cityList;
    ArrayList<City> cityDataList;
    CityArrayAdapter cityArrayAdapter;
    private Button addCityButton;
    private EditText addCityEditText;
    private EditText addProvinceEditText;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addCityEditText = findViewById(R.id.city_name_edit);
        addProvinceEditText = findViewById(R.id.province_name_edit);
        addCityButton = findViewById(R.id.add_city_button);

        cityList = findViewById(R.id.city_list);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        cityDataList = new ArrayList<>();

        cityArrayAdapter = new CityArrayAdapter(this, cityDataList);
        cityList.setAdapter(cityArrayAdapter);

        addCityButton.setOnClickListener(v -> {
            String cityName = addCityEditText.getText().toString().trim();
            String provinceName = addProvinceEditText.getText().toString().trim();

            if (!cityName.isEmpty() && !provinceName.isEmpty()) {
                City newCity = new City(cityName, provinceName);
                addNewCity(newCity);

                addCityEditText.setText("");
                addProvinceEditText.setText("");
            }
        });

        citiesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    cityDataList.clear();
                    for (QueryDocumentSnapshot doc: querySnapshots) {
                        String city = doc.getId();
                        String province = doc.getString("Province");
                        Log.d("Firestore", String.format("City(%s, %s) fetched", city, province));
                        cityDataList.add(new City(city, province));
                    }
                    cityArrayAdapter.notifyDataSetChanged();
                }
            }
        })  ;

        // To delete a city from the list, we use a long click listener on a list item to show a confirmation dialog.
        cityList.setOnItemLongClickListener((parent, view, position, id) -> {
            City cityToDelete = cityDataList.get(position); // Get the city to delete by position
            // Show a confirmation dialog before deleting (AlertDialog)
            new AlertDialog.Builder(this)
                    .setTitle("Delete cityToDelete")
                    .setMessage("Delete " + cityToDelete.getCityName() + ", " + cityToDelete.getProvinceName() + "?")
                    .setPositiveButton("Delete", (d, w) -> deleteCity(cityToDelete))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

    }
    private void addNewCity(City city) {
        cityDataList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        HashMap<String, String> data = new HashMap<>();
        data.put("Province", city.getProvinceName());

        citiesRef.document(city.getCityName()).set(data);
    }

    private void deleteCity(City city) {
        // delete from local list
        cityDataList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();

        // delete from Firestore collection
        citiesRef.document(city.getCityName()).delete();
    }
}