/* =========================================================================

    Compass Navigator
    Copyright (C) 2019 Draekko, Benoit Touchette

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

   ========================================================================= */

package com.draekko.compassnavigator.dialogs;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.draekko.compassnavigator.R;

public class NoSensorErrorDialogFragment extends DialogFragment
        implements OnClickListener {

    public static final String TAG = "NoSensorErrorDialogFragment";

    public OnDialogClickListener onDialogClickListener;

    public interface OnDialogClickListener {
        void onOkClick();
    }

    public static NoSensorErrorDialogFragment newInstance(OnDialogClickListener onDialogClickListener) {
        Bundle bundle = new Bundle();
        NoSensorErrorDialogFragment fragment = new NoSensorErrorDialogFragment();
        fragment.setArguments(bundle);
        fragment.onDialogClickListener = onDialogClickListener;
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nosensor, container, false);
        getDialog().setTitle(getString(R.string.sensor_error));
        Button ok = view.findViewById(R.id.ok);
        ok.setOnClickListener(this);
        return view;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok: {
                dismiss();
                getActivity().finish();
                System.exit(1);
                break;
            }
        }
    }
}
