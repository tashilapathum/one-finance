package com.tantalum.onefinance.investments;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class InvestmentsViewModel extends AndroidViewModel {
    private InvestmentsRepository investmentsRepository;

    public InvestmentsViewModel(@NonNull Application application) {
        super(application);
        investmentsRepository = new InvestmentsRepository(application);
    }

    public void insert(Investment investment) {
        investmentsRepository.insert(investment);
    }

    public void update(Investment investment) {
        investmentsRepository.update(investment);
    }

    public void delete(Investment investment) {
        investmentsRepository.delete(investment);
    }

    public LiveData<List<Investment>> getAllInvestments() {
        return investmentsRepository.getAllInvestments();
    }

    public LiveData<List<Investment>> getInvestmentsSortByInvestValue() {
        return investmentsRepository.getInvestmentsSortByInvestValue();
    }

    public LiveData<List<Investment>> getInvestmentsSortByReturnValue() {
        return investmentsRepository.getInvestmentsSortByReturnValue();
    }

    public LiveData<List<Investment>> getInvestmentsSortByMostProfitable() {
        return investmentsRepository.getInvestmentsSortByMostProfitable();
    }

    public LiveData<List<Investment>> getInvestmentsSortByTime() {
        return investmentsRepository.getInvestmentsSortByTime();
    }
}
