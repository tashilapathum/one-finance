package com.tantalum.onefinance.transactions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tantalum.onefinance.CustomFilterArrayAdapter;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.categories.CategoriesManager;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class TransactionsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public static final String TAG = "TransactionsFragment";
    private View view;
    private RecyclerView recyclerView;
    private TransactionsAdapter transactionsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TransactionsViewModel transactionsViewModel;
    private SharedPreferences sharedPref;
    private EditText etSearch;
    private Spinner dateSpinner;
    private Spinner typeSpinner;
    private Spinner sortSpinner;
    private Spinner categorySpinner;
    private int date;
    private int type;
    private int sort;
    private int category;
    private List<TransactionItem> transactionsList;
    private List<TransactionItem> filteredList;
    private static TransactionsFragment instance;
    private int updatingPosition = 0;
    private boolean animationShown = false;
    private String[] categoriesList;

    public static TransactionsFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_transactions, null);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);
        instance = this;

        loadItems();
        loadFilters();
        etSearch = view.findViewById(R.id.etSearch);
        implementSearch();
        view.findViewById(R.id.toggleFilters).setOnClickListener(v -> {
            v.animate().rotationBy(180f);
            toggleFilters(v);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final TransactionItem transactionItem = transactionsAdapter.getTransactionItemAt(viewHolder.getAdapterPosition());
                Snackbar snackbar = Snackbar.make(recyclerView, R.string.deleted, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, view -> transactionsAdapter.notifyDataSetChanged())
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
        return view;
    }

    private void loadItems() {
        transactionsAdapter = new TransactionsAdapter(getActivity());
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(transactionsAdapter);
        recyclerView.setNestedScrollingEnabled(sharedPref.getBoolean("scrollableTransactions", false));

        transactionsViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        transactionsViewModel.getAllTransactionItems().observe(getActivity(), new Observer<List<TransactionItem>>() {
            @Override
            public void onChanged(List<TransactionItem> transactionItems) {
                if (!animationShown) { //to avoid showing when notifying adapter
                    recyclerView.scheduleLayoutAnimation();
                    animationShown = true;
                }
                transactionsAdapter.submitList(transactionItems);
            }
        });

        transactionsAdapter.setOnTransactionClickListener(new TransactionsAdapter.OnTransactionClickListener() {
            @Override
            public void OnTransactionClick(TransactionItem transactionItem, int position) {
                updatingPosition = position;
                DialogTransactionEditor transactionEditor = new DialogTransactionEditor(true, transactionItem);
                transactionEditor.show(getChildFragmentManager(), "transaction editor dialog");
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
                List<TransactionItem> fullList;
                if (filteredList == null || filtersNotApplied()) {
                    transactionsList = transactionsViewModel.getTransactionsList();
                    fullList = transactionsList;
                }
                else fullList = filteredList;

                if (text.isEmpty())
                    transactionsAdapter.submitList(fullList);
                else {
                    ArrayList<TransactionItem> searchFilteredList = new ArrayList<>();
                    for (TransactionItem item : fullList) {
                        if (item.getDescription().toLowerCase().contains(text.toLowerCase()) || item.getAmount().toLowerCase().contains(text.toLowerCase())) {
                            searchFilteredList.add(item);
                        }
                    }
                    transactionsAdapter.submitList(searchFilteredList);
                }
            }
        });
    }

    public void updateTransaction(TransactionItem transactionItem) {
        transactionsViewModel.update(transactionItem);
        transactionsAdapter.notifyItemChanged(updatingPosition);
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
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
    }

    private void loadFilters() {
        dateSpinner = view.findViewById(R.id.date_spinner);
        typeSpinner = view.findViewById(R.id.type_spinner);
        sortSpinner = view.findViewById(R.id.sort_spinner);
        categorySpinner = view.findViewById(R.id.category_spinner);

        //date
        String[] dateList = getResources().getStringArray(R.array.date_list);
        CustomFilterArrayAdapter dateAdapter = new CustomFilterArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dateList);
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(this);

        //type
        String[] typeList = getResources().getStringArray(R.array.type_list);
        CustomFilterArrayAdapter typeAdapter = new CustomFilterArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, typeList);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(this);

        //sort
        String[] sortList = getResources().getStringArray(R.array.sort_list);
        CustomFilterArrayAdapter sortAdapter = new CustomFilterArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortList);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setOnItemSelectedListener(this);

        //category
        List<String> categories = new CategoriesManager(requireContext()).getCategoryNames();
        categories.add(0, getString(R.string.any_category));
        categories.add(0, getString(R.string.category));
        categoriesList = categories.toArray(new String[0]);
        CustomFilterArrayAdapter categoryAdapter = new CustomFilterArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoriesList);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(this);
    }

    private void filter() {
        date = dateSpinner.getSelectedItemPosition();
        type = typeSpinner.getSelectedItemPosition();
        sort = sortSpinner.getSelectedItemPosition();
        category = categorySpinner.getSelectedItemPosition();

        transactionsList = transactionsViewModel.getTransactionsList();
        filteredList = new ArrayList<>();
        switch (date) {
            case 1: { //all
                filteredList.addAll(transactionsList);
                break;
            }
            case 2: { //today
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty())
                    checkingList.addAll(transactionsList);
                else
                    checkingList.addAll(filteredList);

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
                    checkingList.addAll(transactionsList);
                else
                    checkingList.addAll(filteredList);

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
                    checkingList.addAll(transactionsList);
                else
                    checkingList.addAll(filteredList);

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
                    checkingList.addAll(transactionsList);
                else
                    checkingList.addAll(filteredList);

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
                    checkingList.addAll(transactionsList);
                else
                    checkingList.addAll(filteredList);

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
                    checkingList.addAll(transactionsList);
                else
                    checkingList.addAll(filteredList);

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
                    Collections.sort(filteredList, (o1, o2) -> Double.valueOf(o2.getTimeInMillis()).compareTo(Double.valueOf(o1.getTimeInMillis())));
                else
                    Collections.sort(transactionsList, (o1, o2) -> Double.valueOf(o2.getTimeInMillis()).compareTo(Double.valueOf(o1.getTimeInMillis())));

                break;
            }
            case 3: { //date - ascending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, Comparator.comparing(o -> Double.valueOf(o.getTimeInMillis())));
                else
                    Collections.sort(transactionsList, Comparator.comparing(o -> Double.valueOf(o.getTimeInMillis())));

                break;
            }
            case 4: { //amount - descending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, (o1, o2) -> Double.valueOf(o2.getAmount()).compareTo(Double.valueOf(o1.getAmount())));
                else
                    Collections.sort(transactionsList, (o1, o2) -> Double.valueOf(o2.getAmount()).compareTo(Double.valueOf(o1.getAmount())));
                break;
            }
            case 5: { //amount - ascending
                if (!filteredList.isEmpty())
                    Collections.sort(filteredList, Comparator.comparing(o -> Double.valueOf(o.getAmount())));
                else
                    Collections.sort(transactionsList, Comparator.comparing(o -> Double.valueOf(o.getAmount())));
                break;
            }
        }

        //match category
        if (category > 0) { //unselected
            if (category == 1) { //any category
                if (filteredList.isEmpty())
                    filteredList.addAll(transactionsList);
            } else {
                List<TransactionItem> checkingList = new ArrayList<>();
                if (filteredList.isEmpty() && date == 0 && type == 0)
                    checkingList.addAll(transactionsList);
                else
                    checkingList.addAll(filteredList);

                for (TransactionItem item : checkingList) {
                    if (item.getCategory().split("###")[0].equals(categoriesList[category]) && !containsItem(item))
                        filteredList.add(item);
                    if (!item.getCategory().split("###")[0].equals(categoriesList[category]) && containsItem(item))
                        remove(item);
                }
            }
        }

        Log.d(TAG, "Date: " + date + " / Type: " + type + " / Sort: " + sort + " / Category: " + category);

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

    private void showResults() {
        transactionsAdapter.submitList(filteredList);
        mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
    }

    public void toggleFilters(View v) {
        v.animate().rotationBy(180f);

        MaterialCardView filtersCard = view.findViewById(R.id.filtersCard);
        if (filtersCard.getVisibility() == View.GONE)
            filtersCard.setVisibility(View.VISIBLE);
        else {
            filtersCard.setVisibility(View.GONE);
            dateSpinner.setSelection(0);
            typeSpinner.setSelection(0);
            sortSpinner.setSelection(0);
            categorySpinner.setSelection(0);
            category = 0;
            transactionsList = transactionsViewModel.getTransactionsList();
            transactionsAdapter.submitList(transactionsList);
            mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
        }
    }

    private boolean filtersNotApplied() {
        return filteredList.isEmpty() && date == 0 && type == 0;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            try {
                filter();
            } catch (ConcurrentModificationException e) {
                Toast.makeText(requireContext(), "Process interrupted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
