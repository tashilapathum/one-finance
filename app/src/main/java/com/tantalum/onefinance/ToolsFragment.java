package com.tantalum.onefinance;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.MaterialSharedAxis;
import com.maltaisn.calcdialog.CalcDialog;
import com.tantalum.onefinance.bills.BillsFragment;
import com.tantalum.onefinance.cart.CartFragment;
import com.tantalum.onefinance.loans.LoansFragment;

public class ToolsFragment extends Fragment {
    private View view;
    private static ToolsFragment instance;
    private ToolSelectedListener listener;

    public static ToolsFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_tools, container, false);
        instance = this;

        //theme
        String theme = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE).getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        final Fragment[] fragment = new Fragment[1];
        final String[] fragmentTag = new String[1];

        view.findViewById(R.id.cart).setOnClickListener(v -> {
            fragment[0] = new CartFragment();
            fragmentTag[0] = "CartFragment";
            listener.onToolSelected(fragment[0], fragmentTag[0]);
        });
        view.findViewById(R.id.bills).setOnClickListener(v -> {
            fragment[0] = new BillsFragment();
            fragmentTag[0] = "BillsFragment";
            listener.onToolSelected(fragment[0], fragmentTag[0]);
        });
        view.findViewById(R.id.loans).setOnClickListener(v -> {
            fragment[0] = new LoansFragment();
            fragmentTag[0] = "LoansFragment";
            listener.onToolSelected(fragment[0], fragmentTag[0]);
        });
        view.findViewById(R.id.calc).setOnClickListener(v -> {
            CalcDialog calcDialog = new CalcDialog();
            calcDialog.getSettings().setExpressionShown(true);
            calcDialog.show(getChildFragmentManager(), "calculator dialog");
        });

        return view;
    }

    public interface ToolSelectedListener {
        void onToolSelected(Fragment fragment, String fragmentTag);
    }

    public void setToolSelectedListener(ToolSelectedListener listener) {
        this.listener = listener;
    }

}
