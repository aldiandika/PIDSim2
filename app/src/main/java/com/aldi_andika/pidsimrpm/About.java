package com.aldi_andika.pidsimrpm;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class About extends AppCompatActivity {

    Button help;
    TextView textView3;
    public static boolean intro = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        help = (Button)findViewById(R.id.help);
        textView3 = (TextView) findViewById(R.id.textView3);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intro = true;
                Intent intent = new Intent(About.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });

        String udata="klien.dinotera@gmail.com";
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);//where first 0 shows the starting and udata.length() shows the ending span.if you want to span only part of it than you can change these values like 5,8 then it will underline part of it.
        textView3.setText(content);
    }

    public void composeEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("*/*");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"klien.dinotera@gmail.com"});
        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(About.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
