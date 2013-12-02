package tma.wifisaver;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class NumberPickerFragment extends DialogFragment {

    private EditText hourText,minuteText;


    public interface NumberPickerDialogHandler {
        void onTimeSet(int hourOfDay, int minute);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.number_picker_fragment,container,false);
        hourText = (EditText)view.findViewById(R.id.number_picker_hour);
        minuteText = (EditText)view.findViewById(R.id.number_picker_minute);
        Button button = (Button) view.findViewById(R.id.number_picker_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, minute;
                String temp = hourText.getText().toString();
                hour = temp.equals("") ? 0 : Integer.parseInt(temp);
                temp = minuteText.getText().toString();
                minute = temp.equals("") ? 0 : Integer.parseInt(temp);
                if (hour == 0 && minute == 0) {
                    Toast.makeText(getActivity(), R.string.number_picker_toast, Toast.LENGTH_SHORT).show();
                } else {
                    final Activity activity = getActivity();
                    if (activity instanceof NumberPickerDialogHandler) {
                        final NumberPickerDialogHandler handler = (NumberPickerDialogHandler) activity;
                        handler.onTimeSet(hour, minute);
                        dismiss();
                    }
                }
            }
        });
        return view;
    }
}
