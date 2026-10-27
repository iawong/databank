package com.example.databank;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityManageCategoriesBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class ManageCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryDeleteListener {
    private DatabaseHelper db;
    private ArrayList<Integer> categoryIds;
    private ArrayList<String> categoryNames;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManageCategoriesBinding binding = ActivityManageCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);
        categoryIds = new ArrayList<>();
        categoryNames = new ArrayList<>();

        loadCategories();

        RecyclerView recyclerView = binding.categoriesRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(this, categoryIds, categoryNames, this);
        recyclerView.setAdapter(adapter);

        TextInputLayout inputLayout = binding.textInputNewCategory;
        TextInputEditText inputEditText = binding.newCategoryInput;
        Button addButton = binding.addCategoryButton;

        addButton.setOnClickListener(v -> {
            if (inputEditText.getText() == null) return;
            String name = inputEditText.getText().toString().trim();

            if (name.isEmpty()) {
                inputLayout.setError("Please enter a category name");
                return;
            }

            inputLayout.setError(null);

            long id = db.addCategory(name);

            if (id != -1) {
                categoryIds.add((int) id);
                categoryNames.add(name);
                adapter.notifyItemInserted(categoryNames.size() - 1);
                inputEditText.setText("");
                Toast.makeText(this, "Added category: " + name, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        Cursor cursor = db.getAllCategories();
        categoryIds.clear();
        categoryNames.clear();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                categoryIds.add(cursor.getInt(0));
                categoryNames.add(cursor.getString(1));
            }

            cursor.close();
        }
    }

    @Override
    public void onCategoryDelete(int position, int categoryId) {
        db.deleteCategory(categoryId);
        categoryIds.remove(position);
        categoryNames.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, "Deleted category", Toast.LENGTH_SHORT).show();
    }
}
