package br.com.nobre.localizacaousuario;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //validar as permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // objeto responsável por gerenciar a localização do usuario
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // quando a localização do usuario muda
                Log.i("localizacao", "onLocationChanged: " + location.toString());
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                /*
                 * Geocoding -> processo de transformar um endereço ou descrição de um local em Lat/Long
                 * Reverse Geocoding -> processo de transfomar Lat/Long em um endereço
                 * */


                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    // Reverse Geocoding
                    //List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude, 1); // lat , long, max resultados -- > recebe endereco

                    // Geocoding
                    String stringEndereco = "Rua Ernesto da Silva Rocha - Estância Velha 1734, Canoas - RS";
                    List<Address> listaEndereco = geocoder.getFromLocationName(stringEndereco,1); // recupera Lat/Long
                    if (listaEndereco != null && listaEndereco.size() > 0) {
                        Address endereco = listaEndereco.get(0);

                        /*endereco: Address[
                            addressLines=[0:"R. Augusto Severo, 1700 - Nossa Sra. das Gracas,Canoas - RS, 92110-390, Brasil"],
                            feature=1700,
                            admin=Rio Grande do Sul,
                            sub-admin=Canoas,
                            locality=Canoas,
                            thoroughfare=Rua Augusto Severo,
                            postalCode=92110-390,
                            countryCode=BR,
                            countryName=Brasil
                            -- todas podem ser recuperadas
                            */
                        Log.d("local", "endereco: " + endereco.toString());

                        Double lat = endereco.getLatitude();
                        Double lon = endereco.getLongitude();

                        mMap.clear();// limpa o mapa a cada atualização da localização
                        LatLng localUsuario = new LatLng(lat, lon);
                        mMap.addMarker(new MarkerOptions().position(localUsuario).title("Meu local"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 18));

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                // quando o estatod do serviço de localização muda(Ativado ou desativado)
            }

            @Override
            public void onProviderEnabled(String s) {
                // quando o usuario habilita o serviço de localização
            }

            @Override
            public void onProviderDisabled(String s) {
                // quando o usuario desabilita o serviço de localização
            }
        };

        // recuperar localização do usuario
        /*
         * 1 - provedor de localização
         * 2 - Tempo mínimo entre atualizações de localização (milesegundos)
         * 3 - Distância mínima entre atualizações de localização (metros)
         * Location Listner ( Para recebermos as atualizações)*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { // checa se a permissão de localização está ativada
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, // 0 é todas as atualizações
                    0,
                    locationListener
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) { // permissao negada
                //Alerta
                alertaValidacaoPermissao();
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) { // permissao concedida
                // recuperar localização do usuario
                /*
                 * 1 - provedor de localização
                 * 2 - Tempo mínimo entre atualizações de localização (milesegundos)
                 * 3 - Distância mínima entre atualizações de localização (metros)
                 * Location Listner ( Para recebermos as atualizações)*/
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { // checa se a permissão de localização está ativada
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0, // 0 é todas as atualizações
                            0,
                            locationListener
                    );
                }

            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões solicitadas!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
