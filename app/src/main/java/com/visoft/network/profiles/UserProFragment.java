package com.visoft.network.profiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.visoft.network.R;
import com.visoft.network.custom_views.CustomDialog;
import com.visoft.network.custom_views.CustomSnackBar;
import com.visoft.network.funcionalidades.HolderCurrentAccountManager;
import com.visoft.network.funcionalidades.MapHighlighter;
import com.visoft.network.funcionalidades.Messenger;
import com.visoft.network.objects.QualityInfo;
import com.visoft.network.objects.Review;
import com.visoft.network.objects.RubroEspecifico;
import com.visoft.network.objects.UserPro;
import com.visoft.network.util.Constants;
import com.visoft.network.util.Database;
import com.visoft.network.util.GlideApp;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.davidea.flexibleadapter.FlexibleAdapter;

public class UserProFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private UserPro user;
    private DatabaseReference database;
    private boolean isRunning;

    //Componentes graficas
    private TextView tvUsername, tvNumberReviews, tvHrAtencion;
    private SimpleRatingBar ratingBar;
    private RecyclerView rvRubro;
    private MapView mapView;
    private ImageButton btnCV, btnShowContactInfo, btnMoreInfo;
    private Button btnShowReviews;
    private ImageView btnInstagram, btnWhatsapp, btnMail, btnFacebook;
    private LinearLayout profileControls, containerSocialMedia;
    private ImageView ivProfilePic, editarPerfil;
    private View containerScreens;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        user = (UserPro) getArguments().getSerializable("user");
        database = Database.getDatabase().getReference();

        return inflater.inflate(R.layout.fragment_pro_user_void, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View createdView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(createdView, savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.fragment_pro_user, null);
        ((ViewGroup) (createdView)).addView(view);

        tvNumberReviews = view.findViewById(R.id.tvNumberReviews);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvHrAtencion = view.findViewById(R.id.tvHrAtencion);
        btnCV = view.findViewById(R.id.btnCV);
        rvRubro = view.findViewById(R.id.rvRubros);
        btnMoreInfo = view.findViewById(R.id.btnMoreInfo);
        ratingBar = view.findViewById(R.id.ratingBar);
        mapView = view.findViewById(R.id.map);
        btnShowReviews = view.findViewById(R.id.btnShowReviews);
        btnShowContactInfo = view.findViewById(R.id.btnShowContactInfo);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        btnInstagram = view.findViewById(R.id.btnInstagram);
        btnMail = view.findViewById(R.id.btnMail);
        btnFacebook = view.findViewById(R.id.btnFacebook);
        btnWhatsapp = view.findViewById(R.id.btnWhatsapp);
        profileControls = view.findViewById(R.id.personalControls);
        containerSocialMedia = view.findViewById(R.id.ContainerSocialMedia);
        editarPerfil = view.findViewById(R.id.editar);

        containerScreens = view.findViewById(R.id.ContainerScreen);

        isRunning = true;

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        iniciarUI();
    }

    private void iniciarUI() {
        tvUsername.setText(user.getUsername());

        FlexibleAdapter<RubroEspecifico> adapter = new FlexibleAdapter<>(null);

        for (String s : user.getRubros()) {
            RubroEspecifico aux = new RubroEspecifico(getContext(), s);
            aux.setLayoutRes(R.layout.user_rubros);
            adapter.addItem(aux);
        }

        rvRubro.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvRubro.setAdapter(adapter);

        String[] diasL = getResources().getStringArray(R.array.dias);
        String[] hrAtencion = user.getHoraAtencion().split(" - ");
        String[] diasAtencion = new String[2];
        diasAtencion[0] = diasL[user.getDiasAtencion() / 10];
        diasAtencion[1] = diasL[user.getDiasAtencion() % 10];

        tvHrAtencion.setText(diasAtencion[0] + " " + getString(R.string.a) + " " + diasAtencion[1]
                + "\n " + hrAtencion[0] + " " + getString(R.string.a) + " " + hrAtencion[1]);

        btnShowContactInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactInfo();
            }
        });
        getView().findViewById(R.id.infoApp).setOnClickListener(this);


        final String cv = user.getCvText();
        if (cv.trim().length() > 0) {
            btnCV.setVisibility(View.VISIBLE);
            btnCV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomDialog customDialog = new CustomDialog(getContext());
                    customDialog.setMessage(cv)
                            .setTitle(getString(R.string.descripcion_personal))
                            .setPositiveButton(getString(R.string.aceptar), null)
                            .show();
                }
            });
        } else {
            btnCV.setVisibility(View.GONE);
        }
        btnMoreInfo.setOnClickListener(this);

        if (user.getNumberReviews() > 0) {
            ratingBar.setRating(user.getRating());
            tvNumberReviews.setText(String.format("%.1f", user.getRating()));
            btnShowReviews.setText(user.getNumberReviews() + "");
            btnShowReviews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReviews();
                }
            });
        } else {
            tvNumberReviews.setText("0");
            ratingBar.setRating(0);
            btnShowReviews.setVisibility(View.GONE);
        }

        if (user.getWhatsappNum() != null && user.getWhatsappNum().length() > 0) {
            containerSocialMedia.setVisibility(View.VISIBLE);
            btnWhatsapp.setVisibility(View.VISIBLE);
            btnWhatsapp.setOnClickListener(this);
        } else {
            btnWhatsapp.setVisibility(View.GONE);
        }

        if (user.getInstagramID() != null && user.getInstagramID().length() > 0) {
            containerSocialMedia.setVisibility(View.VISIBLE);
            btnInstagram.setVisibility(View.VISIBLE);
            btnInstagram.setOnClickListener(this);
        } else {
            btnInstagram.setVisibility(View.GONE);
        }

        if (user.getFacebookID() != null && user.getFacebookID().length() > 0) {
            containerSocialMedia.setVisibility(View.VISIBLE);
            btnFacebook.setOnClickListener(this);
            btnFacebook.setVisibility(View.VISIBLE);
        } else {
            btnFacebook.setVisibility(View.GONE);
        }

        if (user.getShowEmail()) {
            containerSocialMedia.setVisibility(View.VISIBLE);
            btnMail.setOnClickListener(this);
            btnMail.setVisibility(View.VISIBLE);
        } else {
            btnMail.setVisibility(View.GONE);
        }

        final Activity act = getActivity();
        if (act instanceof ProfileActivity) {
            ProfileActivity.hideLoadingScreen();
            new Messenger(getContext(), HolderCurrentAccountManager.getCurrent(null).getCurrentUser(-1).getUid(), user.getUid(), (ViewGroup) getView().findViewById(R.id.rootView), containerScreens, database);

            profileControls.setVisibility(View.GONE);
        } else {
            profileControls.findViewById(R.id.cerrarSesion).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ProfileFragmentOwnUser) getParentFragment()).signOut();
                }
            });
            profileControls.findViewById(R.id.eliminarCuenta).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ProfileFragmentOwnUser) getParentFragment()).eliminarCuenta();
                }
            });
            if (user.getIsPro()) {
                editarPerfil.setVisibility(View.VISIBLE);
                editarPerfil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ProfileFragmentOwnUser) getParentFragment()).editarPerfil();
                    }
                });
            }
            ProfileFragmentOwnUser.hideLoadingScreen();
        }

        getProfilePic();
        getInsignias();
    }

    private void getProfilePic() {
        if (user.getHasPic()) {
            StorageReference storage = FirebaseStorage.getInstance().getReference();
            final StorageReference userRef = storage.child(Constants.FIREBASE_USERS_PRO_CONTAINER_NAME + "/" + user.getUid() + user.getImgVersion() + ".jpg");
            GlideApp.with(getContext())
                    .load(userRef)
                    .into(ivProfilePic);

            ivProfilePic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showImageExpanded(getContext(), ivProfilePic, userRef);
                    return false;
                }
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showImageExpanded(Context context, ImageView source, StorageReference ref) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.expanded_iv, null);
        ImageView imageView = dialogView.findViewById(R.id.img);

        GlideApp.with(getContext())
                .load(ref)
                .into(imageView);

        final CustomDialog dialog = new CustomDialog(context);
        dialog.setView(dialogView);
        dialog.show();

        source.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (dialog.isShown()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    int action = event.getActionMasked();
                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        dialog.hide();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnInstagram:
                Intent intentInstagram = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/" + user.getInstagramID()));

                try {
                    intentInstagram.setPackage("com.instagram.android");
                    startActivity(intentInstagram);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/" + user.getInstagramID())));
                }
                break;

            case R.id.btnFacebook:

                Intent intentFacebook = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=http://facebook.com/" + user.getFacebookID()));
                try {
                    intentFacebook.setPackage("com.facebook.katana");
                    startActivity(intentFacebook);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://facebook.com/" + user.getFacebookID())));
                }
                break;

            case R.id.btnMail:

                Intent intentEmail = new Intent(Intent.ACTION_SENDTO);
                intentEmail.setData(Uri.parse("mailto:" + user.getEmail()));
                startActivity(intentEmail);
                break;

            case R.id.btnWhatsapp:

                Intent intentWpp = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + user.getWhatsappNum()));
                try {
                    intentWpp.setPackage("com.whatsapp");
                    startActivity(intentWpp);
                } catch (ActivityNotFoundException e) {
                    CustomSnackBar.makeText(getView().findViewById(R.id.rootView), "Whatsapp not installed");
                }

                break;
            case R.id.btnMoreInfo:

                CustomDialog dialog = new CustomDialog(getContext());
                dialog.setPositiveButton(getString(R.string.aceptar), null);
                View view = getLayoutInflater().inflate(R.layout.more_info_layout, null);
                dialog.setView(view);
                TextView tvCoworkers = view.findViewById(R.id.tvCoworkers);
                TextView tvObraSocial = view.findViewById(R.id.tvobrasocial);
                TextView tvMovilidadPropia = view.findViewById(R.id.tvmovilidadpropia);
                TextView credit = view.findViewById(R.id.tvcredit);
                TextView debit = view.findViewById(R.id.tvdebit);

                if (user.getAcompanantes().size() > 0) {
                    tvCoworkers.setText(getString(R.string.trabaja_acompanado, user.getAcompanantes().size()));
                }

                if (user.getObrasocial() != null) {
                    tvObraSocial.setText(getString(R.string.posee_obra_social, user.getObrasocial()));
                }

                if (user.isMovilidadPropia()) {
                    tvMovilidadPropia.setText(getString(R.string.posee_movilidad_propia));
                }

                if (user.isCredit()) {
                    credit.setText(getString(R.string.acepta_cr_dito));
                }

                if (user.isDebit()) {
                    debit.setText(getString(R.string.acepta_debito));
                }

                dialog.setView(view);
                dialog.show();
                break;
            case R.id.infoApp:
                CustomDialog dialog2 = new CustomDialog(getContext());
                dialog2.setPositiveButton(getString(R.string.aceptar), null);
                dialog2.setMessage("Ezequiel Gimenez\neze.gimenez.98@gmail.com");
                dialog2.show();
                break;
        }
    }

    private void getInsignias() {
        database.child(Constants.FIREBASE_QUALITY_CONTAINER_NAME).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                QualityInfo qualityInfo = dataSnapshot.getValue(QualityInfo.class);
                if (isRunning) {
                    putInsignias(qualityInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showContactInfo() {
        View view = getLayoutInflater().inflate(R.layout.contact_info, null);
        View containerTel2 = view.findViewById(R.id.containerTel2);
        TextView tvEmail = view.findViewById(R.id.tvEmail);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "";
                switch (v.getId()) {
                    case R.id.btnCall1:
                        uri = "tel:" + user.getTelefono1();
                        break;

                    case R.id.btnCall2:
                        uri = "tel:" + user.getTelefono2();
                        break;
                }
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        };

        view.findViewById(R.id.btnCall1).setOnClickListener(listener);
        TextView tvTel1 = view.findViewById(R.id.tvTel1);
        tvTel1.setText(user.getTelefono1());

        if (user.getTelefono2().isEmpty()) {
            containerTel2.setVisibility(View.GONE);
        } else {
            TextView tvTel2 = view.findViewById(R.id.tvTel2);
            tvTel2.setText(user.getTelefono2());
            view.findViewById(R.id.btnCall2).setOnClickListener(listener);
        }

        if (!user.getShowEmail()) {
            tvEmail.setVisibility(View.GONE);
        } else {
            tvEmail.setText(user.getEmail());
        }

        CustomDialog customDialog = new CustomDialog(getContext());
        customDialog.setView(view)
                .setTitle(getString(R.string.contact_info))
                .setPositiveButton(getString(R.string.aceptar), null)
                .show();

    }

    public void showReviews() {

        final CountDownTimer timer = new CountDownTimer(8000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                CustomSnackBar.makeText(getView().findViewById(R.id.rootView), "Revisa tu conexión");
                getActivity().onBackPressed();
            }
        }.start();

        final CustomDialog dialog = new CustomDialog(getContext());
        final View view = getLayoutInflater().inflate(R.layout.reviews_alert_dialog, null);
        final LinearLayout containerReviews = view.findViewById(R.id.ContainerReviews);

        dialog.setView(view)
                .setPositiveButton(getString(R.string.aceptar), null)
                .show();

        database
                .child(Constants.FIREBASE_REVIEWS_CONTAINER_NAME)
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        timer.cancel();
                        int i = 0;
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            Review r = d.getValue(Review.class);
                            if (r != null) {
                                View comment = getLayoutInflater().inflate(R.layout.comment, null);
                                TextView tvUsername = comment.findViewById(R.id.tvUsername);
                                TextView msg = comment.findViewById(R.id.tvMessage);
                                SimpleRatingBar ratingBar = comment.findViewById(R.id.ratingBar);
                                CircleImageView ivPic = comment.findViewById(R.id.ivImage);

                                tvUsername.setText(r.getReviewerUsername());
                                if (r.getMsg() != null && r.getMsg().length() > 0) {
                                    msg.setText(r.getMsg());
                                } else {
                                    msg.setVisibility(View.GONE);
                                }
                                ratingBar.setRating(r.getRating());

                                containerReviews.addView(comment);

                                i++;
                            }
                            if (i > 10) {
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isRunning = false;
    }

    private void putInsignias(QualityInfo qualityInfo) {
        if (qualityInfo != null) {
            if (qualityInfo.getAtencion() >= Constants.MIN_ATENCION_INSIGNIA) {
                TextView insigniaAtencion = getView().findViewById(R.id.ivInsigniaAtencion);
                insigniaAtencion.setVisibility(View.VISIBLE);
            } else {
                TextView insigniaAtencion = getView().findViewById(R.id.ivInsigniaAtencion);
                insigniaAtencion.setVisibility(View.GONE);
            }
            if (qualityInfo.getCalidad() >= Constants.MIN_CALIDAD_INSIGNIA) {
                TextView insigniaCalidad = getView().findViewById(R.id.ivInsigniaCalidad);
                insigniaCalidad.setVisibility(View.VISIBLE);
            } else {
                TextView insigniaCalidad = getView().findViewById(R.id.ivInsigniaCalidad);
                insigniaCalidad.setVisibility(View.GONE);
            }
            if (qualityInfo.getTiempoResp() >= Constants.MIN_TIEMPO_RESP_INSIGNIA) {
                TextView insigniaTiempoResp = getView().findViewById(R.id.ivInsigniaTiempoResp);
                insigniaTiempoResp.setVisibility(View.VISIBLE);
            } else {
                TextView insigniaTiempoResp = getView().findViewById(R.id.ivInsigniaTiempoResp);
                insigniaTiempoResp.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        UiSettings setts = googleMap.getUiSettings();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(user.getMapCenterLat(), user.getMapCenterLng()),
                user.getMapZoom()));

        setts.setAllGesturesEnabled(false);


        MapHighlighter mapHighlighter = new MapHighlighter(getContext(), googleMap);
        mapHighlighter.highlightMap(new LatLng(user.getMapCenterLat(), user.getMapCenterLng()));
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}