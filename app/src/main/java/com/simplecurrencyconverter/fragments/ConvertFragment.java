package com.simplecurrencyconverter.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.simplecurrencyconverter.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.simplecurrencyconverter.fragments.ConvertFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConvertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConvertFragment extends Fragment {

    private static final BigDecimal ONE_EURO_IN_WONS = new BigDecimal(1452.62594);
    private static final BigDecimal ONE_WON_IN_EUROS = new BigDecimal(1).divide(ONE_EURO_IN_WONS, 5, RoundingMode.HALF_EVEN);

    private final DecimalFormat inputAmountFormatter = getAmountFormatter("###,###.##");
    private final DecimalFormat outputAmountFormatter = getAmountFormatter("###,##0.00");

    private DecimalFormat getAmountFormatter(String formatPattern) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat(formatPattern, symbols);
    }

    private final String emptyAmount = "0";

    private boolean allowAmountUpdate = true;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConvertFragment.
     */
    public static ConvertFragment newInstance(String param1, String param2) {
        ConvertFragment fragment = new ConvertFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ConvertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_convert, container, false);
        addCurrencyListeners(view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addCurrencyListeners(View view) {
        EditText krwEditText = (EditText) view.findViewById(R.id.krw_amount);
        EditText eurEditText = (EditText) view.findViewById(R.id.eur_amount);

        addAmountChangedListeners(krwEditText, eurEditText, ONE_WON_IN_EUROS);
        addFocusChangedListener(krwEditText);

        addAmountChangedListeners(eurEditText, krwEditText, ONE_EURO_IN_WONS);
        addFocusChangedListener(eurEditText);
    }

    private void addAmountChangedListeners(final EditText editText, final EditText otherEditText, final BigDecimal multiplier) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateOtherAmount(s, otherEditText, multiplier);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void addFocusChangedListener(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    updateLostFocusAmount((EditText) v);
                }
            }
        });
    }

    private void updateOtherAmount(Editable changedText, EditText editTextToChange, BigDecimal multiplier) {
        if (!allowAmountUpdate) {
            allowAmountUpdate = true;
            return;
        }
        allowAmountUpdate = false;

        String formattedOutputAmount;
        if (changedText.toString().length() == 0) {
            formattedOutputAmount = "";
        }
        else {
            BigDecimal inputAmount = parseDecimal(changedText.toString());
            BigDecimal outputAmount = inputAmount.multiply(multiplier).setScale(2, RoundingMode.HALF_EVEN);
            formattedOutputAmount = formatAmount(outputAmount, outputAmountFormatter);
        }
        editTextToChange.setText(formattedOutputAmount);
    }

    private void updateLostFocusAmount(EditText editText) {
        BigDecimal amount = parseDecimal(editText.getText().toString());
        String formattedOutputAmount = formatAmount(amount, inputAmountFormatter);
        // Empty the other amount too if the amount that lost focus was emptied
        allowAmountUpdate = formattedOutputAmount.length() == 0;
        editText.setText(formattedOutputAmount);
    }

    private BigDecimal parseDecimal(String s) {
        try {
            return new BigDecimal(inputAmountFormatter.parse(s).doubleValue());
        }
        catch (NumberFormatException e) {
            return new BigDecimal(0);
        }
        catch (ParseException e) {
            return new BigDecimal(0);
        }
    }

    private String formatAmount(BigDecimal amount, DecimalFormat formatter) {
        String formattedValue = formatter.format(amount.doubleValue());
        return formattedValue.equals(emptyAmount) ? "" : formattedValue;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onConvertFragmentInteraction(Uri uri);
    }
}