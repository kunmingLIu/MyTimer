package com.liu.mytimer.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liu.mytimer.R;
import com.liu.mytimer.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by kunming.liu on 2017/9/22.
 */

public class EditWorkRecordDialog extends DialogFragment {

    @BindView(R.id.txtStartTime)
    TextView txtStartTime;

    @BindView(R.id.txtEndTime)
    TextView txtEndTime;

    @BindView(R.id.txtTotalTime)
    TextView txtTotalTime;

    @BindView(R.id.editContent)
    EditText editContent;
    @BindView(R.id.btnDelete)
    Button btnDelete;
    @BindView(R.id.btnOK)
    Button btnOK;
    private Unbinder unbinder;

    private ShowClockDialog showClockDialog = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.edit_content_dialog, container);
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        txtTotalTime.setText(Util.covertTimeToString(bundle.getLong("total_work_time")));
        txtStartTime.setText(bundle.getString("start_time"));
        txtEndTime.setText(bundle.getString("end_time"));
        editContent.setText(bundle.getString("work_content"));
        editContent.setSelection(editContent.getText().length());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btnDelete, R.id.btnOK, R.id.txtTotalTime})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnDelete:
                break;
            case R.id.btnOK:
                break;
            case R.id.txtTotalTime:
                if(showClockDialog == null){
                    showClockDialog = new ShowClockDialog();
                }
                showClockDialog.show(getActivity().getSupportFragmentManager(),"[CLOCK_DIALOG]");
                break;
        }
    }
}
