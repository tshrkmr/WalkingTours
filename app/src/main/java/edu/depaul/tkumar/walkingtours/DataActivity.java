package edu.depaul.tkumar.walkingtours;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DataActivity extends AppCompatActivity {

    TextView buildingName;
    TextView buildingAddress;
    TextView buildingDescription;
    ImageView buildingPhoto;
    FenceData fenceData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        fenceData = (FenceData) getIntent().getSerializableExtra("DATA");
        setUpActionBar();
        initializeFields();
        setData();
    }

    private void initializeFields(){
        buildingName = findViewById(R.id.dataBuildingNameTextView);
        buildingAddress = findViewById(R.id.dataBuildingAddressTextView);
        buildingDescription = findViewById(R.id.dataBuildingDescriptionTextView);
        buildingPhoto = findViewById(R.id.dataBuildingPhotoImageView);
        buildingDescription.setMovementMethod(new ScrollingMovementMethod());

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        buildingName.setTypeface(font);
        buildingAddress.setTypeface(font);
        buildingDescription.setTypeface(font);

    }

    private void setData(){
        if(fenceData != null){
            buildingName.setText(fenceData.getId());
            buildingAddress.setText(fenceData.getAddress());
            buildingDescription.setText(fenceData.getDescription());
            String urlToImage = fenceData.getImageURL();
            if(!urlToImage.equals("") && !urlToImage.equals("null")){
                Picasso.get().load(urlToImage)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.brokenimage)
                        .into(buildingPhoto);
            }
        }
    }

    private void setUpActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.home_image);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setTitle("");
        }
    }
}