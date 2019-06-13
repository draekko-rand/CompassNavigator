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
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.draekko.compassnavigator.R;

public class CalibrateDialogFragment extends DialogFragment
        implements OnClickListener {

    public static final String TAG = "CalibrateDialogFragment";

    public OnDialogClickListener onDialogClickListener;

    public interface OnDialogClickListener {
        void onOkClick();
    }

    public static CalibrateDialogFragment newInstance(OnDialogClickListener onDialogClickListener) {
        Bundle bundle = new Bundle();
        CalibrateDialogFragment fragment = new CalibrateDialogFragment();
        fragment.setArguments(bundle);
        fragment.onDialogClickListener = onDialogClickListener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calibrate, container, false);
        getDialog().setTitle(getString(R.string.calibration));
        Button ok = view.findViewById(R.id.ok);
        ok.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.ok: {
                onDialogClickListener.onOkClick();
                dismiss();
                break;
            }
        }
    }
}
