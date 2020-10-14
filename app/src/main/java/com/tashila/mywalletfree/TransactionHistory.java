package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
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
import com.google.android.material.snackbar.Snackbar;
import com.shreyaspatil.material.navigationview.MaterialNavigationView;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

public class TransactionHistory extends AppCompatActivity implements MaterialNavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    public static final String TAG = "TransactionHistory";
    SharedPreferences sharedPref;
    private RecyclerView recyclerView;
    private TransactionsAdapter transactionsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TransactionsViewModel transactionsViewModel;
    private EditText etSearch;
    private String currency;
    private Spinner dateSpinner;
    private Spinner typeSpinner;
    private Spinner sortSpinner;
    private int date;
    private int type;
    private int sort;
    List<TransactionItem> transactionsList;
    List<TransactionItem> filteredList;
    private MaterialNavigationView navigationView;


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
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        currency = sharedPref.getString("currency", "");
        loadItems();
        loadFilters();
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
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_home)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_recent_trans: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_recent_trans)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, TransactionHistory.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_reports: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_reports)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, Reports.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_accounts: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_accounts)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, AccountManager.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_settings: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_settings)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, Settings.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_get_pro: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_get_pro)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, UpgradeToPro.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_about: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_about)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, About.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_exit: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_exit)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    sharedPref.edit().putBoolean("exit", true).apply();
                    finishAndRemoveTask();
                }
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
        navigationView.setCheckedItem(R.id.nav_recent_trans);
        if (sharedPref.getBoolean("exit", false))
            finishAndRemoveTask();
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
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
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
                DialogTransactionEditor transactionEditor = new DialogTransactionEditor(TransactionHistory.this, transactionItem);
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
                String text = editable.toString();
                List<TransactionItem> fullList = transactionsViewModel.getTransactionsList();
                ArrayList<TransactionItem> filteredList = new ArrayList<>();
                for (TransactionItem item : fullList) {
                    if (item.getDescription().toLowerCase().contains(text.toLowerCase()) || item.getAmount().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                transactionsAdapter.submitList(filteredList);
            }
        });
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
        dateSpinner = findViewById(R.id.date_spinner);
        typeSpinner = findViewById(R.id.type_spinner);
        sortSpinner = findViewById(R.id.sort_spinner);

        //date
        String[] dateList = getResources().getStringArray(R.array.date_list);
        CustomFilterArrayAdapter dateAdapter = new CustomFilterArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dateList);
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    try {
                        typeSpinner.setSelection(0);
                        sortSpinner.setSelection(0);
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(TransactionHistory.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //type
        String[] typeList = getResources().getStringArray(R.array.type_list);
        CustomFilterArrayAdapter typeAdapter = new CustomFilterArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeList);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    try {
                        dateSpinner.setSelection(0);
                        sortSpinner.setSelection(0);
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(TransactionHistory.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //sort
        String[] sortList = getResources().getStringArray(R.array.sort_list);
        CustomFilterArrayAdapter sortAdapter = new CustomFilterArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortList);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    try {
                        dateSpinner.setSelection(0);
                        typeSpinner.setSelection(0);
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(TransactionHistory.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void filter() {
        date = dateSpinner.getSelectedItemPosition();
        type = typeSpinner.getSelectedItemPosition();
        sort = sortSpinner.getSelectedItemPosition();

        transactionsList = transactionsViewModel.getTransactionsList();
        filteredList = new ArrayList<>();
        switch (date) {
            case 1: { //all
                filteredList.addAll(transactionsList);
                break;
            }
            case 2: { //today
                for (TransactionItem item : transactionsList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
                    if (dateTimeHandler.getDayOfYear() == LocalDate.now().getDayOfYear() && !containsItem(item))
                        filteredList.add(item);
                    else
                        filteredList.remove(item);
                }
                break;
            }
            case 3: { //yesterday
                for (TransactionItem item : transactionsList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
                    if (dateTimeHandler.getDayOfYear() == LocalDate.now().minusDays(1).getDayOfYear() && !containsItem(item))
                        filteredList.add(item);
                    else
                        filteredList.remove(item);
                }
                break;
            }
            case 4: { //this week
                for (TransactionItem item : transactionsList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
                    if (dateTimeHandler.getWeekOfYear() == dateTimeHandler.getWeekOfYear(LocalDateTime.now()) && !containsItem(item))
                        filteredList.add(item);
                    else
                        filteredList.remove(item);
                }
                break;
            }
            case 5: { //last week
                for (TransactionItem item : transactionsList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
                    if (dateTimeHandler.getWeekOfYear() == dateTimeHandler.getWeekOfYear(LocalDateTime.now().minusDays(7)) && !containsItem(item))
                        filteredList.add(item);
                    else
                        filteredList.remove(item);
                }
                break;
            }
            case 6: { //pick date
                Bundle bundle = new Bundle();
                bundle.putString("pickDate", "fromTransactionFilter");
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(bundle);
                datePicker.show(getSupportFragmentManager(), "date picker dialog");
                break;
            }
        }

        switch (type) {
            case 1: { //all
                filteredList.addAll(transactionsList);
                break;
            }
            case 2: { //incomes
                for (TransactionItem item : transactionsList) {
                    if (item.getPrefix().equals("+") && !containsItem(item))
                        filteredList.add(item);
                    else
                        filteredList.remove(item);
                }
                break;
            }
            case 3: { //expenses
                for (TransactionItem item : transactionsList) {
                    if (item.getPrefix().equals("-") && !containsItem(item))
                        filteredList.add(item);
                    else
                        filteredList.remove(item);
                }
                break;
            }
            /*case 4: { //bank
                for (TransactionItem item : transactionsList) {
                    if (item.isBankRelated())
                        filteredList.add(item);
                }
                transactionsAdapter.submitList(filteredList);
                break;
            }*/
        }

        switch (sort) {
            case 1: { //added order
                filteredList.addAll(transactionsList);
                break;
            }
            case 2: { //date - descending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o2.getUserDate()).compareTo(Double.valueOf(o1.getUserDate()));
                        }
                    });
                else
                    Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o2.getUserDate()).compareTo(Double.valueOf(o1.getUserDate()));
                        }
                    });

                break;
            }
            case 3: { //date - ascending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o1.getUserDate()).compareTo(Double.valueOf(o2.getUserDate()));
                        }
                    });
                else
                    Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o1.getUserDate()).compareTo(Double.valueOf(o2.getUserDate()));
                        }
                    });

                break;
            }
            case 4: { //amount - descending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o2.getAmount()).compareTo(Double.valueOf(o1.getAmount()));
                        }
                    });
                else
                    Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o2.getAmount()).compareTo(Double.valueOf(o1.getAmount()));
                        }
                    });
                break;
            }
            case 5: { //amount - ascending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o1.getAmount()).compareTo(Double.valueOf(o2.getAmount()));
                        }
                    });
                else
                    Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o1.getAmount()).compareTo(Double.valueOf(o2.getAmount()));
                        }
                    });
                break;
            }
        }

        if (date != 6)
            showResults();
    }

    private boolean containsItem(TransactionItem item) {
        return filteredList.stream().anyMatch(o -> o.getId() == item.getId());
    }

    public void filterByDate(int date) {
        for (TransactionItem item : transactionsList) {
            DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
            if (dateTimeHandler.getDayOfYear() == date && !filteredList.contains(item))
                filteredList.add(item);
            else
                filteredList.remove(item);
        }
        showResults();
    }

    private void showResults() {
        if (filteredList.isEmpty())
            if (sort > 0)
                transactionsAdapter.submitList(transactionsList);
            else
                Toast.makeText(this, "No results", Toast.LENGTH_SHORT).show();
        else
            transactionsAdapter.submitList(filteredList);
    }
}

//TODO: add multiple filter (use ELSE to fix duplicate items!!)