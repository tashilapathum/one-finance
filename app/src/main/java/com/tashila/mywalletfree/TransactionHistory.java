package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TransactionHistory extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    public static final String TAG = "TransactionHistory";
    SharedPreferences sharedPref;
    private RecyclerView recyclerView;
    private TransactionsAdapter transactionsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TransactionsViewModel transactionsViewModel;
    private EditText etSearch;
    private String currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*------------------------------Essential for every activity------------------------------*/
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppThemeDark);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_trans_history);
            View layout = findViewById(R.id.drawer_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_trans_history);
            toolbar = findViewById(R.id.toolbar);
        }

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        currency = sharedPref.getString("currency", "");
        loadFilters();
        loadItems();
        etSearch = findViewById(R.id.etSearch);
        implementSearch();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final TransactionItem transactionItem = transactionsAdapter.getTransactionItemAt(viewHolder.getAdapterPosition());
                Snackbar snackbar = Snackbar.make(recyclerView, R.string.deleted, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                transactionsAdapter.notifyDataSetChanged();
                            }
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
                                    deleteTransaction(transactionItem);
                            }
                        });
                snackbar.show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override //so the language change works with dark mode
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_recent_trans: {
                Intent intent = new Intent(this, TransactionHistory.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_reports: {
                Intent intent = new Intent(this, Reports.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_get_pro: {
                Intent intent = new Intent(this, UpgradeToPro.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_about: {
                DialogAbout dialogAbout = new DialogAbout();
                dialogAbout.show(getSupportFragmentManager(), "about dialog");
                break;
            }
            case R.id.nav_exit: {
                sharedPref.edit().putBoolean("exit", true).apply();
                finishAndRemoveTask();
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!sharedPref.getBoolean("insTransactionsShown", false)) {
            new BubbleShowCaseBuilder(this)
                    .title(getString(R.string.transactions))
                    .description(getString(R.string.transactions_help))
                    .show();
            sharedPref.edit().putBoolean("insTransactionsShown", true).apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("exit", false).apply();
    }

    private void loadItems() {
        transactionsAdapter = new TransactionsAdapter(this);
        recyclerView = findViewById(R.id.THRecyclerView);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(transactionsAdapter);

        transactionsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(TransactionsViewModel.class);
        transactionsViewModel.getAllTransactionItems().observe(this, new Observer<List<TransactionItem>>() {
            @Override
            public void onChanged(List<TransactionItem> transactionItems) {
                transactionsAdapter.submitList(transactionItems);
            }
        });

        transactionsAdapter.setOnTransactionClickListener(new TransactionsAdapter.OnTransactionClickListener() {
            @Override
            public void OnTransactionClick(TransactionItem transactionItem) {
                String amount = transactionItem.getAmount();
                String description = transactionItem.getDescription();
                String date = new DateTimeHandler(transactionItem.getUserDate()).getTimestamp().split(" ")[0];

                Bundle bundle = new Bundle();
                bundle.putString("amount", amount);
                bundle.putString("description", description);
                bundle.putString("date", date);

                DialogTransactionEditor transactionEditor = new DialogTransactionEditor(TransactionHistory.this, transactionItem);
                transactionEditor.setArguments(bundle);
                transactionEditor.show(getSupportFragmentManager(), "transaction editor dialog");
            }
        });
    }

    private void implementSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    filter(editable.toString());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void filter(String text) throws ExecutionException, InterruptedException {
        List<TransactionItem> fullList = transactionsViewModel.getTransactionsList();
        ArrayList<TransactionItem> filteredList = new ArrayList<>();
        for (TransactionItem item : fullList) {
            if (item.getDescription().toLowerCase().contains(text.toLowerCase()) || item.getUserDate().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        transactionsAdapter.submitList(filteredList);
    }

    public void updateTransaction(TransactionItem transactionItem) {
        transactionsViewModel.update(transactionItem);
        transactionsAdapter.notifyDataSetChanged();
        Toast.makeText(this, R.string.updated, Toast.LENGTH_SHORT).show();
    }

    private void deleteTransaction(TransactionItem transactionItem) {
        transactionsViewModel.delete(transactionItem);
        String prefix = transactionItem.getPrefix();
        double balance = Double.parseDouble(sharedPref.getString("balance", "0"));
        double amount = Double.parseDouble(transactionItem.getAmount());
        if (prefix.equals("+"))
            balance = balance - amount;
        else
            balance = balance + amount;
        DecimalFormat df = new DecimalFormat("#.00");
        String newBalance = df.format(balance);
        sharedPref.edit().putString("balance", newBalance).apply();
        transactionsAdapter.notifyDataSetChanged();
    }

    private void loadFilters() {
        //date
        Spinner dateSpinner = findViewById(R.id.date_spinner);
        String[] dateList = getResources().getStringArray(R.array.date_list);
        CustomArrayAdapter dateAdapter = new CustomArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dateList);
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();
                List<TransactionItem> filteredList = new ArrayList<>();
                switch (position) {
                    case 1: { //all
                        transactionsAdapter.submitList(transactionsViewModel.getTransactionsList());
                        break;
                    }
                    case 2: { //today
                        for (int i = 0; i < transactionsList.size(); i++) {
                            DateTimeHandler dateTimeHandler = new DateTimeHandler(transactionsList.get(i).getUserDate());
                            if (dateTimeHandler.getDayOfYear() == LocalDate.now().getDayOfYear())
                                filteredList.add(transactionsList.get(i));
                        }
                        transactionsAdapter.submitList(filteredList);
                        break;
                    }
                    case 3: { //yesterday
                        for (int i = 0; i < transactionsList.size(); i++) {
                            DateTimeHandler dateTimeHandler = new DateTimeHandler(transactionsList.get(i).getUserDate());
                            if (dateTimeHandler.getDayOfYear() == LocalDate.now().minusDays(1).getDayOfYear())
                                filteredList.add(transactionsList.get(i));
                        }
                        transactionsAdapter.submitList(filteredList);
                        break;
                    }
                    case 4: { //this week
                        for (int i = 0; i < transactionsList.size(); i++) {
                            DateTimeHandler dateTimeHandler = new DateTimeHandler(transactionsList.get(i).getUserDate());
                            if (dateTimeHandler.getWeek() == dateTimeHandler.getWeek(LocalDateTime.now()))
                                filteredList.add(transactionsList.get(i));
                        }
                        transactionsAdapter.submitList(filteredList);
                        break;
                    }
                    case 5: { //last week
                        for (int i = 0; i < transactionsList.size(); i++) {
                            DateTimeHandler dateTimeHandler = new DateTimeHandler(transactionsList.get(i).getUserDate());
                            if (dateTimeHandler.getWeek() == dateTimeHandler.getWeek(LocalDateTime.now().minusDays(7)))
                                filteredList.add(transactionsList.get(i));
                        }
                        transactionsAdapter.submitList(filteredList);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //type
        Spinner typeSpinner = findViewById(R.id.type_spinner);
        String[] typeList = getResources().getStringArray(R.array.type_list);
        CustomArrayAdapter typeAdapter = new CustomArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeList);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();
                List<TransactionItem> filteredList = new ArrayList<>();
                switch (position) {
                    case 1: { //all
                        transactionsAdapter.submitList(transactionsViewModel.getTransactionsList());
                        break;
                    }
                    case 2: { //incomes
                        for (int i = 0; i < transactionsList.size(); i++) {
                            if (transactionsList.get(i).getPrefix().equals("+"))
                                filteredList.add(transactionsList.get(i));
                        }
                        transactionsAdapter.submitList(filteredList);
                        break;
                    }
                    case 3: { //expenses
                        for (int i = 0; i < transactionsList.size(); i++) {
                            if (transactionsList.get(i).getPrefix().equals("-"))
                                filteredList.add(transactionsList.get(i));
                        }
                        transactionsAdapter.submitList(filteredList);
                        break;
                    }/*
                    case 4: { //bank
                        for (int i=0; i<transactionsList.size(); i++) {
                            if (transactionsList.get(i).isBankRelated())
                                filteredList.add(transactionsList.get(i));
                        }
                        transactionsAdapter.submitList(filteredList);
                        break;
                    }*/
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //sort
        Spinner sortSpinner = findViewById(R.id.sort_spinner);
        String[] sortList = getResources().getStringArray(R.array.sort_list);
        CustomArrayAdapter sortAdapter = new CustomArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortList);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();
                switch (position) {
                    case 1: { //all
                        transactionsAdapter.submitList(transactionsViewModel.getTransactionsList());
                        break;
                    }
                    case 2: { //date - descending
                        Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                            @Override
                            public int compare(TransactionItem o1, TransactionItem o2) {
                                return Double.valueOf(o2.getUserDate()).compareTo(Double.valueOf(o1.getUserDate()));
                            }
                        });
                        break;
                    }
                    case 3: { //date - ascending
                        Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                            @Override
                            public int compare(TransactionItem o1, TransactionItem o2) {
                                return Double.valueOf(o1.getUserDate()).compareTo(Double.valueOf(o2.getUserDate()));
                            }
                        });
                        break;
                    }
                    case 4: { //amount - descending
                        Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                            @Override
                            public int compare(TransactionItem o1, TransactionItem o2) {
                                return Double.valueOf(o2.getAmount()).compareTo(Double.valueOf(o1.getAmount()));
                            }
                        });
                        break;
                    }
                    case 5: { //amount - ascending
                        Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                            @Override
                            public int compare(TransactionItem o1, TransactionItem o2) {
                                return Double.valueOf(o1.getAmount()).compareTo(Double.valueOf(o2.getAmount()));
                            }
                        });
                        break;
                    }
                }
                transactionsAdapter.submitList(transactionsList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}