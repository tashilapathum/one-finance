package com.tashila.mywalletfree;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.MaterialSharedAxis;
import com.maltaisn.calcdialog.CalcDialog;
import com.tashila.mywalletfree.bills.BillsFragment;
import com.tashila.mywalletfree.cart.CartFragment;
import com.tashila.mywalletfree.loans.LoansFragment;

public class ToolsFragment extends Fragment {
    private View view;
    private static ToolsFragment instance;

    public static ToolsFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_tools, container, false);
        instance = this;

        final Fragment[] fragment = new Fragment[1];
        final String[] fragmentTag = new String[1];

        view.findViewById(R.id.cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment[0] = new CartFragment();
                fragmentTag[0] = "CartFragment";
                openPage(fragment[0], fragmentTag[0]);
            }
        });
        view.findViewById(R.id.bills).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment[0] = new BillsFragment();
                fragmentTag[0] = "BillsFragment";
                openPage(fragment[0], fragmentTag[0]);
            }
        });
        view.findViewById(R.id.loans).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment[0] = new LoansFragment();
                fragmentTag[0] = "LoansFragment";
                openPage(fragment[0], fragmentTag[0]);
            }
        });
        view.findViewById(R.id.calc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalcDialog calcDialog = new CalcDialog();
                calcDialog.getSettings().setExpressionShown(true);
                calcDialog.show(getChildFragmentManager(), "calculator dialog");
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (view.findViewById(R.id.tools_grid).getVisibility() == View.INVISIBLE)
            view.findViewById(R.id.tools_grid).setVisibility(View.VISIBLE);
    }

    private void openPage(Fragment fragment, String fragmentTag) {
        view.findViewById(R.id.tools_grid).setVisibility(View.INVISIBLE);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, fragmentTag)
                .addToBackStack(null)
                .commit();
    }


}
