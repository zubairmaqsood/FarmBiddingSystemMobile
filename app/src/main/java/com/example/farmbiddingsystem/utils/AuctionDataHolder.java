package com.example.farmbiddingsystem.utils;

import com.example.farmbiddingsystem.models.AuctionModel;
import java.util.ArrayList;
import java.util.List;

public class AuctionDataHolder {
    private static AuctionDataHolder instance;
    private List<AuctionModel> masterList;

    private AuctionDataHolder() {
        masterList = new ArrayList<>();
    }

    public static AuctionDataHolder getInstance() {
        if (instance == null) {
            instance = new AuctionDataHolder();
        }
        return instance;
    }

    public void setMasterList(List<AuctionModel> list) {
        this.masterList = list;
    }

    public List<AuctionModel> getMasterList() {
        return masterList;
    }
}