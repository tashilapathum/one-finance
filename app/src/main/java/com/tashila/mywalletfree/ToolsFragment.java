package com.tashila.mywalletfree;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media2.exoplayer.external.C;
import androidx.transition.TransitionManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ToolsFragment extends Fragment {
    private View view;
    private static ToolsFragment instance;

    public static ToolsFragment getInstance() {
        return instance;
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
        view.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(getActivity())
                        .setTitle("Test")
                        .setMessage("Message")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        return view;
    }

    private void openPage(Fragment fragment, String fragmentTag) {
        view.findViewById(R.id.tools_grid).setVisibility(View.GONE);
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.fragment_container, fragment, fragmentTag)
                .addToBackStack(null)
                .commit();
    }


}
