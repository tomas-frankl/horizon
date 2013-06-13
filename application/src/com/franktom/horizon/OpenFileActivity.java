
package com.franktom.horizon;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import java.io.File;

public class OpenFileActivity extends Activity implements IFolderItemListener {

    FolderLayout localFolders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);
        //setContentView(R.layout.folders);
        localFolders = (FolderLayout)findViewById(R.id.localfolders);
        localFolders.setIFolderItemListener(this);
        //localFolders.setDir("./sys");//change directory if u want,default is root

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_open_file, menu);
        return true;
    }

    @Override
    public void OnCannotFileRead(File file) {
        System.out.println(file.toString());
    }

    @Override
    public void OnFileClicked(File file) {
        System.out.println(file.toString());
    }

}
