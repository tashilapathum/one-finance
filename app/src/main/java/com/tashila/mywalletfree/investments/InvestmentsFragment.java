package com.tashila.mywalletfree.investments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.MaterialSharedAxis;
import com.tashila.mywalletfree.R;

public class InvestmentsFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.frag_investments, container, false);
        return view;
    }
}
