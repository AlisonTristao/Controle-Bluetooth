package com.example.controlebluetooth;

//importa a lista (janelinha q aparece na tela)

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Set;

public class Dispositivos extends ListActivity {

    //endereço mac e o bluetooth do celular
    static String endMac = null;
    BluetoothAdapter adapBT = null;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //cria a lista (como uma janela)
        ArrayAdapter<String> disp = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1);

        adapBT = BluetoothAdapter.getDefaultAdapter();//adaptador bluetooth
        //pega a lista dos dispositivos pareados
        Set<BluetoothDevice> dispPareados;
        dispPareados = adapBT.getBondedDevices();

        //preenche a lista de dispositivos na lista
        if(dispPareados.size() > 0) {//verifica se tem algum
            for (BluetoothDevice dispo : dispPareados) {//para cada dispBT add na lista
                //pega o nome e o endereço mac
                String nomeDisp = dispo.getName();
                String macDisp = dispo.getAddress();

                //add na lista (\n = quebra de linha)
                disp.add(nomeDisp + "\n" + macDisp);
            }
        }else{
            Toast.makeText(this, "Nenhum dispositivo pareado!",
                                                                    Toast.LENGTH_SHORT).show();
        }

        //define pra ser visivel
        setListAdapter(disp);
    }

    //quando clica em algum item da lista
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //pega os dados totais da area de texto clicada
        String dados = ((TextView) v).getText().toString();

        //endereço mac são os 17 primeiros caracteres
        String mac = dados.substring(dados.length() - 17);

        //intent para retornar o endereco mac pra fazer coneão
        Intent retornaMac = new Intent();
        retornaMac.putExtra(endMac, mac);//retorna o endereco mac com nome endMac
        setResult(RESULT_OK, retornaMac);//retorna ok
        finish();//fecha a lista
    }
}
