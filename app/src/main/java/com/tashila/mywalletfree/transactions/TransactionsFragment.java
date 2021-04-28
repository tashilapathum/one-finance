package com.tashila.mywalletfree.transactions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.tashila.mywalletfree.AnimationHandler;
import com.tashila.mywalletfree.CustomFilterArrayAdapter;
import com.tashila.mywalletfree.DateTimeHandler;
import com.tashila.mywalletfree.R;

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

public class TransactionsFragment extends Fragment {
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
    private int date;
    private int type;
    private int sort;
    List<TransactionItem> transactionsList;
    List<TransactionItem> filteredList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, false));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_transactions, null);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);

        loadItems();
        loadFilters();
        etSearch = view.findViewById(R.id.etSearch);
        implementSearch();
        view.findViewById(R.id.toggleFilters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.animate().rotationBy(180f);
                toggleFilters();
            }
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
        transactionsViewModel.getAllTransactionItems().observe(this, new Observer<List<TransactionItem>>() {
            @Override
            public void onChanged(List<TransactionItem> transactionItems) {
                transactionsAdapter.submitList(transactionItems);
            }
        });

        transactionsAdapter.setOnTransactionClickListener(new TransactionsAdapter.OnTransactionClickListener() {
            @Override
            public void OnTransactionClick(TransactionItem transactionItem) {
                DialogTransactionEditor transactionEditor = new DialogTransactionEditor(getActivity(), transactionItem);
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
        transactionsAdapter.notifyDataSetChanged();
    }

    private void loadFilters() {
        dateSpinner = view.findViewById(R.id.date_spinner);
        typeSpinner = view.findViewById(R.id.type_spinner);
        sortSpinner = view.findViewById(R.id.sort_spinner);

        //date
        String[] dateList = getResources().getStringArray(R.array.date_list);
        CustomFilterArrayAdapter dateAdapter = new CustomFilterArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, dateList);
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    try {
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(getActivity(), "Process interrupted", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //type
        String[] typeList = getResources().getStringArray(R.array.type_list);
        CustomFilterArrayAdapter typeAdapter = new CustomFilterArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, typeList);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    try {
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(getActivity(), "Process interrupted", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //sort
        String[] sortList = getResources().getStringArray(R.array.sort_list);
        CustomFilterArrayAdapter sortAdapter = new CustomFilterArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, sortList);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    try {
                        filter();
                    } catch (ConcurrentModificationException e) {
                        Toast.makeText(getActivity(), "Process interrupted", Toast.LENGTH_SHORT).show();
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
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
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
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
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
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
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
                    DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
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
            DateTimeHandler dateTimeHandler = new DateTimeHandler(item.getUserDate());
            if (dateTimeHandler.getDayOfYear() == date && !filteredList.contains(item))
                filteredList.add(item);
        }
        showResults();
    }

    private void showResults() {
        transactionsAdapter.submitList(filteredList);
        mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
    }

    public void toggleFilters() {
        MaterialCardView filtersCard = view.findViewById(R.id.filtersCard);
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
