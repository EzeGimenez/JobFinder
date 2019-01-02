package com.visoft.network.funcionalidades;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.visoft.network.Objects.User;
import com.visoft.network.Objects.UserPro;
import com.visoft.network.Util.Constants;
import com.visoft.network.Util.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearcherProUser {

    private DatabaseReference ref;
    private List<UserPro> proUsers;
    private Gson gson;
    private LatLng location;

    public SearcherProUser() {
        gson = GsonerUser.getGson();

        ref = Database.getDatabase()
                .getReference()
                .child(Constants.FIREBASE_USERS_PRO_CONTAINER_NAME);

        getFromDatabase();
    }

    private void getFromDatabase() {
        String mock = "data refresh";
        Query query = ref;

        query.keepSynced(true);
        ref.child("mock").setValue(mock).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("mock").removeValue();

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        proUsers = new ArrayList<>();

                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            if (d != null) {
                                UserPro u = (UserPro) gson.fromJson(d.getValue(String.class), User.class);
                                proUsers.add(u);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public void setLocation(LatLng l) {
        location = l;
    }

    public List<UserPro> getProUsers() {
        if (proUsers == null) {
            proUsers = new ArrayList<>();
        }
        proUsers.remove(HolderCurrentAccountManager.getCurrent(null).getCurrentUser(1));
        Collections.sort(proUsers, new ProUserComparator());
        return proUsers;
    }

    private class ProUserComparator implements Comparator<UserPro> {

        @Override
        public int compare(UserPro p1, UserPro p2) {
            int score = 0;
            if (p2.getNumberReviews() + 10 < p1.getNumberReviews()) {
                score--;
            } else if (p1.getNumberReviews() + 10 <= p2.getNumberReviews()) {
                score++;
            }

            if (p2.getRating() + 0.5 < p1.getRating()) {
                score--;
            } else if (p1.getRating() + 0.5 < p2.getRating()) {
                score++;
            }

            if (location != null) {
                float[] distanceP1 = new float[1];
                float[] distanceP2 = new float[1];
                Location.distanceBetween(location.latitude, location.longitude, p1.getMapCenterLat(), p1.getMapCenterLng(), distanceP1);
                Location.distanceBetween(location.latitude, location.longitude, p2.getMapCenterLat(), p2.getMapCenterLng(), distanceP2);

                int minDistance = Constants.MIN_DISTANCE;
                boolean distanciaP1 = distanceP1[0] <= minDistance;
                boolean distanciaP2 = distanceP2[0] <= minDistance;

                if (!distanciaP1 && distanciaP2) {
                    return -1;
                } else if (distanciaP1 && !distanciaP2) {
                    return 1;
                }

                if (distanceP2[0] - 5 * 1000 > distanceP1[0]) {
                    score += 2;
                } else if (distanceP1[0] - 5 * 1000 > distanceP2[0]) {
                    score -= 2;
                }
            }
            return score;
        }
    }
}
