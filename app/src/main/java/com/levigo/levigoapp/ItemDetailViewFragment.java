package com.levigo.levigoapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ItemDetailViewFragment extends Fragment {

    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("users");
    private FirebaseAuth mAuth;

    private DocumentReference typeRef;
    private CollectionReference siteRef;
    private DocumentReference physLocRef;
    private DocumentReference siteDocRef;
    private CollectionReference accessionNumberRef;

    private String mNetworkId;
    private String mNetworkName;
    private String mHospitalId;
    private String mHospitalName;

    private TextInputLayout specificationLayout;
    private TextInputLayout usageLayout;


    private TextInputEditText itemName;
    private TextInputEditText udi;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText quantity;
    private TextInputEditText expiration;
    private TextInputEditText hospitalName;
    private TextInputEditText physicalLocation;
    private TextInputEditText type;
    private TextInputEditText usage;
    private TextInputEditText medicalSpecialty;
    private TextInputEditText referenceNumber;
    private TextInputEditText lotNumber;
    private TextInputEditText manufacturer;
    private TextInputEditText lastUpdate;
    private TextInputEditText notes;
    private TextInputEditText deviceDescription;

    private String itemQuantity;
    private String currentDate;
    private String currentTime;
    private int procedureCount;
    // firebase key labels to avoid hard-coded paths
    private final String NAME_KEY = "name";
    private final String TYPE_KEY = "equipment_type";
    private final String COMPANY_KEY = "company";
    private final String SITE_KEY = "site_name";
    private final String SPECIALTY_KEY = "medical_specialty";
    private final String DESCRIPTION_KEY = "device_description";
    private final String USAGE_KEY = "usage";
    private final String PROCEDURE_KEY = "procedure_used";
    private final String PROCEDUREDATE_KEY = "procedure_date";
    private final String AMOUNTUSED_KEY = "amount_used";
    private final String ACCESSION_KEY = "accession_number";
    private final String PHYSICALLOC_KEY = "physical_location";
    private final String TIME_KEY = "current_time";
    private final String QUANTITY_KEY = "quantity";
    private final String SINGLEORMULTI_KEY = "single_multi";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_viewonlyitemdetail, container, false);
        parent = getActivity();
        itemName = rootView.findViewById(R.id.itemname_text);
        udi = rootView.findViewById(R.id.barcode_edittext);
        deviceIdentifier = rootView.findViewById(R.id.di_edittext);
        quantity = rootView.findViewById(R.id.quantity_edittext);
        expiration = rootView.findViewById(R.id.expiration_edittext);
        hospitalName = rootView.findViewById(R.id.site_edittext);
        physicalLocation = rootView.findViewById(R.id.physicallocation_edittext);
        type = rootView.findViewById(R.id.type_edittext);
        usage = rootView.findViewById(R.id.usage_edittext);
        medicalSpecialty = rootView.findViewById(R.id.medicalspecialty_edittext);
        referenceNumber = rootView.findViewById(R.id.referencenumber_edittext);
        lotNumber = rootView.findViewById(R.id.lotnumber_edittext);
        manufacturer = rootView.findViewById(R.id.company_edittext);
        lastUpdate = rootView.findViewById(R.id.lasteupdate_edittext);
        notes = rootView.findViewById(R.id.notes_edittext);
        deviceDescription = rootView.findViewById(R.id.devicedescription_edittext);
        specificationLayout = rootView.findViewById(R.id.specifications_header);
        usageLayout = rootView.findViewById(R.id.usageicon_header);


        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        final DocumentReference currentUserRef = usersRef.document(userId);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String toastMessage;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try {
                            //TODO Davit update database paths with these variables
                            mNetworkId = document.get("network_id").toString();
                            mNetworkName = document.get("network_name").toString();
                            mHospitalId = document.get("hospital_id").toString();
                            mHospitalName = document.get("hospital_name").toString();
                            typeRef = db.collection("networks").document(mNetworkId).collection("hospitals")
                                    .document(mHospitalId).collection("types").document("type_options");
                            siteRef = db.collection("networks").document(mNetworkId)
                                    .collection("hospitals");
                            physLocRef = db.collection("networks").document(mNetworkId)
                                    .collection("hospitals").document(mHospitalId)
                                    .collection("physical_locations").document("locations");
                            siteDocRef = db.collection("networks").document(mNetworkId)
                                    .collection("hospitals").document(mHospitalId);
                            accessionNumberRef = db.collection("networks")
                                    .document(mNetworkId)
                                    .collection("hospitals").document(mHospitalId)
                                    .collection("accession_numbers");
                        } catch (NullPointerException e) {
                            toastMessage = "Error retrieving user information; Please contact support";
                            Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // document for user doesn't exist
                        toastMessage = "User not found; Please contact support";
                        Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    toastMessage = "User lookup failed; Please try again and contact support if issue persists";
                    Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        if (getArguments() != null) {
            String barcode = getArguments().getString("barcode");
            udi.setText(barcode);
            autoPopulate(siteDocRef, rootView);
        }
        return rootView;
    }

    String di = "";
    private void autoPopulate(final DocumentReference siteDocRef, final View view) {


        final String udiStr = Objects.requireNonNull(udi.getText()).toString();
        udi.setFocusable(false);
        Log.d(TAG, udiStr);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";
        //  final String[] di = {""};

        url = url + udiStr;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject responseJson;
                        try {
                            responseJson = new JSONObject(response);

                            Log.d(TAG, "RESPONSE: " + response);

                            JSONObject deviceInfo = responseJson.getJSONObject("gudid").getJSONObject("device");
                            JSONObject udi = responseJson.getJSONObject("udi");
                            JSONArray productCodes = responseJson.getJSONArray("productCodes");
                            StringBuilder medicalSpecialties = new StringBuilder();
                            for (int i = 0; i < productCodes.length(); i++) {
                                medicalSpecialties.append(productCodes.getJSONObject(i).getString("medicalSpecialty"));
                                medicalSpecialties.append("; ");
                            }
                            medicalSpecialties = new StringBuilder(medicalSpecialties.substring(0, medicalSpecialties.length() - 2));

                            lotNumber.setText(udi.getString("lotNumber"));
                            lotNumber.setFocusable(false);

                            manufacturer.setText(deviceInfo.getString("companyName"));
                            manufacturer.setFocusable(false);

                            expiration.setText(udi.getString("expirationDate"));
                            expiration.setFocusable(false);



                            di = udi.getString("di");
                            deviceIdentifier.setText(udi.getString("di"));
                            deviceIdentifier.setFocusable(false);


                            itemName.setText(deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));
                            itemName.setFocusable(false);

                            deviceDescription.setText(deviceInfo.getString("deviceDescription"));
                            deviceDescription.setFocusable(false);

                            referenceNumber.setText(deviceInfo.getString("catalogNumber"));
                            referenceNumber.setFocusable(false);


                            medicalSpecialty.setText(medicalSpecialties.toString());
                            medicalSpecialty.setFocusable(false);

                            // TODO implement these two
                            autoPopulateFromDatabase(udi, siteDocRef, udiStr, view);
                            //     updateProcedureFieldAdded(udiStr, di);

                            JSONArray deviceSizeArray = deviceInfo.getJSONObject("deviceSizes").getJSONArray("deviceSize");

                            for (int i = 0; i < deviceSizeArray.length(); ++i) {
                                String k;
                                String v;
                                JSONObject currentSizeObject = deviceSizeArray.getJSONObject(i);
                                k = currentSizeObject.getString("sizeType");
                                Log.d(TAG, "KEYS: " + k);
                                if (k.equals("Device Size Text, specify")) {
                                    String customSizeText = currentSizeObject.getString("sizeText");
                                    // Key is usually substring before first number (e.g. "Co-Axial Introducer Needle: 17ga x 14.9cm")
                                    k = customSizeText.split("[0-9]+")[0];

                                    // needs remember the cutoff to retrieve the rest of the string
                                    int cutoff = k.length();
                                    // take off trailing whitespace
                                    try {
                                        k = k.substring(0, k.length() - 2);
                                    } catch (StringIndexOutOfBoundsException e) { // if sizeText starts with number
                                        k = "Size";
                                    }

                                    // Value is assumed to be the substring starting with the number
                                    v = customSizeText.substring(cutoff);
                                    Log.d(TAG, "Custom Key: " + k);
                                    Log.d(TAG, "Custom Value: " + v);

                                } else {
                                    v = currentSizeObject.getJSONObject("size").getString("value")
                                            + " "
                                            + currentSizeObject.getJSONObject("size").getString("unit");
                                    Log.d(TAG, "Value: " + v);
                                }
                              //  addItemSpecs(k, v, view);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error in parsing barcode");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private void autoPopulateFromDatabase(final JSONObject udi, final DocumentReference siteDocRef, final String udiStr, final View view) {
        DocumentReference udiDocRef = null;
        DocumentReference diDocRef = null;

        udiDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di)
                .collection("udis").document(udiStr);

        diDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di);


        udiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get("procedure_number") != null){
                            procedureCount = Integer.parseInt(
                                    Objects.requireNonNull(document.getString("procedure_number")));

                            // TODO implement this function
                           // getProcedureInfo(procedureCount,siteDocRef,udi, udiStr, view);
                        }else{
                            procedureCount = 0;
                        }

                    } else {
                        procedureCount = 0;

                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    procedureCount = 0;
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

        diDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get(TYPE_KEY) != null){
                            type.setText(document.getString(TYPE_KEY));
                            type.setFocusable(false);

                        }if(document.get(SITE_KEY) != null){
                            hospitalName.setText(document.getString(SITE_KEY));
                            hospitalName.setFocusable(false);
                        }if(document.get(QUANTITY_KEY) != null){

                        }else{

                        }if(document.get(USAGE_KEY) != null){
                            String usageStr = document.getString(USAGE_KEY);
                            usage.setText(usageStr);

                        }
                    } else {

                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });


        udiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get("quantity") != null) {
                            itemQuantity = document.getString(QUANTITY_KEY);
                            quantity.setText(itemQuantity);
                        }else{
                            itemQuantity = "0";
                            quantity.setText("0");
                        }if(document.get(PHYSICALLOC_KEY) != null){
                            physicalLocation.setText(document.getString(PHYSICALLOC_KEY));
                        }if(document.get("current_date") != null){
                            currentDate = document.getString("current_date");
                        }if(document.get("current_date_time") != null){
                            currentTime = document.getString("current_date_time");
                            lastUpdate.setText(String.format("%s\n%s", currentDate, currentTime));
                        }
                    } else {
                        itemQuantity = "0";
                        quantity.setText("0");

                        Log.d(TAG, "Document does not exist!");
                    }
                    quantity.setText(document.getString(QUANTITY_KEY));
                    quantity.setFocusable(false);
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

    }
}
