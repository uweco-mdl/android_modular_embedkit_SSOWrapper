package com.mdlive.embedkit_harness;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mdlive.embedkit.global.MDLiveConfig;
import com.mdlive.embedkit.global.MDLiveConfig.ENVIRON;
import com.mdlive.embedkit.global.MDLiveConfig.SIGNALS;
import com.mdlive.embedkit.global.MDLiveConfig.EMBEDKITS;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends Activity {
    public String gender = "male",  strDate;
    private int month,day,year;
    private DatePickerDialog datePickerDialog;

    TextView txtDOB;
    ENVIRON env;

    static LinkedHashMap memberData;
    static LinkedHashMap pharmaData;

    public enum AFFILIATE {
        BAYLOR, STJOSEPH, CAREINGTON, SUTTER
    }

    private static final Map<AFFILIATE,Integer> AffiliateLayout = new HashMap<>();
    static {
        AffiliateLayout.put(AFFILIATE.BAYLOR, R.layout.main_baylor);
        AffiliateLayout.put(AFFILIATE.STJOSEPH, R.layout.main_stjoseph);
        AffiliateLayout.put(AFFILIATE.CAREINGTON, R.layout.main_careington);
        AffiliateLayout.put(AFFILIATE.SUTTER, R.layout.main_sutter);
    }

    // ***** STAGING DATA ******
    //
    private static final Map<AFFILIATE, String> AffilClientSecret_Stage = new HashMap<>();
    static {
        AffilClientSecret_Stage.put(AFFILIATE.BAYLOR, "10c098fe22d78b51f7f");
        AffilClientSecret_Stage.put(AFFILIATE.STJOSEPH, "7dfc226d06448fbc62c");
        AffilClientSecret_Stage.put(AFFILIATE.CAREINGTON, "a01c7a995c728a1b328");
        AffilClientSecret_Stage.put(AFFILIATE.SUTTER, "");
    }

    private static final Map<AFFILIATE, String> AffilAPIKey_Stage = new HashMap<>();
    static {
        AffilAPIKey_Stage.put(AFFILIATE.BAYLOR, "5f808afe15447728b3c3");
        AffilAPIKey_Stage.put(AFFILIATE.STJOSEPH, "49c0e7a2d2c14a01bfa0");
        AffilAPIKey_Stage.put(AFFILIATE.CAREINGTON, "0611b8c6c77c510e766f");
        AffilAPIKey_Stage.put(AFFILIATE.SUTTER, "");
    }

    // ****** PROD DATA
    //
    private static final Map<AFFILIATE, String> AffilClientSecret_Prod = new HashMap<>();
    static {
        AffilClientSecret_Prod.put(AFFILIATE.BAYLOR, "0000");
        AffilClientSecret_Prod.put(AFFILIATE.STJOSEPH, "0000");
        AffilClientSecret_Prod.put(AFFILIATE.CAREINGTON, "3c7d73325ff3e3ff3c9");
        AffilClientSecret_Prod.put(AFFILIATE.SUTTER, "0000");
    }

    private static final Map<AFFILIATE, String> AffilAPIKey_Prod = new HashMap<>();
    static {
        AffilAPIKey_Prod.put(AFFILIATE.BAYLOR, "0000");
        AffilAPIKey_Prod.put(AFFILIATE.STJOSEPH, "0000");
        AffilAPIKey_Prod.put(AFFILIATE.CAREINGTON, "7d0adbaeb36629f6e394");
        AffilAPIKey_Prod.put(AFFILIATE.SUTTER, "0000");
    }

    private AFFILIATE affil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        affil = AFFILIATE.BAYLOR;

        setContentView(AffiliateLayout.get(affil));

        // listen for EmbedKit exit signal and respond accordingly
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(SIGNALS.EXIT_SIGNAL.name()));

        // ***** DEFAULT ENVIRONMENT VALUE HERE *****************
        env = ENVIRON.STAGE;
        // ******************************************

        getDateOfBirth();                        // Sets up dialog for selecting a Birth Date
        memberData = new LinkedHashMap();        // holds the 'member' key-value pairs
        pharmaData = new LinkedHashMap();        // holds the 'pharmacy' key-value pairs

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.genderGroup);
		
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.Male) {
                    gender = "male";
                } else {
                    gender = "female";
                }
            }
        });

        ((RadioGroup) findViewById(R.id.environment)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.dev) {
                    env = ENVIRON.DEV;
                } else if (checkedId == R.id.qa) {
                    env = ENVIRON.QA;
                } else if (checkedId == R.id.stage) {
                    env = ENVIRON.STAGE;
                } else {
                    env = ENVIRON.PROD;
                }
            }
        });

        txtDOB= (TextView) findViewById(R.id.datePicker);
        txtDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

    }

    /**
     * Listener for the EmbedKit exit signal (LocalBroadcast).
     * Simply resume/reload this activity upon EmbedKit termination
     */
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Toast t = Toast.makeText(MainActivity.this,"< Received 'Finish' signal from EmbedKit >",Toast.LENGTH_SHORT);
            TextView v = (TextView) t.getView().findViewById(android.R.id.message);
            v.setTextColor(Color.CYAN);
            t.show();

            // relaunch current activity
            /*
            Intent intentRestart = new Intent(context, SplashScreenActivity.class);
            intentRestart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentRestart);
            */
            recreate();
        }
    };

    private String getInputText(int id) {
        String inputText="";
        try {
            inputText = ((EditText) findViewById(id)).getText().toString().trim();
        }catch(Exception ex){
            // ...
            // ...
        }
        return (inputText);
    }

    /**
     * Indicates whether screen contains unpopulated fields
     *
     * @return
     */
    private boolean hasEmptyField()
    {
        // DEBUG ONLY.
        if(getInputText(R.id.address1).isEmpty())
            Toast.makeText(this,"ADDRESS empty!",Toast.LENGTH_SHORT).show();
        if(txtDOB.getText().toString().isEmpty())
            Toast.makeText(this,"DOB empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.city).isEmpty())
            Toast.makeText(this,"CITY empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.email).isEmpty())
            Toast.makeText(this,"EMAIL empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.fName).isEmpty())
            Toast.makeText(this,"FIRST NAME empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.lName).isEmpty())
            Toast.makeText(this,"LAST NAME empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.mName).isEmpty())
            Toast.makeText(this,"MIDDLE NAME empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.phone).isEmpty())
            Toast.makeText(this,"PHONE empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.state).isEmpty())
            Toast.makeText(this,"STATE empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.zipcode).isEmpty())
            Toast.makeText(this,"ZIPCODE empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.storeaddress).isEmpty())
            Toast.makeText(this,"STORE ADDR empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.storecity).isEmpty())
            Toast.makeText(this,"STORE CITY empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.latitude).isEmpty())
            Toast.makeText(this,"LATITUDE empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.longitude).isEmpty())
            Toast.makeText(this,"LONGITUDE empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.storestate).isEmpty())
            Toast.makeText(this,"STORE STATE empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.storeNumber).isEmpty())
            Toast.makeText(this,"STORE NUM empty!",Toast.LENGTH_SHORT).show();
        if(getInputText(R.id.storeZip).isEmpty())
            Toast.makeText(this,"STORE ZIP empty!",Toast.LENGTH_SHORT).show();


        boolean hasEmpty = getInputText(R.id.address1).isEmpty()
                            || txtDOB.getText().toString().isEmpty()
                            || getInputText(R.id.city).isEmpty()
                            || getInputText(R.id.email).isEmpty()
                            || getInputText(R.id.fName).isEmpty()
                            || getInputText(R.id.lName).isEmpty()
                            || getInputText(R.id.mName).isEmpty()
                            || getInputText(R.id.phone).isEmpty()
                            || getInputText(R.id.state).isEmpty()
                            || getInputText(R.id.zipcode).isEmpty()
                            || getInputText(R.id.storeaddress).isEmpty()
                            || getInputText(R.id.storecity).isEmpty()
                            || getInputText(R.id.latitude).isEmpty()
                            || getInputText(R.id.longitude).isEmpty()
                            || getInputText(R.id.storestate).isEmpty()
                            || getInputText(R.id.storeNumber).isEmpty()
                            || getInputText(R.id.storeZip).isEmpty();

        return(hasEmpty);
    }

    private void showSnackbarMessage(String mesg) throws NullPointerException
    {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.framelayout);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, mesg, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();

    }

    public void openMyHealth(View v)
    {
        if(hasEmptyField()) {
            if(affil.equals(AFFILIATE.CAREINGTON)){
                showSnackbarMessage(getString(R.string.emptyField));
            }
            return;
        }

        String jsonString = constructJsonString();

        MDLiveConfig.activate(EMBEDKITS.MY_HEALTH, jsonString, env, this);

        finish();

    }

    public void openSAV(View v) {
        if(hasEmptyField()) {
            if(affil.equals(AFFILIATE.CAREINGTON)){
                showSnackbarMessage(getString(R.string.emptyField));
            }
            return;
        }

        /**
         * OLD way of invoking EmbedKit component
         */
        /*
        Intent embedKitIntent = new Intent(MainActivity.this, SSOActivity.class);
        embedKitIntent.putExtra("affiliate_sso_login", jsonString);
        embedKitIntent.putExtra("env", env.name());
        startActivity(embedKitIntent);
        */

        /**
         * NEW way of invoking an EmbedKit component
         */
        String jsonString = constructJsonString();

        MDLiveConfig.activate(EMBEDKITS.DOCTOR_CONSULT, jsonString, env, this);

        finish();
    }

    private String constructJsonString(){

        String jsonString=null;
        JsonObject jsoMessage = new JsonObject();
        try {

            memberData.put("address1", getInputText(R.id.address1));
            memberData.put("address2", getInputText(R.id.address2));
            memberData.put("birthdate", (txtDOB.getText().toString()));
            memberData.put("city", getInputText(R.id.city));
            memberData.put("email", getInputText(R.id.email));
            memberData.put("first_name", getInputText(R.id.fName));
            memberData.put("gender", gender);
            memberData.put("last_name", getInputText(R.id.lName));
            memberData.put("middle_name", getInputText(R.id.mName));
            memberData.put("phone", getInputText(R.id.phone));
            memberData.put("state", getInputText(R.id.state));
            memberData.put("zip", getInputText(R.id.zipcode));

            pharmaData.put("address1", getInputText(R.id.storeaddress));
            pharmaData.put("city", getInputText(R.id.storecity));
            String intersec = getInputText(R.id.intersection).toString();
            pharmaData.put("intersection", intersec==null||intersec.isEmpty() ? "" : intersec);
            pharmaData.put("latitude", getInputText(R.id.latitude));
            pharmaData.put("longitude", getInputText(R.id.longitude));
            pharmaData.put("state", getInputText(R.id.storestate));
            pharmaData.put("store_number", getInputText(R.id.storeNumber));
            pharmaData.put("zipcode", getInputText(R.id.storeZip));


            String ts = Utils.GetCurrentTimeStamp(Utils.DATE_NOTATION.MILLI);
            // ****  !!! DEBUG ONLY ***** REMOVE BEFORE PRODUCTION COMPILATION !!! ********
            //jsoMessage.addProperty("client_api_key", "c9e63d9a77f17039c470");
            // ****************************************************************************
            jsoMessage.addProperty("client_api_key", getApiKey(env, this.affil));

            jsoMessage.addProperty("timestamp", ts);
            jsoMessage.addProperty("unique_id", getInputText(R.id.meid));

            // append remaining key-value pairs
            Gson gson = new Gson();
            JsonElement elem1 = gson.toJsonTree(memberData);
            JsonElement elem2 = gson.toJsonTree(pharmaData);
            jsoMessage.add("member", elem1);
            jsoMessage.add("pharmacy", elem2);


            JsonObject jsonData = new JsonObject();
            // ****  !!! DEBUG ONLY ***** REMOVE BEFORE PRODUCTION COMPILATION !!! ********
            //jsonData.addProperty("client_secret", "b302e84f866a8730eb2");
            // ****************************************************************************
            jsonData.addProperty("client_secret", getClientSecret(env, this.affil));
            jsonData.addProperty("digital_signature", getDigitalSignature(env, this.affil));
            jsonData.add("encrypted_message", jsoMessage);

            jsonString = jsonData.toString();

        }catch(Exception e){
            e.printStackTrace();
        }

        return(jsonString);
    }

    private void getDateOfBirth() {
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, pickerListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setMinDate(this.getDateBeforeNumberOfYears(65));
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.updateDate(1977,0,28);
    }

    /*
     * Return the long mili seconds for a date which is n years back
     */
    private long getDateBeforeNumberOfYears(int numberOfYears)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.YEAR, -numberOfYears);
        return(calendar.getTime().getTime());
    }

	/**
     * The Current date and time will be retrieved by using this method.
     * @param selectedText - the corresponding Textview will be passed as an parameter so the
     * date will be set in the corresponding view.
     *
     */
    public void GetCurrentDate(TextView selectedText)
    {
        // Get current date by calender
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day   = c.get(Calendar.DAY_OF_MONTH);
    }

    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(selectedYear, selectedMonth, selectedDay);
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            Calendar currendDate = Calendar.getInstance();
            currendDate.setTime(new Date());
            strDate = (new StringBuilder().append(month+1).append("/").append(day).append("/").append(year).append(" ")+"");
            txtDOB.setText(strDate);

        }
    };

    private String getApiKey(ENVIRON env, AFFILIATE affil)
    {
        String s = null;
        switch(env)
        {
            case DEV:
                s = "e36046013047c217e810";
                break;
            case QA:
                s = "eb7d90bbeae90e77cf27";
                break;

            case PROD:
                s = AffilAPIKey_Prod.get(affil);
                break;
            case STAGE:
                s = AffilAPIKey_Stage.get(affil);
                //s = "c9e63d9a77f17039c470";
                break;
            default:
                break;
        }

        return(s);
    }


    /**
     *
     * @param env
     * @return
     */
    private String getDigitalSignature(ENVIRON env, AFFILIATE affil)
    {
        String s = null;
        switch(env)
        {
            case DEV:
                s = "ALKSJDLAISUDOAISJDLKASNDLASNDLKJASDLKJ982739823h2j3h42i38y2oihdisahdkjsdh";
                break;
            case QA:
                s = "ALKSJDLAISUDOAISJDLKASNDLASNDLKJASDLKJ982739823h2j3h42i38y2oihdisahdkjsdh";
                break;

            case PROD:
            case STAGE:
                s = "ALKSJDLAISUDOAISJDLKASNDLASNDLKJASDLKJ982739823h2j3h42i38y2oihdisahdkjsdh";
                break;
            default:
                break;
        }

        return(s);
    }

    /**
     *
     * @param env
     * @return
     */
    private String getClientSecret(ENVIRON env, AFFILIATE affil)
    {
        String s = null;
        switch(env)
        {
            case DEV:
                s = "24b4651397af507aa2a";
                break;
            case QA:
                s = "5543d3df8577838a538";
                break;

            case PROD:
                s = AffilClientSecret_Prod.get(affil);
                break;
            case STAGE:
                s = AffilClientSecret_Stage.get(affil);
                //s = "b302e84f866a8730eb2";
                break;
            default:
                break;
        }

        return(s);
    }

}
