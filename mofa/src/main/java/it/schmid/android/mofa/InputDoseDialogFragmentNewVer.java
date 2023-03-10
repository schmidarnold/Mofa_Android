package it.schmid.android.mofa;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import it.schmid.android.mofa.adapter.WirkungAdapter;
import it.schmid.android.mofa.interfaces.InputDoseASANewFragmentListener;
import it.schmid.android.mofa.interfaces.ProductInterface;
import it.schmid.android.mofa.model.Pesticide;
import it.schmid.android.mofa.model.Wartefrist;
import it.schmid.android.mofa.model.Wirkung;


@SuppressLint("ValidFragment")
public class InputDoseDialogFragmentNewVer extends DialogFragment implements OnEditorActionListener{
	private static final String TAG = "InputDoseDialogFragment";
	private Spinner mReasonSpinner;
	private TextView mSizeText;
	//private TextView mAmountProHa;
	private TextView mStatusText;
	private TextView mWaitingTimeText;
	private TextView mConstraintsText;
	private EditText mDoseHlText;
	private EditText mAmountText;
	private EditText mAmountProHa;
	private Button mOkButton;
	private Button mCancelButton;
	private ProductInterface mPesticide;
	private Double mConc;
	private Double mWaterAmount;
	private Double mSize;
	private String mReason;
	private InputDoseASANewFragmentListener callback;
	private Integer focus = 0; //checking which field is active to calculate the right amount on closing the dialog
	private Double mAmount;
	private Double mDose;
	private Wirkung mWirkung;
	private Boolean edit=false;
	private PestInfos pestInfos;
	private boolean isPest = false;
	private List<Wirkung> wirkungList;
	WirkungAdapter wirkungAdapter;
	private String strDefCultivationTyp;
//	public interface InputDoseDialogFragmentListener {
//		void onFinishEditDialog(Double doseInput, Double amountInput);
//
//	}
	public InputDoseDialogFragmentNewVer() {

	}
	public InputDoseDialogFragmentNewVer(ProductInterface pesticide, Double conc, Double water, Double size) {
		this.mPesticide=pesticide;
		this.mConc=conc;
		this.mWaterAmount=water;
		this.mSize=size;
	}
//	constructor for existing entries, to modify the dose or amount
	public InputDoseDialogFragmentNewVer(ProductInterface pesticide, Double dose, Double doseAmount, Double conc, Double water, Double size,String reason){
		this.mPesticide=pesticide;
		this.mConc=conc;
		this.mWaterAmount=water;
		this.mDose=dose;
		this.mAmount=doseAmount;
		this.mSize=size;
		this.mReason=reason;
		this.edit=true;
	}
	public void setCallback(InputDoseASANewFragmentListener mCallback){
		callback = mCallback;
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	if (edit==false){
        		callback = (InputDoseASANewFragmentListener) getTargetFragment();
        	}
			if (this.mPesticide instanceof Pesticide){
				Gson gson = new Gson();
				isPest = true;
				Pesticide pest = (Pesticide) this.mPesticide;
				String jsonStr = pest.getConstraints();
				pestInfos = gson.fromJson(jsonStr,PestInfos.class);
			}
        	
            
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogFragmentListener interface");
        }
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_input_dose_asa, container);

		mSizeText=(TextView) view.findViewById(R.id.lbl_size);
		Resources res = getResources();
		String sizeText = String.format(res.getString(R.string.sizeInfo), mSize.toString());
		mSizeText.setText(sizeText);
		mAmountProHa =(EditText) view.findViewById(R.id.txt_amount_ha_value);
		mReasonSpinner = (Spinner) view.findViewById(R.id.spReasons);
		if (isPest){
			Pesticide pest = (Pesticide) this.mPesticide;
			showDetailsOfPest(pest, view);
			loadSpinner();
		}
        mDoseHlText = (EditText) view.findViewById(R.id.txt_dose_hl);
        mAmountText = (EditText) view.findViewById(R.id.txt_dose_total);
        mOkButton = (Button) view.findViewById(R.id.ok_confirm_button);

        mCancelButton = (Button) view.findViewById(R.id.cancel_confirm_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});
        mOkButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (focus==0){
	            	mAmountText.setText((calcAmount(mWaterAmount,mConc))); //recalculate the amount
	            }else{
	            	mDoseHlText.setText((calcDose(mWaterAmount,mConc))); //recalculate the dose
	            }
				SharedPreferences sharedPreferences = PreferenceManager .getDefaultSharedPreferences(getActivity());

				boolean showPestInfos = sharedPreferences.getBoolean("showPestInfos",false);
				//Log.d("showpestInfo", "value = " + showPestInfos);
				//TODO showing constraints on pesticide
				if (mPesticide.showInfo()== 1 && showPestInfos) { //only for pesticides
					PestInfoDialog infoDialog = PestInfoDialog.newInstance(mPesticide.getId());
				 	infoDialog.show(getFragmentManager(),"DialogFragment");
				}
//
				mWirkung = (Wirkung) mReasonSpinner.getSelectedItem();
				callback.onFinishEditDialog(mDose,mAmount,mWirkung);
				
				
	            getDialog().dismiss();
				
			}
		});
        getDialog().setTitle(mPesticide.getProductName());
        	if (edit==false){ //only the case for new selection
        		if (mPesticide.getDefaultDose()!=null){
            		//java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
            		NumberFormat nf = NumberFormat.getInstance(Locale.US);
					//new DecimalFormat("####.000", DecimalFormatSymbols.getInstance(Locale.US));
            		//DecimalFormat nf = new DecimalFormat("#.###");
					((DecimalFormat) nf).applyPattern("###.###");
					mDoseHlText.setText (nf.format(mPesticide.getDefaultDose()));
            		mAmountText.setText((calcAmount(mWaterAmount,mConc)));
            	}
        	}else{ //editing the selected product
        		NumberFormat nf = NumberFormat.getInstance(Locale.US);
        		mDoseHlText.setText (nf.format(mDose));
        		mAmountText.setText(nf.format(mAmount));
				mWirkung = getWirkung(mReason);
				mReasonSpinner.setSelection(wirkungAdapter.getPosition(mWirkung));
				setReasonTextField(mWirkung);
				calcAmountProHa(mAmount);
        	}
        	
        	// Show soft keyboard automaticallybi
            mDoseHlText.requestFocus();
            mDoseHlText.setOnEditorActionListener(this);
            mAmountText.setOnEditorActionListener(this);
            mDoseHlText.setOnFocusChangeListener(new OnFocusChangeListener() { //listener to calculate the amount
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						mAmountText.setText((calcAmount(mWaterAmount,mConc)));
						Log.d(TAG,"[mDoseHlText.setOnFocusChange] " + "calculating with " + mWaterAmount +"," + mConc);
					}else{
						//Toast.makeText(getActivity(), "focus==0", Toast.LENGTH_LONG).show();
						focus=0;
					}
						
					
				}
			});
            
            mAmountText.setOnFocusChangeListener(new OnFocusChangeListener() { //listener to calculate the dose
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus){
						mDoseHlText.setText((calcDose(mWaterAmount,mConc)));
						Log.d(TAG,"[mAmountText.setOnFocusChange] " + "calculating with " + mWaterAmount +"," + mConc);
					}else{
						focus=1;
					}
					
				}
			});
		mAmountProHa.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mAmountProHa.hasFocus()&& mSize != null && s.length()>0){ //only if the current textfield has focus
					mAmount=0.00;
					mDose=0.00;
					Double amountHa=0.00;
					NumberFormat nf = NumberFormat.getInstance(Locale.US);
					((DecimalFormat) nf).applyPattern("###.###");
					try {
						amountHa = nf.parse(s.toString()).doubleValue();
						mAmount = amountHa/10000 * mSize;
						mAmountText.setText(nf.format(mAmount));
						mDoseHlText.setText(calcDose(mWaterAmount,mConc));
					} catch (ParseException e) {
						Toast.makeText(getActivity(),"Please enter a number, not a text",Toast.LENGTH_LONG).show();
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
        return view;

	}
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			
            if (focus==0){
            	mAmountText.setText((calcAmount(mWaterAmount,mConc))); //recalculate the amount
            }else{
            	mDoseHlText.setText((calcDose(mWaterAmount,mConc))); //recalculate the dose
            }
            	
			callback.onFinishEditDialog(mDose,mAmount,mWirkung);
			
            this.dismiss();
            return true;
        }
        return false;
    }
	//showing constraints from ASA
	private void showDetailsOfPest(Pesticide pest, View view){

		if (pest.getStatus()!= null){
			mStatusText=(TextView) view.findViewById(R.id.lbl_status);
			mStatusText.setText(getResources().getString(R.string.status) + " " + pest.getStatus());
		}
		if (pestInfos!= null){
			mWaitingTimeText = (TextView) view.findViewById(R.id.waitingTimetxt);
			mConstraintsText = (TextView) view.findViewById(R.id.restrictionstxt);

			SpannableStringBuilder warteFristStr = getWarteFristStr(pestInfos.getWartefrist());
			mWaitingTimeText.setText(warteFristStr);
			mReasonSpinner = (Spinner) view.findViewById(R.id.spReasons);

		}

	}
	private String calcAmount(Double wateramount, Double conc){
		mAmount=0.00;
		mDose=0.00;

		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		((DecimalFormat) nf).applyPattern("###.###");
		try {
			mDose = nf.parse(mDoseHlText.getText().toString()).doubleValue();
			mAmount = (mDose*wateramount*conc);
			if (mAmountProHa.hasFocus()==false){
				calcAmountProHa(mAmount);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return nf.format(mAmount);
	}	
	private String calcDose(Double wateramount,Double conc){
		mAmount=0.000;
		mDose=0.000;
		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		((DecimalFormat) nf).applyPattern("###.###");
		try {
			mAmount = nf.parse(mAmountText.getText().toString()).doubleValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		mDose = mAmount/(wateramount*conc);
		return nf.format(mDose);
	}
	private void calcAmountProHa(Double amount){
		Double amountProHa=0.00;
		if(mSize!= 0){
			amountProHa =amount/mSize*10000 ;
			amountProHa = (double)Math.round(amountProHa * 100) / 100;
			mAmountProHa.setText(amountProHa.toString());
		}

	}
	private void loadSpinner(){
		wirkungList = pestInfos.getWirkung();
		// Creating adapter for spinner
		wirkungAdapter = new WirkungAdapter(wirkungList,getContext());

		mReasonSpinner.setAdapter(wirkungAdapter);
		if (!wirkungList.isEmpty()){
			mWirkung = wirkungList.get(0);

		}
		mReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				 mWirkung = (Wirkung) parent.getItemAtPosition(position);
				 setReasonTextField(mWirkung);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

	}
	private Wirkung getWirkung(String reason){
		for (Wirkung w: wirkungList){
			if (w.toString().equalsIgnoreCase(reason)){
				return w;
			}
		}
		return wirkungList.get(0); //first element.only if there is no equal reason, which should not be
	}
	private class PestInfos {

		@SerializedName("Wirkung")
		@Expose
		private List<Wirkung> wirkung = null;
		@SerializedName("Wartefrist")
		@Expose
		private List<Wartefrist> wartefrist = null;

		public List<Wirkung> getWirkung() {
			return wirkung;
		}



		public List<Wartefrist> getWartefrist() {
			return wartefrist;
		}



	}
	private SpannableStringBuilder getWarteFristStr (List<Wartefrist> warteList){
		String cultivationType;
		String anbauArt;
		SpannableStringBuilder builder = new SpannableStringBuilder();
		SpannableStringBuilder beeBuilder = new SpannableStringBuilder();

		int redColor = ContextCompat.getColor(getContext(),R.color.red);



		ForegroundColorSpan redSpan = new ForegroundColorSpan(redColor);

		builder.append(getResources().getString(R.string.warteFristen));
		if (!warteList.isEmpty()){

			if (warteList.get(0).getBeeRestriction()==1){
				String beeText = getResources().getString(R.string.beeWarning);

				beeBuilder.append(beeText);
				beeBuilder.setSpan(redSpan, beeBuilder.length() - beeText.length(), beeBuilder.length(),0);
				builder.append(beeBuilder);
			}
		}
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		strDefCultivationTyp = preferences.getString("listCultivationType","1");
		switch (strDefCultivationTyp) {
			case "1":
				anbauArt = "Agrios";
				break;
			case "2":
				anbauArt = "Bio";
				break;
			case "3":
				anbauArt = "Gesetzlich";
				break;
			default:
				anbauArt = "Agrios";
		}
		//Log.d("InputFragmentASANew", "Anbauart = " + strDefCultivationTyp);
		for (Wartefrist w : warteList){
			if (w.getAnbauart().equalsIgnoreCase(anbauArt)){
				SpannableStringBuilder warteFristBuilder = new SpannableStringBuilder();


				String karenzZeit;
				ForegroundColorSpan colorSpan;

				warteFristBuilder.append("\n");


				karenzZeit = w.getKultur() +", "+w.getAnbauart() +": " + w.getKarenzzeit();


				warteFristBuilder.append(karenzZeit);
				warteFristBuilder.setSpan(new BulletSpan(10),warteFristBuilder.length() - karenzZeit.length(),warteFristBuilder.length(),17);
				builder.append(warteFristBuilder);
			}

		}

	return builder;
	}
	private void setReasonTextField(Wirkung mWirkung){
		SpannableStringBuilder builder = new SpannableStringBuilder();
		if (mWirkung.getMinDose()!= null){
			builder.append(getResources().getString(R.string.min_dose) + " " + mWirkung.getMinDose());
			builder.append("\n");
		}
		if (mWirkung.getMaxDose()!= null){
			builder.append(getResources().getString(R.string.max_dose) + " " + mWirkung.getMaxDose());
			builder.append("\n");
		}
		if (mWirkung.getMaxUseProYear()!= null){
			builder.append(getResources().getString(R.string.max_use_pro_year) + " " + mWirkung.getMaxUseProYear());
			builder.append("\n");
		}
		if (mWirkung.getMaxAmountProUse()!= null){
			builder.append(getResources().getString(R.string.maxamountha) + " " + mWirkung.getMaxAmountProUse());
			builder.append("\n");
		}
		if (mWirkung.getMaxUsageInSerie()!= null){
			builder.append(getResources().getString(R.string.max_use_year_in_serie) + " " + mWirkung.getMaxUsageInSerie());
			builder.append("\n");
		}

		mConstraintsText.setText(builder);
	}

}
