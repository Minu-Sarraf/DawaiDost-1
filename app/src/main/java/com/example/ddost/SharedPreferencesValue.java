package com.example.ddost;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.commons.validator.routines.EmailValidator;

public class SharedPreferencesValue {
    private SharedPreferences sharedPreferences;
    private static final String MYPREFERENCES="MyPrefs";
    private static final String Phone="phoneKey";
    private static final String Name="nameKey";
    private static final String Age="ageKey";
    private static final String Address="addressKey";
    private static final String Pincode="pinKey";
    private static final String Email ="emailKey";
    private static final String Password ="passwordKey";
    private static final String Prescription ="prescriptionKey";
    private static final String Question ="questionKey";
    private static final String Answer ="answerKey";
    private static final String Link ="linkKey";
    private Context context;

    public SharedPreferencesValue(Context context){
        this.context=context;
    }

    public void setSharedPreferences(){
        sharedPreferences=context.getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
    }

    public void setValues(String phone, String name, String age, String address, String pincode, String email,String password,String question, String answer){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Phone,phone);
        editor.putString(Name,name);
        editor.putString(Age,age);
        editor.putString(Address,address);
        editor.putString(Pincode,pincode);
        editor.putString(Email,email);
        editor.putString(Password,password);
        editor.putString(Question,question);
        editor.putString(Answer,answer);
        editor.commit();
    }

    public String getPhone(){
        return sharedPreferences.getString(Phone," ");
    }

    public String getName(){
        return sharedPreferences.getString(Name," ");
    }

    public String getAge(){
        return sharedPreferences.getString(Age," ");
    }

    public String getAddress(){
        return sharedPreferences.getString(Address," ");
    }

    public String getPincode(){
        return sharedPreferences.getString(Pincode," ");
    }

    public String getEmail(){
        return sharedPreferences.getString(Email," ");
    }

    public String getPassword(){ return sharedPreferences.getString(Password," ");}

    public Boolean getPrescription(){return sharedPreferences.getBoolean(Prescription,false);}

    public String getQuestion(){
        return sharedPreferences.getString(Question," ");
    }

    public String getAnswer(){
        return sharedPreferences.getString(Answer," ");
    }

    public String getLink(){
        return sharedPreferences.getString(Link," ");
    }

    public void setPrescription(Boolean prescription){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Prescription,prescription);
        editor.apply();
    }

    public void setLink(String link){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Link,link);
        editor.apply();
    }

    public void setValues(String phone, String name, String age, String address, String pincode, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Phone,phone);
        editor.putString(Name,name);
        editor.putString(Age,age);
        editor.putString(Address,address);
        editor.putString(Pincode,pincode);
        editor.putString(Email,email);
        editor.apply();
    }

    public boolean isValidEmail(String email){
        EmailValidator validator = EmailValidator.getInstance();
        Log.d("valid", String.valueOf(validator.isValid(email)));
        return  validator.isValid(email);
    }

}
