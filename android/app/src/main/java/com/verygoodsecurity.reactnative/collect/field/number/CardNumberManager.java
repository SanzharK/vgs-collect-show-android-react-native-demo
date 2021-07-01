package com.verygoodsecurity.reactnative.collect.field.number;

import android.view.Gravity;

import com.facebook.react.bridge.ReactMethod;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.verygoodsecurity.vgscollect.widget.VGSCardNumberEditText;
import com.verygoodsecurity.vgscollect.widget.VGSTextInputLayout;
import com.verygoodsecurity.vgscollect.view.card.BrandParams;
import com.verygoodsecurity.vgscollect.view.card.CardBrand;
import com.verygoodsecurity.vgscollect.view.card.CardType;
import com.verygoodsecurity.vgscollect.view.card.validation.payment.ChecksumAlgorithm;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ViewGroupManager;

import android.util.TypedValue;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.verygoodsecurity.reactnative.collect.VGSCollectOnCreateViewInstanceListener;
import com.verygoodsecurity.vgscollect.view.card.FieldType;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.ReadableArray;
import com.verygoodsecurity.reactnative.util.ResourceUtil;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;

public class CardNumberManager extends ViewGroupManager<VGSTextInputLayout> {
    private VGSCardNumberEditText editText;
    private VGSTextInputLayout vgsTextInputLayout;

    private VGSCollectOnCreateViewInstanceListener listener;
    private String regex;

    CardNumberManager(VGSCollectOnCreateViewInstanceListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public String getName() {
        return "CardNumberLayout";
    }

    @Override
    protected VGSTextInputLayout createViewInstance(ThemedReactContext reactContext) {
        regex = "";
        createVGSTextInputLayout(reactContext);
        createVGSCardNumberEditText(reactContext);

        return vgsTextInputLayout;
    }

    private void createVGSTextInputLayout(ThemedReactContext reactContext) {
        vgsTextInputLayout = new VGSTextInputLayout(reactContext);
    }

    @ReactProp(name = "padding")
    public void setPadding(VGSTextInputLayout view, int padding) {
        int paddingDp = ResourceUtil.convertPxToDp(view.getContext(), padding);
        editText.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
    }

    @ReactProp(name = "fontSize")
    public void setFontSize(VGSTextInputLayout view, int size) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    @ReactProp(name = "hint")
    public void setHint(VGSTextInputLayout view, String text) {
        view.setHint(text);
    }

    @ReactProp(name = "fiendName")
    public void setFieldName(VGSTextInputLayout view, String text) {
        editText.setFieldName(text);
    }

    @ReactProp(name = "corners", defaultInt = 0)
    public void setBoxCornerRadius(VGSTextInputLayout view, int radius) {
        view.setBoxCornerRadius(radius, radius, radius, radius);
    }

    private void fetchBins() {
        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(new File(""), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String url ="https://run.mocky.io/v3/b329f354-4228-4505-80e4-78b8a7072644";

        // Formulate the request and handle the response.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        regex = "^(56)";
                        getEditTextInstance().setFieldName("SUCCESS");
                        setCustomCardBrand();
                        // Stop since it's a one-time request
                        requestQueue.stop();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        regex = "^(55)";
                        getEditTextInstance().setFieldName(error.toString());
                        setCustomCardBrand();
                        // Stop since it's a one-time request
                        requestQueue.stop();
                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(request);
    }

    private void setCustomCardBrand() {
        if (!regex.isEmpty()) {
            BrandParams brandParams = new BrandParams(
                    "#### #### #### #### ###",
                    ChecksumAlgorithm.NONE,
                    new Integer[]{15, 19},
                    new Integer[]{3}
            );

            CardBrand brand = new CardBrand(
                    regex,
                    "eftpos",
                    android.R.drawable.ic_dialog_info,
                    brandParams
            );
            getEditTextInstance().addCardBrand(brand);
        }
    }

    private void createVGSCardNumberEditText(ThemedReactContext reactContext) {
        editText = new VGSCardNumberEditText(reactContext);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        editText.setIsRequired(true);
        editText.setDivider('-');
        editText.setCardBrandIconGravity(Gravity.END);
        fetchBins();
        setCustomCardBrand();
        vgsTextInputLayout.addView(editText);

        listener.onCreateViewInstance(editText);
    }

    public VGSCardNumberEditText getEditTextInstance() { // <-- returns the View instance
        return editText;
    }

    public String getFieldName() {
        if(editText == null) {
            return "";
        } else {
            return editText.getFieldName();
        }
    }

}