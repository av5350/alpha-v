package com.example.alpha_v;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class UploadFileActivity extends AppCompatActivity {
    EditText etA, etB, etC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        etA = (EditText) findViewById(R.id.etA);
        etB = (EditText) findViewById(R.id.etB);
        etC = (EditText) findViewById(R.id.etC);

        if (ContextCompat.checkSelfPermission(UploadFileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(UploadFileActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    public void createCsv(View view) {
        try {
            String content = etA.getText().toString() + "," + etB.getText().toString() + "," +etC.getText().toString();
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/alpha_" + UUID.randomUUID().toString().replaceAll("-", "") + ".csv");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

            Toast.makeText(UploadFileActivity.this, "good - csv", Toast.LENGTH_SHORT).show();
            uploadToFirebase(Uri.fromFile(file));
            //file.delete(); todo: how to delete the file at the end???
            // todo: minSDK = 26, its good? or its has to be lower?

        } catch (IOException e) {
            Toast.makeText(UploadFileActivity.this, "bad - csv", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToFirebase(Uri file)
    {
        UploadTask uploadTask = FBref.storageRef.child("files/"+ file.getLastPathSegment()).putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(UploadFileActivity.this, "field to upload", Toast.LENGTH_SHORT).show();
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Toast.makeText(UploadFileActivity.this, "yes to upload", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createWord(View view) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/alpha_" + UUID.randomUUID().toString().replaceAll("-", "") + ".docx");

        try {
            if (!file.exists()){
                file.createNewFile();
            }

            XWPFDocument xwpfDocument = new XWPFDocument();
            XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph();
            XWPFRun xwpfRun = xwpfParagraph.createRun();

            xwpfRun.setText(etA.getText().toString() + "\n" + etB.getText().toString() + "\n" + etC.getText().toString());
            xwpfRun.setFontSize(24);

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            xwpfDocument.write(fileOutputStream);

            if (fileOutputStream!=null){
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            xwpfDocument.close();

            Toast.makeText(UploadFileActivity.this, "good - word", Toast.LENGTH_SHORT).show();
            uploadToFirebase(Uri.fromFile(file));
        } catch (IOException e) {
            Toast.makeText(UploadFileActivity.this, "bad - word", Toast.LENGTH_SHORT).show();
        }
    }

    public void createPdf(View view) {
        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(2480,3508,1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        // put the text in the opposite way - what we see (et 3, et 2, et 1)
        Paint titlePaint = new Paint();
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(300);
        titlePaint.setColor(Color.BLACK);

        canvas.drawText(etA.getText().toString(),1240,1754 , titlePaint);

        titlePaint = new Paint();
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(300);
        titlePaint.setColor(Color.BLACK);

        canvas.drawText(etB.getText().toString(),1240,1354 , titlePaint);

        titlePaint = new Paint();
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(300);
        titlePaint.setColor(Color.BLACK);

        canvas.drawText(etC.getText().toString(),1240,954 , titlePaint);


        pdfDocument.finishPage(page);

        File dataFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/alpha_" + UUID.randomUUID().toString().replaceAll("-", "") + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(dataFile));
            Toast.makeText(this, "pdf saved!", Toast.LENGTH_LONG).show();

            uploadToFirebase(Uri.fromFile(dataFile));

        } catch (IOException e){
            Toast.makeText(this, "error with pdf create!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        pdfDocument.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.gallery) {
            Intent si = new Intent(this, GalleryActivity.class);
            startActivity(si);
        }
        else if (id == R.id.auth)
        {
            Intent si = new Intent(this, MainActivity.class);
            startActivity(si);
        }
        else if (id == R.id.camera)
        {
            Intent si = new Intent(this, CameraActivity.class);
            startActivity(si);
        }
        else if (id == R.id.calendar)
        {
            Intent si = new Intent(this, CalendarActivity.class);
            startActivity(si);
        }

        return true;
    }
}