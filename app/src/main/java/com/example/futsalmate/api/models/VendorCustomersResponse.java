package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VendorCustomersResponse {
    @SerializedName("customers")
    private List<VendorCustomer> customers;

    @SerializedName("total_customers")
    private int totalCustomers;

    public List<VendorCustomer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<VendorCustomer> customers) {
        this.customers = customers;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }
}
