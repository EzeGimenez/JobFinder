package com.visoft.network.tab_search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.visoft.network.R;
import com.visoft.network.objects.RubroEspecifico;
import com.visoft.network.objects.RubroGeneral;

import java.util.ArrayList;

import eu.davidea.flexibleadapter.FlexibleAdapter;

public class FragmentSpecific extends FragmentFirstTab {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_fragment_specific1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        t.setActual(getTag());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        RubroGeneral rubroGeneral = (RubroGeneral) getArguments().getSerializable("rubro");

        final ArrayList<RubroEspecifico> list = rubroGeneral.getSubRubros();
        for (RubroEspecifico u : list) {
            u.setColor(rubroGeneral.getColor());
        }

        FlexibleAdapter adapter = new FlexibleAdapter<>(list);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter.addListener(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {

                Bundle bundle = new Bundle();
                bundle.putString("rubro", list.get(position).getId());
                t.setCurrentQuery(list.get(position).getId());
                t.advance(bundle);
                return true;
            }
        });
    }
}
