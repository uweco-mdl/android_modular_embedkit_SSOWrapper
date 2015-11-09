package com.mdlive.mobile;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
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
import com.mdlive.unifiedmiddleware.commonclasses.utils.MdliveUtils;

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

    private AFFILIATE affil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        affil = AFFILIATE.STJOSEPH;

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

        // register EmbedKit exit signal listener and respond accordingly
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(SIGNALS.EXIT_SIGNAL.name()));
		
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
            Intent intentRestart = new Intent(context, SplashScreenActivity.class);
            intentRestart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentRestart);
        }
    };

    private String getInputText(int id) {
        String inputText ="";
        return ((EditText) findViewById(id)).getText().toString().trim();
    }


    public void openMDLIVE(View v) {
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
            pharmaData.put("longitude", getInputText(R.id.langitude));
            pharmaData.put("state", getInputText(R.id.storestate));
            pharmaData.put("store_number", getInputText(R.id.storeNumber));
            pharmaData.put("zipcode", getInputText(R.id.storeZip));


            String ts = Utils.GetCurrentTimeStamp(Utils.DATE_NOTATION.MILLI);
            // ****  !!! DEBUG ONLY ***** REMOVE BEFORE PRODUCTION COMPILATION !!! ********
            jsoMessage.addProperty("client_api_key", "c9e63d9a77f17039c470");
            // ****************************************************************************
            //jsoMessage.addProperty("client_api_key", getApiKey(env, this.affil));

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
            jsonData.addProperty("client_secret", "b302e84f866a8730eb2");
            // ****************************************************************************
            //jsonData.addProperty("client_secret", getClientSecret(env, this.affil));
            jsonData.addProperty("digital_signature", getDigitalSignature(env, this.affil));
            jsonData.add("encrypted_message", jsoMessage);

            String jsonString = jsonData.toString();

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
            MDLiveConfig.activate(EMBEDKITS.DOCTOR_CONSULT, jsonString, env, this);

            finish();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void getDateOfBirth() {
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, pickerListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setMinDate(MdliveUtils.getDateBeforeNumberOfYears(65));
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.updateDate(1977,0,28);
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
