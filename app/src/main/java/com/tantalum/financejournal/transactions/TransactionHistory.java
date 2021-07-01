package com.tantalum.financejournal.transactions;

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
import android.widget.TextView;
import android.widget.Toast;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.shreyaspatil.material.navigationview.MaterialNavigationView;
import com.tantalum.financejournal.About;
import com.tantalum.financejournal.CustomFilterArrayAdapter;
import com.tantalum.financejournal.DateTimeHandler;
import com.tantalum.financejournal.MainActivity;
import com.tantalum.financejournal.R;
import com.tantalum.financejournal.UpgradeToPro;
import com.tantalum.financejournal.accounts.AccountManager;
import com.tantalum.financejournal.reports.Reports;
import com.tantalum.financejournal.settings.Settings;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class TransactionHistory extends AppCompatActivity implements MaterialNavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    public static final String TAG = "TransactionHistory";
    private SharedPreferences sharedPref;
    private RecyclerView recyclerView;
    private TransactionsAdapter transactionsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TransactionsViewModel transactionsViewModel;
    private EditText etSearch;
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
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            View navHeader = navigationView.getHeaderView(0);
            TextView tvAppName = navHeader.findViewById(R.id.appName);
            tvAppName.setText(R.string.my_wallet_pro);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        String currency = sharedPref.getString("currency", "");
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
                    intent.putExtra("showPinScreen", false);
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
        recyclerView.scheduleLayoutAnimation();
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
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(TransactionHistory.this, "Process interrupted", Toast.LENGTH_SHORT).show();
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
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(TransactionHistory.this, "Process interrupted", Toast.LENGTH_SHORT).show();
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
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(TransactionHistory.this, "Process interrupted", Toast.LENGTH_SHORT).show();
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
                if (filteredList.isEmpty())
                    filteredList.addAll(transactionsList);
                break;
            }
            case 2: { //today
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty())
                    for (int i = 0; i < transactionsList.size(); i++)
                        checkingList.add(transactionsList.get(i));
                else
                    for (int i = 0; i < filteredList.size(); i++)
                        checkingList.add(filteredList.get(i));

                for (TransactionItem item : checkingList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getTimeInMillis());
                    if (dateTimeHandler.getDayOfYear() == LocalDate.now().getDayOfYear() && !containsItem(item))
                        filteredList.add(item);
                    if (dateTimeHandler.getDayOfYear() != LocalDate.now().getDayOfYear() && containsItem(item))
                        remove(item);
                }
                break;
            }
            case 3: { //yesterday
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty())
                    for (int i = 0; i < transactionsList.size(); i++)
                        checkingList.add(transactionsList.get(i));
                else
                    for (int i = 0; i < filteredList.size(); i++)
                        checkingList.add(filteredList.get(i));

                for (TransactionItem item : checkingList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getTimeInMillis());
                    if (dateTimeHandler.getDayOfYear() == LocalDate.now().minusDays(1).getDayOfYear() && !containsItem(item))
                        filteredList.add(item);
                    if (dateTimeHandler.getDayOfYear() != LocalDate.now().minusDays(1).getDayOfYear() && containsItem(item))
                        remove(item);
                }
                break;
            }
            case 4: { //this week
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty())
                    for (int i = 0; i < transactionsList.size(); i++)
                        checkingList.add(transactionsList.get(i));
                else
                    for (int i = 0; i < filteredList.size(); i++)
                        checkingList.add(filteredList.get(i));

                for (TransactionItem item : checkingList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getTimeInMillis());
                    if (dateTimeHandler.getWeekOfYear() == dateTimeHandler.getWeekOfYear(LocalDateTime.now()) && !containsItem(item))
                        filteredList.add(item);
                    if (dateTimeHandler.getWeekOfYear() != dateTimeHandler.getWeekOfYear(LocalDateTime.now()) && containsItem(item))
                        remove(item);
                }
                break;
            }
            case 5: { //last week
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty())
                    for (int i = 0; i < transactionsList.size(); i++)
                        checkingList.add(transactionsList.get(i));
                else
                    for (int i = 0; i < filteredList.size(); i++)
                        checkingList.add(filteredList.get(i));

                for (TransactionItem item : checkingList) {
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getTimeInMillis());
                    if (dateTimeHandler.getWeekOfYear() == dateTimeHandler.getWeekOfYear(LocalDateTime.now().minusDays(7)) && !containsItem(item))
                        filteredList.add(item);
                    if (dateTimeHandler.getWeekOfYear() != dateTimeHandler.getWeekOfYear(LocalDateTime.now().minusDays(7)) && containsItem(item))
                        remove(item);
                }
                break;
            }
            /*case 6: { //pick date
                Bundle bundle = new Bundle();
                bundle.putString("pickDate", "fromTransactionFilter");
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(bundle);
                datePicker.show(getSupportFragmentManager(), "date picker dialog");
                break;
            }*/
        }

        switch (type) {
            case 1: { //all
                if (filteredList.isEmpty())
                    filteredList.addAll(transactionsList);
                break;
            }
            case 2: { //incomes
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty())
                    for (int i = 0; i < transactionsList.size(); i++)
                        checkingList.add(transactionsList.get(i));
                else
                    for (int i = 0; i < filteredList.size(); i++)
                        checkingList.add(filteredList.get(i));

                for (TransactionItem item : checkingList) {
                    if (item.getPrefix().equals("+") && !containsItem(item))
                        filteredList.add(item);
                    if (!item.getPrefix().equals("+") && containsItem(item))
                        remove(item);
                }
                break;
            }
            case 3: { //expenses
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty())
                    for (int i = 0; i < transactionsList.size(); i++)
                        checkingList.add(transactionsList.get(i));
                else
                    for (int i = 0; i < filteredList.size(); i++)
                        checkingList.add(filteredList.get(i));

                for (TransactionItem item : checkingList) {
                    if (item.getPrefix().equals("-") && !containsItem(item))
                        filteredList.add(item);
                    if (!item.getPrefix().equals("-") && containsItem(item))
                        remove(item);
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
                if (filteredList.isEmpty())
                    filteredList.addAll(transactionsList);
                break;
            }
            case 2: { //date - descending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o2.getTimeInMillis()).compareTo(Double.valueOf(o1.getTimeInMillis()));
                        }
                    });
                else
                    Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o2.getTimeInMillis()).compareTo(Double.valueOf(o1.getTimeInMillis()));
                        }
                    });

                break;
            }
            case 3: { //date - ascending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o1.getTimeInMillis()).compareTo(Double.valueOf(o2.getTimeInMillis()));
                        }
                    });
                else
                    Collections.sort(transactionsList, new Comparator<TransactionItem>() {
                        @Override
                        public int compare(TransactionItem o1, TransactionItem o2) {
                            return Double.valueOf(o1.getTimeInMillis()).compareTo(Double.valueOf(o2.getTimeInMillis()));
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

        //if (date != 6)
        showResults();
    }

    private boolean containsItem(TransactionItem item) {
        return filteredList.stream().anyMatch(o -> o.getId() == item.getId());
    }

    private void remove(TransactionItem item) {
        for (Iterator<TransactionItem> iterator = filteredList.iterator(); iterator.hasNext(); ) {
            TransactionItem nextItem = iterator.next();
            if (nextItem.equals(item))
                iterator.remove();
        }
    }

    public void filterByDate(int date) {
        for (TransactionItem item : transactionsList) {
            DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getTimeInMillis());
            if (dateTimeHandler.getDayOfYear() == date && !filteredList.contains(item))
                filteredList.add(item);
        }
        showResults();
    }

    private void showResults() {
        transactionsAdapter.submitList(filteredList);
        mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
    }

    public void toggleFilters(View view) {
        view.animate().rotationBy(180f);

        MaterialCardView filtersCard = findViewById(R.id.filtersCard);
        if (filtersCard.getVisibility() == View.GONE)
            filtersCard.setVisibility(View.VISIBLE);
        else {
            filtersCard.setVisibility(View.GONE);
            dateSpinner.setSelection(0);
            typeSpinner.setSelection(0);
            sortSpinner.setSelection(0);
            transactionsList = transactionsViewModel.getTransactionsList();
            transactionsAdapter.submitList(transactionsList);
            mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
        }
    }
}