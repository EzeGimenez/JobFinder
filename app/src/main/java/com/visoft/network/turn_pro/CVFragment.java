package com.visoft.network.turn_pro;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.visoft.network.R;
import com.visoft.network.funcionalidades.ErrorAnimator;
import com.visoft.network.objects.UserPro;
import com.visoft.network.util.Constants;


public class CVFragment extends Fragment {

    //COmponentes gráficas
    private TextView tvCaracteres;
    private EditText etCV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvInfo = getActivity().findViewById(R.id.tvInfo);
        tvInfo.setText(R.string.escriba_descripcion);

        tvCaracteres = view.findViewById(R.id.tvCaracteres);
        etCV = view.findViewById(R.id.etCV);

        etCV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCaracteres.setVisibility(View.VISIBLE);
                tvCaracteres.setText(s.length() + "/" + Constants.MAX_CARACTERES);
                if (s.length() > Constants.MAX_CARACTERES) {
                    tvCaracteres.setTextColor(Color.RED);
                } else {
                    tvCaracteres.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            etCV.setText(bundle.getString("cv"));
        }
    }

    public boolean isInputOk() {
        return etCV.getText().toString().length() <= Constants.MAX_CARACTERES;
    }

    public void setCv(UserPro user) {
        user.setCvText(etCV.getText().toString().trim());
    }

    public void vibrate() {
        ErrorAnimator.shakeError(getContext(), tvCaracteres);
    }
}