package com.example.hp.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    private Button CreateAccountbutton;
    private Button HaveAccountButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        CreateAccountbutton = (Button) findViewById(R.id.create_ac);
        HaveAccountButton = (Button) findViewById(R.id.have_ac);

        CreateAccountbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent registerIntent= new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });


        HaveAccountButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(loginIntent);

            }
        });
    }
}

