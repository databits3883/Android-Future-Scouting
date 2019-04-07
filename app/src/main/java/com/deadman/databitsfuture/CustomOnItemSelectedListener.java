package com.deadman.databitsfuture;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.github.kimkevin.cachepot.CachePot;

public class CustomOnItemSelectedListener implements OnItemSelectedListener {

    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        Integer intobj = pos;
        CachePot.getInstance().push(1,intobj);
        CachePot.getInstance().push(2,intobj);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // Auto-generated method stub
    }

}