package com.certifyglobal.pojo;

public class CompanyInfo {
    public int id;
    public byte[] image;
    public String name;
    public String hostName;

    public byte[] getImageOF(String name) {
        return image;
    }
}
