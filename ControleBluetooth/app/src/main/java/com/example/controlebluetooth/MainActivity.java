package com.example.controlebluetooth;

/*
* Autor: Alison de Oliveira Tristão
* Email: AlisonTristao@hotmail.com
* */

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static MainActivity.conTh conTh;
    //cria os componentes q serão usados (botoes e txt...)
    private Button btnConectar;
    private TextView txtDir;
    private TextView txtEsq;
    private SeekBar seekbarEsq;
    private SeekBar seekbarDir;
    private int valores[] = new int[2];//guarda a velocidade de cada motor - esq 0 e dir 1

    //variaveis para a conexão bluetoohth
    BluetoothAdapter adapBT = null;//adaptador bluetooth
    BluetoothDevice devBT = null; //dispositipo bluetooth
    BluetoothSocket socBT = null; //entrada/canal bluetooth
    public boolean con = false; //conectado ou nao
    static String endMac = null; //endereço mac do dispositivo
    /*maracutaia pra comunicação bt (todas os dispositivos devem ter esse UUID)
    cada dispositivo tem uma porta UUID diferente, então vai dar erro se tentar conectar com algo
    diferente (computador, tablet...)*/
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private Handler handler; // handler that gets info from Bluetooth service

    //(cada request tem um int para sua identificação)
    public static final int REQ_BT = 1; //requisição da ativação bt
    public static final int REQ_CON = 2; //requisição para conexão bt

    public void iniciarComponentes(){
        //define os componentes que criamos como os elementos do xml
        btnConectar = findViewById(R.id.btnConectar);
        txtEsq = findViewById(R.id.txtDir);//(botei invertido kkkkkk)
        txtDir = findViewById(R.id.txtEsq);
        seekbarEsq = findViewById(R.id.seekBar);
        seekbarDir = findViewById(R.id.seekBar2);

        //full screen
        hideSystemBars();
    }

    //esconde as barras de navegação e barra de status
    protected void hideSystemBars() {
        //deixa app em full screen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY//define q mesmo se encostar na tela continua hide
        | View.SYSTEM_UI_FLAG_FULLSCREEN //esconde status bar
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//esconde statusbar
        );
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicia os componentes
        iniciarComponentes();

        // -------- inicia o bluetooth -------- //

        adapBT = BluetoothAdapter.getDefaultAdapter();
        //verifica se o dispositivo possui bluetooth
        if (adapBT == null) {
            //dispositivo não suporta bluetooth
            Toast.makeText(this, "Seu dispositivo não possui Bluetooth!",
                    Toast.LENGTH_SHORT).show();
            //é preciso verifica pois se tentar ligar o bt sem ter bt crasha o app
        } else if (!adapBT.isEnabled()) { //verifica se o bluetooth está desligado
            //pede pra ligar o bluetooth
            //(aqui da um erro q n sei o pq, eu suprimi e funcionou ok)
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQ_BT);
        }

        // -------- codigos dos botoes -------- //

        //abre a lista de dispositivos pareados ou desconecta
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //se não estiver conectado abre a lista de dispositivos
                if (con) {//se estiver conectado
                    try{//tenta desconectar
                        socBT.close();
                        Toast.makeText(MainActivity.this, "Desconectado!",
                                Toast.LENGTH_SHORT).show();
                        //define como desconectado
                        con = false;
                        btnConectar.setText("Conectar");
                    } catch (IOException er) {//mensagem de erro
                        Toast.makeText(MainActivity.this, "Erro ao desconectar!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {//senão abre uma lista com dispositivos pareados
                    Intent abreList = new Intent(MainActivity.this, Dispositivos.class);
                    startActivityForResult(abreList, REQ_CON);
                }
            }
        });

        //atualiza o valor de valores quando muda a posição do seekbar
        seekbarEsq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //atualiza o valor no vetor
                valores[0] = i;

                //altera o txt
                txtEsq.setText(Integer.toString(i));
            }

            //codigo gerado automatico não sei pra q funciona
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //quando clica na seekbar
                //(não faz nada)
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //quando solta a seekbar
                seekbarEsq.setProgress(0);//volta pra zero
            }
        });

        seekbarDir.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //atualiza o valor no vetor
                valores[1] = i;

                //altera o txt
                txtDir.setText(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //quando clica na seekbar
                //(não faz nada)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //quando solta a seekbar
                seekbarDir.setProgress(0);//volta pra zero
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {//verifica qual a requisição feita
            case REQ_BT://caso for uma requisição para ligar o bt
                if (resultCode == Activity.RESULT_OK) {//se clicou ok
                } else {//se não deixou ligar o bt
                    Toast.makeText(this, "Erro ao ligar o Bluetooth!",
                                                                    Toast.LENGTH_SHORT).show();
                }
                break;

            case REQ_CON://caso seja requisição para conectar em um dispositivo
                if (resultCode == Activity.RESULT_OK) {//temos o endereço mac que retorna da lista
                    //salva o endereço mac q retornou
                    endMac = data.getExtras().getString(Dispositivos.endMac);

                    //buscamos o dispositivo bluetooth pelo seu endereço mac
                    devBT = adapBT.getRemoteDevice(endMac);

                    //criamos um canal de comunicação e começamos a enivar os dados
                    try {
                        //criamos um canal de comunicação com UUID
                        socBT = devBT.createInsecureRfcommSocketToServiceRecord(uuid);

                        //conecta com o dispostivo
                        socBT.connect();

                        //define os fluxos de entrada e saida de dados
                        conTh = new conTh(socBT);
                        conTh.start();

                        //mensagem
                        Toast.makeText(this, "Conectado com: " + endMac,
                                                                    Toast.LENGTH_SHORT).show();

                        //define como conectado
                        con = true;
                        btnConectar.setText("Desconectar");

                        //começa enviar os dados bluetooth
                        new th().start();

                    }catch (IOException er){//mensagem de erro
                        Toast.makeText(this, "Erro ao criar comunicação bluetooth!",
                                                                    Toast.LENGTH_SHORT).show();

                        //define como desconectado
                        con = false;
                        btnConectar.setText("Conectar");

                    }
                }else{//nao conseguiu obeter o endereço mac
                    Toast.makeText(this, "Erro ao obeter endereço Mac!",
                                                                    Toast.LENGTH_SHORT).show();
                }

                //full screen
                hideSystemBars();
                break;
        }
    }

    //classe para enviar e receber dados pela porta/canal socket
    private class conTh extends Thread {
        private final InputStream mmInStream;//saida
        private final OutputStream mmOutStream;//entrada

        public conTh(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Obtem o fluxo de dados de entrada e saida
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Erro no InputStream!",
                                                                    Toast.LENGTH_SHORT).show();
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Erro no OutputStream!",
                                                                    Toast.LENGTH_SHORT).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //isso aqui é diferente (tem forma melhor de obter os dados)
        /*public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(MessageConstants.MESSAGE_READ,
                                                                        numBytes, -1, mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }*/

        // enviar dados
        public void enviar(String texto) {
            //converte String pra um vetor de bytes
            byte[] bytes = texto.getBytes();
            try{
                //fluxo de saida (envia os bytes)
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Erro ao enviar dados!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class th extends Thread{//função q vai executar em segundo plano
        public void run() {
            while (con == true){//enquanto a conexão estiver ativa
                //manda os valores
                MainActivity.conTh.enviar("[" + valores[0] + "," + valores[1] + "]");
                try {
                    //espera 100 milis segundos
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}